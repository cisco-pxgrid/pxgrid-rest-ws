package main

import (
	"bytes"
	"context"
	"encoding/base64"
	"log"
	"net/http"
	"time"

	"nhooyr.io/websocket"
)

const (
	pongWait   = 30 * time.Second
	pingPeriod = (pongWait * 9) / 10
	writeWait  = time.Second
)

type Endpoint struct {
	c          *websocket.Conn
	ticker     *time.Ticker
	httpClient *http.Client
}

type EndpointData struct {
	Content []byte
	Err     error
}

func NewEndpoint(config *Config) (endpoint *Endpoint, err error) {

	transport := &http.Transport{
		DialTLS: config.DialTLS,
	}

	endpoint = &Endpoint{
		httpClient: &http.Client{Transport: transport},
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
		// TODO not finished. Need inactivity check. Perhaps listen for an activity channel?
		ctx, cancel := context.WithDeadline(context.Background(), time.Now().Add(pongWait))
		log.Printf("pinging...")
		err := e.c.Ping(ctx)
		log.Printf("pinged")
		cancel()
		if err != nil {
			log.Printf("ping err: %v", err)
			return
		}
	}
}

func (e *Endpoint) Connect(url, user, password string) (err error) {
	ctx, cancel := context.WithTimeout(context.Background(), time.Minute)
	defer cancel()

	e.c, _, err = websocket.Dial(ctx, url, &websocket.DialOptions{
		HTTPClient:   e.httpClient,
		HTTPHeader:   createBasicAuthHeader(user, password),
		Subprotocols: []string{"echo"},
	})
	if err != nil {
		return err
	}

	// TODO ping pong later
	// e.ws.SetPongHandler(func(string) error { e.ws.SetReadDeadline(time.Now().Add(pongWait)); return nil })
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
	return e.c.Write(context.Background(), websocket.MessageBinary, out.Bytes())
}

func (e *Endpoint) Listener(dataChan chan<- *EndpointData) {
	log.Println("Listening...")
	// TODO how to stop? Close the connection??
	ctx := context.Background()
	for {
		_, p, err := e.c.Read(ctx)
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
	err = e.c.Close(websocket.StatusNormalClosure, "Normal closure")
	return
}
