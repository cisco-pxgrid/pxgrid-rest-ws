package main

import (
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"
)

func dataPrinter(dataChan <-chan *EndpointData) {
	for data := range dataChan {
		if data.Err == nil {
			log.Println("Message=" + string(data.Content))
		} else {
			log.Println(data.Err)
		}
	}
}

func main() {
	config := NewConfig()
	control, err := NewControl(config)
	if err != nil {
		log.Fatal(err)
	}

	// AccountActivate
	for {
		res, err := control.AccountActivate()
		if err != nil {
			log.Fatal(err)
		}
		if res.AccountState == "ENABLED" {
			break
		}
		time.Sleep(30 * time.Second)
	}

	// pxGrid ServiceLookup for Session Directory
	services, err := control.ServiceLookup(config.service)
	if err != nil {
		log.Fatal(err)
	} else if len(services) == 0 {
		log.Fatal("Service unavailable")
	}

	// Use first service
	wsPubsubService := services[0].Properties["wsPubsubService"]
	topic := services[0].Properties[config.topic]
	log.Println("wsPubsubService=", wsPubsubService, "topic=", topic)

	// pxGrid ServiceLookup for pubsub service
	pubsubServices, err := control.ServiceLookup(wsPubsubService)
	if err != nil {
		log.Fatal(err)
	} else if len(pubsubServices) == 0 {
		log.Fatal("Pubsub service unavailable")
	}

	// Use first pubsub service
	pubsubService := pubsubServices[0]
	pubsubNodeName := pubsubService.NodeName
	wsUrl := pubsubService.Properties["wsUrl"]
	log.Println("wsUrl=", wsUrl)

	// pxGrid AccessSecret with the pubsub node
	secret, err := control.GetAccessSecret(pubsubNodeName)
	if err != nil {
		log.Fatal(err)
	}

	// Setup WebSocket
	endpoint, err := NewEndpoint(config)
	if err != nil {
		log.Fatal(err)
	}

	err = endpoint.Connect(wsUrl, config.nodeName, secret)
	if err != nil {
		log.Fatal(err)
	}

	err = endpoint.Subscribe(topic, config.filter)
	if err != nil {
		log.Fatal(err)
	}

	dataChan := make(chan *EndpointData)
	go dataPrinter(dataChan)
	wg := sync.WaitGroup{}

	go func() {
		wg.Add(1)
		endpoint.Listener(dataChan)
		wg.Done()
	}()

	// Setup abort channel
	log.Println("Press <Ctrl-c> to disconnect...")
	abort := make(chan os.Signal, 1)
	signal.Notify(abort, os.Interrupt, syscall.SIGTERM)
	<-abort

	// Cleanup
	log.Printf("Disconnecting websocket connection...")
	endpoint.Disconnect()

	wg.Wait()
	close(dataChan)
}
