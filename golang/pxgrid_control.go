package main

import (
	"bytes"
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

type Service struct {
	Name       string            `json:"name"`
	NodeName   string            `json:"nodeName"`
	Properties map[string]string `json:"properties"`
}

type AccountActivateRequest struct {
}

type AccountActivateResponse struct {
	AccountState string `json:"accountState"`
	Version      string `json:"version"`
}

type ServiceLookupRequest struct {
	Name string `json:"name"`
}

type ServiceLookupResponse struct {
	Services []Service `json:"services"`
}

type AccessSecretRequest struct {
	PeerNodeName string `json:"peerNodeName"`
}

type AccessSecretResponse struct {
	Secret string `json:"secret"`
}

type Control struct {
	config Config
	client *http.Client
}

func NewControl(config Config) (control *Control, err error) {
	tlsConfig, err := config.GetTLSConfig()
	if err != nil {
		return
	}
	transport := &http.Transport{
		TLSClientConfig: tlsConfig,
	}
	control = &Control{
		config: config,
		client: &http.Client{Transport: transport},
	}
	return
}

func (control *Control) sendRequest(url string, request interface{}, response interface{}) (err error) {
	requestBytes, err := json.Marshal(request)
	if err != nil {
		return
	}
	// For logging purpose
	slashIndex := strings.LastIndex(url, "/")
	urlSuffix := url[slashIndex+1:]
	log.Println(urlSuffix + " request=" + string(requestBytes[:]))

	req, err := http.NewRequest("POST", url, bytes.NewReader(requestBytes))
	if err != nil {
		return
	}
	req.Header.Add("Content-Type", "application/json")
	req.Header.Add("Accept", "application/json")
	req.SetBasicAuth(*control.config.nodeName, "")
	resp, err := control.client.Do(req)
	if err != nil {
		return
	}
	defer resp.Body.Close()
	responseBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return
	}
	log.Println(urlSuffix + " response=" + string(responseBytes[:]))
	err = json.Unmarshal(responseBytes, response)
	if err != nil {
		return
	}
	return
}

func (control *Control) AccountActivate() (response *AccountActivateResponse, err error) {
	url := "https://" + *control.config.hostName + ":8910/pxgrid/control/AccountActivate"
	request := AccountActivateRequest{}
	response = &AccountActivateResponse{}
	err = control.sendRequest(url, request, response)
	return
}

func (control *Control) ServiceLookup(serviceName string) (services []Service, err error) {
	url := "https://" + *control.config.hostName + ":8910/pxgrid/control/ServiceLookup"
	request := ServiceLookupRequest{serviceName}
	response := &ServiceLookupResponse{}
	err = control.sendRequest(url, request, response)
	if err != nil {
		return
	}
	services = response.Services
	return
}

func (control *Control) GetAccessSecret(peerNode string) (secret string, err error) {
	url := "https://" + *control.config.hostName + ":8910/pxgrid/control/AccessSecret"
	request := AccessSecretRequest{peerNode}
	response := &AccessSecretResponse{}
	err = control.sendRequest(url, request, response)
	if err != nil {
		return
	}
	secret = response.Secret
	return
}
