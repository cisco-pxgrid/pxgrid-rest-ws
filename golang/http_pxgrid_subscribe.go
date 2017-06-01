package main 

import (
        "log"
        "time"
        "fmt"
)

func main() {
    user, host, certFile, keyFile, caFile, topicKey, serviceName, index := ParseCommandLine()
	
    tlsConfig, err := InitTlsConfig(certFile, keyFile, caFile)
	
    if err != nil {
	log.Fatal(err)
     }
	 
    control := InitControl(tlsConfig, host, user);
    
    func() {
	    for { // wait for acccount to be activated
	        res, err := control.AccountActivate()
	        
		    if err != nil {
				log.Fatal(err)
			}
	        
	        log.Println("account_state=" + res.AccountState + ":controller_version=" + res.Version)
	
	        if res.AccountState == "ENABLED" {
	                break
	        }
	
	        log.Println("sleeping ...")
	        time.Sleep(10 * time.Second)
		}
    }()
    
    // Any service will do as we are simply extracting topic and wsPubsubService name
    services, err := control.LookupService(serviceName)
	
    if err != nil {
	log.Fatal(err)
     } else if len(services) == 0 {
	log.Fatal("No services found for ", serviceName)
     }
	
     wsPubsubService := services[0].Properties["wsPubsubService"]
     topic := services[0].Properties[topicKey]
	
    log.Println("wsPubsubService=", wsPubsubService)
    log.Println(	"topic=", topic)
	
    pubsubServices, err := control.LookupService(wsPubsubService)
	
    if err != nil {
	log.Fatal("Error occured while looking up", wsPubsubService, err)
    } else if len(pubsubServices) == 0 {
	log.Fatal("No pubsub services found using ", wsPubsubService)
    }
	
    // Here we should loop in case of failures. Sample will just use service at index 0
    pubsubService := pubsubServices[index]
	
    log.Println("wsPubsubNode=", pubsubService.NodeName)
    log.Println("wsUrl=", pubsubService.Properties["wsUrl"])
    
    // Get shared secret between user and wsPubsubNode
    secret, err := control.GetAccessSecret(pubsubService.NodeName)
	
    if err != nil {
	log.Fatal(err)
    }
	
    var endpoint Endpoint
	
    endpoint.Init(tlsConfig)
	
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
	
   endpoint.ConnectUsing(pubsubService.Properties["wsUrl"], user, secret)
   endpoint.Subscribe(topic)
   go endpoint.Receive()

   input := ""

   for input != "q" {
	fmt.Scanln(&input)
	
	if input == "d" {
	    endpoint.Disconnect()
	}
   }	
}

