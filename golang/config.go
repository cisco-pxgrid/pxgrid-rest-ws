package main

import (
	"crypto/tls"
	"crypto/x509"
	"flag"
	"os"
)

type Config struct {
	hostName    string
	nodeName    string
	description string
	certFile    string
	keyFile     string
	password    string
	caFile      string
	insecure    bool
	filter      string
	service     string
	topic       string
}

func NewConfig() *Config {
	c := &Config{}
	flag.StringVar(&c.hostName, "a", "", "Host name (multiple accepted)")
	flag.StringVar(&c.nodeName, "n", "", "Node name")
	flag.StringVar(&c.description, "d", "", "Description (optional)")
	flag.StringVar(&c.filter, "f", "", "Server Side Filter (optional)")
	flag.StringVar(&c.service, "service", "com.cisco.ise.session", "Service name (optional)")
	flag.StringVar(&c.topic, "topic", "sessionTopic", "Topic name (optional)")
	flag.StringVar(&c.certFile, "c", "", "Client certificate chain .pem filename (not required if password is specified)")
	flag.StringVar(&c.keyFile, "k", "", "Client key unencrypted .key filename (not required if password is specified)")
	flag.StringVar(&c.password, "w", "", "Password (not required if client certificate is specified)")
	flag.StringVar(&c.caFile, "s", "", "Server certificates .pem filename")
	flag.BoolVar(&c.insecure, "insecure", false, "Insecure skip validation")
	flag.Parse()
	return c
}

func (config *Config) GetTLSConfig() (*tls.Config, error) {
	if config.insecure {
		return &tls.Config{InsecureSkipVerify: true}, nil
	}

	var clientCert []tls.Certificate
	if config.certFile != "" {
		cert, err := tls.LoadX509KeyPair(config.certFile, config.keyFile)
		if err != nil {
			return nil, err
		}
		clientCert = []tls.Certificate{cert}
	}

	caCert, err := os.ReadFile(config.caFile)
	if err != nil {
		return nil, err
	}
	caCertPool := x509.NewCertPool()
	caCertPool.AppendCertsFromPEM(caCert)

	tlsConfig := &tls.Config{
		Certificates: clientCert,
		RootCAs:      caCertPool,
	}

	return tlsConfig, err
}
