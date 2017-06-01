package main

import (
	"crypto/tls"
     "crypto/x509"
     "io/ioutil"
     "net"
     "net/http"
      "encoding/base64"
     "flag"
     "time"
     "bytes"
     "bufio"
     "errors"
)

const (
	connectTimeout = 30 * time.Second
	rwTimeout = 30 * time.Second
)

var supportedServices = map[string]string {
	"securityGroupTopic" : "com.cisco.ise.trustsec.security.group",
	"sessionTopic" : "com.cisco.ise.session",
}

func ParseCommandLine() (user, host, certFile, keyFile, caFile, topicName, serviceName string, index int) {
    flag.StringVar(&certFile, "cert", "golang_.cer", "A PEM eoncoded certificate file.")
    flag.StringVar(&keyFile, "key", "golang_.key", "A PEM encoded private key file.")
    flag.StringVar(&caFile, "CA", "golang_root.cer", "A PEM eoncoded CA's certificate file.")
    flag.StringVar(&user, "user", "golang", "pxgrid client name")
    flag.StringVar(&host, "host", "pxgrid-host-fqdn.cisco.com", "pxgrid fqdn ")
    flag.StringVar(&topicName, "topicName", "sessionTopic", "pxgrid service ")
    flag.IntVar(&index, "index", 0, "service index when testing HA")
    flag.Parse()
    return user, host, certFile, keyFile, caFile, topicName, supportedServices[topicName], index
}

func TimeoutDialer() func(net, addr string) (c net.Conn, err error) {
    return func(netw, addr string) (net.Conn, error) {
    	conn, err := net.DialTimeout(netw, addr, connectTimeout)
        
    	if err != nil {
        	return nil, err
    	}
        
    	// conn.SetDeadline(time.Now().Add(rwTimeout))
        return conn, nil
    }
}

func InitTlsConfig(certFile, keyFile, caFile string)(*tls.Config, error) {
    cert, err := tls.LoadX509KeyPair(certFile, keyFile)
    
    if err != nil {
            return nil, err
    }

    caCert, err := ioutil.ReadFile(caFile)
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


func CreateBasicAuthHeader(user, secret string) (http.Header) {
	userPassword := user + ":" + secret
    sEnc := "Basic " + base64.StdEncoding.EncodeToString([]byte(userPassword))
    return http.Header{"Authorization": {sEnc}}
}

func SubscribeMessage(topic string) ([] byte) {
	message := "SUBSCRIBE\n"  + "destination:" + topic + "\n" + "id:0\n\n"
    var b bytes.Buffer 

	b.WriteString(message)
	b.WriteByte(0) 
	return b.Bytes()
}

func ParseStompMessage(frame []byte) ([] byte, error) {
	r := bytes.NewReader(frame)
	x  := bufio.NewReader(r)
	
	line, _, _ := x.ReadLine()
	cmd := string(line)
	
	switch cmd {
		case "MESSAGE":
			for {
				if line, _, _ = x.ReadLine(); string(line) == "" { // Done reading STOMP headers
					break
				}	
			}
			
			content, _ := x.ReadBytes(0)
			return content, nil
		case "ERROR":	
			for {
				if line, _, err := x.ReadLine(); err != nil {
					return nil, err
				} else if string(line) == "message" {
					return nil, errors.New(string(line))
				}	
			}
		default:	
			return frame, errors.New("Received unexpected message:" + cmd)
	}	
}

func PParseStompMessage(frame []byte) ([] byte, error) {
	r := bytes.NewReader(frame)
	x  := bufio.NewReader(r)
	
	// var line []byte
	
	line, _, _ := x.ReadLine()
	
	if string(line) == "MESSAGE" { 
		for {
			line, _, _ = x.ReadLine()
			
			if string(line) == "" { // Done reading STOMP headers
				break
			}	
		}
		
		content, _ := x.ReadBytes(0)
		return content, nil
	} else if string(line) == "ERROR" {	
		for {
			line, _, _ = x.ReadLine()
			
			if string(line) == "message" {
				return nil, errors.New(string(line))
			}	
			
			if string(line) == "" { // Done reading STOMP headers
				break
			}	
		}
	} 
	
	return frame, errors.New("Received unknow message")		
}

