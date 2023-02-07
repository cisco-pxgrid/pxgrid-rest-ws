Install Go
Download using golang website:
  https://golang.org/dl/
Or use brew on MacOS
> brew install go

Build sample session_subscribe:
> go build

The quickest way to get started is to generate a pxGrid client cert on Cisco ISE.  
Use PEM/PKCS8 format for Certificate Download format.

Concatenate the client cert chain from the certificates in the correct order:
> cat client.cer intermediate-n.cer...intermediate-1.cer root.cer > client-chain.cer

Go does not have built-in support for encrypted PKCS8 key, so decrypt the client key using openssl command:
> openssl pkcs8 -in client.key  > client.key.clear

Run sample session_subscribe:
> ./session_subscribe -a <hostname> -n <nodename> -c <client-chain.cer> -k <client.key.clear> -s <server.cer>

