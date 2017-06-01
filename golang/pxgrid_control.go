package main

import (
		"encoding/json"
        "strings"
        "io/ioutil"
	    "net/http"
        "crypto/tls"
)

type Service struct {
    Name string `json:"name"`
    NodeName string `json:"nodeName"`
    Properties map[string]string `json:"properties"`
}

type AccountActivateResponse struct {
    AccountState string `json:"accountState"`
    Version string `json:"version"`
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
	client *http.Client
	user string
	host string
} 

func InitControl(tlsConfig *tls.Config, host, user string) (control *Control) {
	transport := &http.Transport{ 
		TLSClientConfig: tlsConfig, 
		Dial: TimeoutDialer(), 
	}
	
	control = &Control { 
		client: &http.Client{ Transport: transport, },
		user: user,
		host: host,
	}
	
	return
}


func (control *Control) AccountActivate()(res *AccountActivateResponse, err error) {		
    url := "https://" + control.host + ":8910/pxgrid/control/AccountActivate"
    var req *http.Request
   
    req, err = http.NewRequest("POST", url, strings.NewReader("{}"))
    
    if err != nil {
            return
    }
    
    req.Header.Add("Content-Type", "application/json")
    req.Header.Add("Accept", "application/json")
    req.SetBasicAuth(control.user, "")
    resp, err := control.client.Do(req)
    
    if err != nil {
	    	return
    }

    defer resp.Body.Close()

    data, err := ioutil.ReadAll(resp.Body)
    
    if err != nil {
	    	return
    }

    res = &AccountActivateResponse{}
    json.Unmarshal(data, res)
    return
}

func (control *Control) LookupService(serviceName string)(services []Service, err error) {
    url := "https://" + control.host + ":8910/pxgrid/control/ServiceLookup"
    lookupRequest, _ := json.Marshal(ServiceLookupRequest{ serviceName })
    
    var req *http.Request
    
    req, err = http.NewRequest("POST", url, strings.NewReader(string(lookupRequest)))
    
    if err != nil {
            return
    }
    
    req.Header.Add("Content-Type", "application/json")
    req.Header.Add("Accept", "application/json")
    req.SetBasicAuth(control.user, "")
    resp, err := control.client.Do(req)
    
    if err != nil {
	    	return
    }

    defer resp.Body.Close()

    data, err := ioutil.ReadAll(resp.Body)
    
    if err != nil {
	    	return
    }

    res := &ServiceLookupResponse{}
    json.Unmarshal(data, res)
    services = res.Services
    return
}

func (control *Control) GetAccessSecret(peerNode string)(secret string, err error) {
    url := "https://" + control.host + ":8910/pxgrid/control/AccessSecret"
    accessRequest, _ := json.Marshal(AccessSecretRequest{ peerNode })
    
    var req *http.Request
    
    req, err = http.NewRequest("POST", url, strings.NewReader(string(accessRequest)))
    
    if err != nil {
            return
    }
    
    req.Header.Add("Content-Type", "application/json")
    req.Header.Add("Accept", "application/json")
    req.SetBasicAuth(control.user, "")
    resp, err := control.client.Do(req)
    
    if err != nil {
	    	return
    }

    defer resp.Body.Close()

    data, err := ioutil.ReadAll(resp.Body)
    
    if err != nil {
	    	return
    }

    res := &AccessSecretResponse{}
    json.Unmarshal(data, res)
    secret = res.Secret
    return
}
