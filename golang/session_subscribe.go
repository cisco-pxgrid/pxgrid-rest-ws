package main

import (
	"fmt"
	"log"
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
	services, err := control.ServiceLookup("com.cisco.ise.session")
	if err != nil {
		log.Fatal(err)
	} else if len(services) == 0 {
		log.Fatal("Service unavailable")
	}

	// Use first service
	wsPubsubService := services[0].Properties["wsPubsubService"]
	sessionTopic := services[0].Properties["sessionTopic"]
	log.Println("wsPubsubService=", wsPubsubService, "sessionTopic=", sessionTopic)

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

	err = endpoint.Subscribe(sessionTopic)
	if err != nil {
		log.Fatal(err)
	}

	dataChan := make(chan *EndpointData)
	go dataPrinter(dataChan)
	go endpoint.Listener(dataChan)

	log.Println("Press <enter> to disconnect...")
	fmt.Scanln()
	endpoint.Disconnect()
	close(dataChan)
}
