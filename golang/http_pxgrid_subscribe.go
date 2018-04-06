package main

import (
	"fmt"
	"log"
	"time"
)

func main() {
	config := NewConfig()
	control, err := NewControl(config)
	if err != nil {
		log.Fatal(err)
	}

	for { // wait for acccount to be activated
		res, err := control.AccountActivate()
		if err != nil {
			log.Fatal(err)
		}
		if res.AccountState == "ENABLED" {
			break
		}
		time.Sleep(10 * time.Second)
	}

	// pxGrid ServiceLookup
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

	pubsubServices, err := control.ServiceLookup(wsPubsubService)

	if err != nil {
		log.Fatal("Error occured while looking up", wsPubsubService, err)
	} else if len(pubsubServices) == 0 {
		log.Fatal("No pubsub services found using ", wsPubsubService)
	}

	// Use the first service
	pubsubService := pubsubServices[0]

	log.Println("wsPubsubNode=", pubsubService.NodeName)
	log.Println("wsUrl=", pubsubService.Properties["wsUrl"])

	// Get shared secret between user and wsPubsubNode
	secret, err := control.GetAccessSecret(pubsubService.NodeName)
	if err != nil {
		log.Fatal(err)
	}

	endpoint, err := NewEndpoint(config)
	if err != nil {
		log.Fatal(err)
	}

	messageHandler := func(message []byte) {
		// Notifications. Let us print it ...
		log.Println(string(message))
	}

	errorHandler := func(err error) {
		log.Println("Got error", err, "Disconnecting endpoint ...")
		endpoint.Disconnect()
	}

	endpoint.SetMessageHandler(messageHandler)
	endpoint.SetErrorHandler(errorHandler)

	log.Println("Press q to exit:")

	endpoint.ConnectUsing(pubsubService.Properties["wsUrl"], *config.nodeName, secret)
	endpoint.Subscribe(sessionTopic)
	go endpoint.Receive()

	input := ""

	for input != "q" {
		fmt.Scanln(&input)

		if input == "d" {
			endpoint.Disconnect()
		}
	}
}
