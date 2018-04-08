package main

import (
	"bytes"
	"encoding/base64"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

const (
	pongWait   = 60 * time.Second
	pingPeriod = (pongWait * 9) / 10
	writeWait  = time.Second
)

type Endpoint struct {
	dialer *websocket.Dialer
	ws     *websocket.Conn
	ticker *time.Ticker
}

type EndpointData struct {
	Content []byte
	Err     error
}

func NewEndpoint(config *Config) (endpoint *Endpoint, err error) {
	endpoint = &Endpoint{}
	tlsConfig, err := config.GetTLSConfig()
	if err != nil {
		return
	}
	endpoint.dialer = &websocket.Dialer{
		TLSClientConfig: tlsConfig,
	}
	return
}

func createBasicAuthHeader(user, secret string) http.Header {
	userPassword := user + ":" + secret
	encoded := "Basic " + base64.StdEncoding.EncodeToString([]byte(userPassword))
	return http.Header{"Authorization": {encoded}}
}

func (e *Endpoint) pinger(ch <-chan time.Time) {
	for range ch {
		err := e.ws.WriteControl(websocket.PingMessage, []byte(""), time.Time{})
		if err != nil {
			return
		}
	}
}

func (e *Endpoint) Connect(url, user, password string) (err error) {
	e.ws, _, err = e.dialer.Dial(url, createBasicAuthHeader(user, password))
	if err != nil {
		return
	}
	e.ws.SetPongHandler(func(string) error { e.ws.SetReadDeadline(time.Now().Add(pongWait)); return nil })
	e.ticker = time.NewTicker(pingPeriod)
	go e.pinger(e.ticker.C)
	return
}

func (e *Endpoint) Subscribe(topic string) (err error) {
	stomp := NewStomp()
	stomp.command = "SUBSCRIBE"
	stomp.headers["destination"] = topic
	stomp.headers["id"] = "0"
	var out bytes.Buffer
	stomp.Write(&out)
	return e.ws.WriteMessage(websocket.BinaryMessage, out.Bytes())
}

func (e *Endpoint) Listener(dataChan chan<- *EndpointData) {
	log.Println("Listening...")
	e.ws.SetReadDeadline(time.Now().Add(pongWait))
	for {
		_, p, err := e.ws.ReadMessage()
		if err != nil {
			dataChan <- &EndpointData{Err: err}
			return
		}

		stomp := NewStomp()
		reader := bytes.NewReader(p)
		err = stomp.Parse(reader)
		if err != nil {
			dataChan <- &EndpointData{Err: err}
			return
		}
		dataChan <- &EndpointData{Content: stomp.content}
	}
}

func (e *Endpoint) Disconnect() (err error) {
	e.ticker.Stop()
	err = e.ws.Close()
	return
}
