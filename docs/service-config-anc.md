# Service: com.cisco.ise.config.anc
This is Adaptive Network Control configuration service

# Service properties
| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/config/anc |
| wsPubsubService | | com.cisco.ise.pubsub |
| topic | | /topic/com.cisco.ise.config.anc |

---

# REST APIs

### POST [restBaseUrl]/applyEndpointPolicyByIP
##### Request
    {
      "policyName": string (required),
      "ipAddress": string (required)
    }

##### Reponse
    {
      "status": ??
    }

---

# WS STOMP messaging

### topic
    {
      "status": ?? 
    }

---

# Objects

### "policy" object

### "endpointPolicy" object


