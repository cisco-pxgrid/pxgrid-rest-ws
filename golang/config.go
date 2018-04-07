package main

import (
	"crypto/tls"
	"crypto/x509"
	"flag"
	"io/ioutil"
)

type Config struct {
	hostName    string
	nodeName    string
	description string
	certFile    string
	keyFile     string
	password    string
	caFile      string
}

func NewConfig() *Config {
	c := &Config{}
	flag.StringVar(&c.hostName, "a", "", "Host name (multiple accepted)")
	flag.StringVar(&c.nodeName, "n", "", "Node name")
	flag.StringVar(&c.description, "d", "", "Description (optional)")
	flag.StringVar(&c.certFile, "c", "", "Client certificate chain .pem filename (not required if password is specified)")
	flag.StringVar(&c.keyFile, "k", "", "Client key unencrypted .key filename (not required if password is specified)")
	flag.StringVar(&c.password, "w", "", "Password (not required if client certificate is specified)")
	flag.StringVar(&c.caFile, "s", "", "Server certificates .pem filename")
	flag.Parse()
	return c
}

func (config *Config) GetTLSConfig() (*tls.Config, error) {
	cert, err := tls.LoadX509KeyPair(config.certFile, config.keyFile)

	if err != nil {
		return nil, err
	}

	caCert, err := ioutil.ReadFile(config.caFile)
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
