package main

import (
	"crypto/tls"
	"crypto/x509"
	"flag"
	"io/ioutil"
	"log"
	"net"

	"github.com/spacemonkeygo/openssl"
)

type Config struct {
	hostName    string
	nodeName    string
	description string
	certFile    string
	keyFile     string
	passphrase  string
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
	flag.StringVar(&c.passphrase, "p", "", "Passphrase for encrypted key")
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

func (config *Config) DialTLS(network, addr string) (net.Conn, error) {
	log.Printf("DialTLS network=%s addr=%s", network, addr)

	certBytes, err := ioutil.ReadFile(config.certFile)
	if err != nil {
		return nil, err
	}

	keyBytes, err := ioutil.ReadFile(config.keyFile)
	if err != nil {
		return nil, err
	}

	cert, err := openssl.LoadCertificateFromPEM(certBytes)
	if err != nil {
		return nil, err
	}

	key, err := openssl.LoadPrivateKeyFromPEMWithPassword(keyBytes, config.passphrase)
	if err != nil {
		return nil, err
	}

	ctx, err := openssl.NewCtx()
	if err != nil {
		return nil, err
	}

	err = ctx.UseCertificate(cert)
	if err != nil {
		return nil, err
	}

	err = ctx.UsePrivateKey(key)
	if err != nil {
		return nil, err
	}

	ctx.SetVerifyMode(openssl.VerifyPeer)
	err = ctx.LoadVerifyLocations(config.caFile, "")
	if err != nil {
		return nil, err
	}

	conn, err := openssl.Dial(network, addr, ctx, 0)
	if err != nil {
		return nil, err
	}

	log.Printf("DialTLS done")

	return conn, nil
}
