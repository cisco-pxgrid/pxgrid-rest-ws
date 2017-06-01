Install golang Can use brew command or jsut download using golang website https://golang.org/dl/
>brew install go

Install  websocket library  set GOPATH go where golang packages can be stored:
> export GOPATH="..."
>go get github.com/gorilla/websocket

Build http_pxgrid_subscribe executable:
>go build http_pxgrid_subscribe.go  util.go pxgrid_control.go stomp_endpoint.go 

The quickest way to get started is to generate a pxgrid client cert on Ise.  
For CN enter golang 
Use PEM/PKCS8 format for Certificate Download format.  
Decrypt the key using openssl command  and save it to golang_.key.clear
Open zip file. There should be golang_.cer, golang_.key, golang_root.cer
For now our sample does not handle encrypted keys. Decrypt it  and save it to golang_.key.clear

>openssl pkcs8 -in golang_.key  > golang_.key.clear

To subscribe to session run the command:
./http_pxgrid_subscribe -cert=golang_.cer -key=golang_.key.clear -CA=CertificateServicesRootCA-xxxx_.cer -host=pxgrid-host-fqdn -user=golang


To subscribe to SGT topic run the command:
./http_pxgrid_subscribe -cert=golang_.cer -key=golang_.key.clear -CA=golang_root.cer -host=pxgrid-host-fqdn  -user=golang -topicName=securityGroupTopic


