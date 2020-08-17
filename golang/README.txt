Install Go
Download using golang website:
  https://golang.org/dl/
Or use brew on MacOS
> brew install go

Install websocket library. Set GOPATH go where golang packages can be stored:
> export GOPATH="..."
> go get github.com/gorilla/websocket

Build sample session_subscribe:
> go build session_subscribe.go config.go pxgrid_control.go pubsub_endpoint.go stomp.go 

The quickest way to get started is to generate a pxGrid client cert on Cisco ISE.  
Use PEM/PKCS8 format for Certificate Download format.  
Go does not have built-in support for encrypted PKCS8 key, so decrypt the client key using openssl command:
> openssl pkcs8 -in client.key  > client.key.clear

Run sample session_subscribe:
> ./session_subscribe -a <hostname> -n <nodename> -c <client-chain.cer> -k <client.key.clear> -s <server.cer>

