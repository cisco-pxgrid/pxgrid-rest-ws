package main

import (
	"crypto/tls"
	"crypto/x509"
	"flag"
	"io/ioutil"
)

var supportedServices = map[string]string{
	"securityGroupTopic": "com.cisco.ise.trustsec.security.group",
	"sessionTopic":       "com.cisco.ise.session",
}

type Config struct {
	hostName    *string
	nodeName    *string
	description *string
	certFile    *string
	keyFile     *string
	password    *string
	caFile      *string
}

func NewConfig() Config {
	c := Config{}
	c.hostName = flag.String("a", "", "Host name (multiple accepted)")
	c.nodeName = flag.String("n", "", "Node name")
	c.description = flag.String("d", "", "Description (optional)")
	c.certFile = flag.String("c", "", "Client certificate chain .pem filename (not required if password is specified)")
	c.keyFile = flag.String("k", "", "Client key unencrypted .key filename (not required if password is specified)")
	c.password = flag.String("w", "", "Password (not required if client certificate is specified)")
	c.caFile = flag.String("s", "", "Server certificates .pem filename")
	flag.Parse()
	return c
}

func (config Config) GetTLSConfig() (*tls.Config, error) {
	cert, err := tls.LoadX509KeyPair(*config.certFile, *config.keyFile)

	if err != nil {
		return nil, err
	}

	caCert, err := ioutil.ReadFile(*config.caFile)
	if err != nil {
		return nil, err
	}

	caCertPool := x509.NewCertPool()
	caCertPool.AppendCertsFromPEM(caCert)
	tlsConfig := &tls.Config{
		Certificates: []tls.Certificate{cert},
		RootCAs:      caCertPool,
	}

	tlsConfig.BuildNameToCertificate()
	return tlsConfig, err
}
