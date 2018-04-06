package main

import (
	"bytes"
	"encoding/base64"
	"errors"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

const (
	writeWait       = time.Second
	pingDelay       = 60 * time.Second
	readBufferSize  = 102400
	writeBufferSize = 1024
)

type MessageHandler func(message []byte)
type ErrorHandler func(err error)

type pingTask struct {
	ws       *websocket.Conn
	gotPong  chan bool
	done     chan bool
	ehandler ErrorHandler
}

func (task *pingTask) init(ws *websocket.Conn, ehandler ErrorHandler) {
	task.ws = ws
	task.done = make(chan bool)
	task.gotPong = make(chan bool, 1)
	task.gotPong <- true
	task.ehandler = ehandler
}

func (task *pingTask) run() {
	if len(task.done) == 0 {
		if len(task.gotPong) != 0 {
			log.Println("Sending ping to server ...")
			task.ws.WriteControl(websocket.PingMessage, []byte(""), time.Now().Add(writeWait))
			<-task.gotPong
			return
		}

		task.ws.Close()
		log.Println("Warning did not get pong ... stopping ping task")
		task.ehandler(errors.New("Did not get a pong closing connection"))
		task.done <- true
	}
}

func (task *pingTask) onPong() {
	log.Println("Received pong (2) from server ...")
	task.gotPong <- true
}

func (task *pingTask) stop() {
	task.done <- true
}

func (task *pingTask) start() {
	go func() {
		for {
			select {
			case <-time.After(pingDelay):
			case <-task.done:
				break
			}

			task.run()
		}
	}()
}

type Endpoint struct {
	dialer   *websocket.Dialer
	ws       *websocket.Conn
	task     *pingTask
	mhandler MessageHandler
	ehandler ErrorHandler
}

func NewEndpoint(config Config) (endpoint *Endpoint, err error) {
	endpoint = &Endpoint{}
	tlsConfig, err := config.GetTLSConfig()
	if err != nil {
		return
	}
	endpoint.dialer = &websocket.Dialer{
		NetDial:         TimeoutDialer(),
		ReadBufferSize:  readBufferSize,
		WriteBufferSize: writeBufferSize,
		TLSClientConfig: tlsConfig,
	}
	return
}

func (endpoint *Endpoint) SetMessageHandler(mhandler MessageHandler) {
	endpoint.mhandler = mhandler
}

func (endpoint *Endpoint) SetErrorHandler(ehandler ErrorHandler) {
	endpoint.ehandler = ehandler
}

func createBasicAuthHeader(user, secret string) http.Header {
	userPassword := user + ":" + secret
	encoded := "Basic " + base64.StdEncoding.EncodeToString([]byte(userPassword))
	return http.Header{"Authorization": {encoded}}
}

func (endpoint *Endpoint) ConnectUsing(url, user, password string) (err error) {
	ws, resp, err := endpoint.dialer.Dial(url, createBasicAuthHeader(user, password))
	if err != nil {
		return err
	}

	log.Println("HTTP RESPONSE FROM WS SERVER:", *resp)

	// defaultPingHandler := ws.PingHandler();
	task := &pingTask{}
	task.init(ws, func(err error) { endpoint.ehandler(err) })

	pingHandler := func(message string) (err error) {
		log.Println("Received ping from server ...")
		// err = defaultPingHandler(message)
		err = ws.WriteControl(websocket.PongMessage, []byte(message), time.Now().Add(writeWait))

		if err == nil {
			log.Println("Sent pong to server ...")
		}

		return
	}

	pongHandler := func(message string) error {
		log.Println("Received pong from server ...")
		task.onPong()
		return nil
	}

	ws.SetPingHandler(pingHandler)
	ws.SetPongHandler(pongHandler)
	endpoint.ws = ws
	endpoint.task = task
	endpoint.task.start()
	return nil
}

func (endpoint *Endpoint) Subscribe(topic string) (err error) {
	stomp := NewStomp()
	stomp.command = "SUBSCRIBE"
	stomp.headers["destination"] = topic
	stomp.headers["id"] = "0"
	var out bytes.Buffer
	stomp.Write(&out)
	return endpoint.ws.WriteMessage(websocket.BinaryMessage, out.Bytes())
}

func (endpoint *Endpoint) Receive() {
	for {
		log.Println("In receive data ...")
		messageType, p, err := endpoint.ws.ReadMessage()

		if err != nil {
			endpoint.ehandler(err)
			return
		}

		if messageType != websocket.BinaryMessage {
			log.Println("WARN:Did not expect non-binary messages from pxgrid ...")
		}

		// content, err := ParseStompMessage(p)
		stomp := NewStomp()
		reader := bytes.NewReader(p)
		err = stomp.Parse(reader)

		if err != nil {
			endpoint.ehandler(err)
			return
		}

		endpoint.mhandler(stomp.content)
	}
}

func (endpoint *Endpoint) Disconnect() (err error) {
	endpoint.task.stop()
	endpoint.ws.Close()
	return nil
}
