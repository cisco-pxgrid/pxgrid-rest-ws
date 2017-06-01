# Service: com.cisco.ise.sxp
This is ISE SXP service.

# Service properties
| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/sxp |
| wsPubsubService | | com.cisco.ise.pubsub |
| bindingTopic | | /topic/com.cisco.ise.sxp.binding |
---
# REST APIs

### POST [restBaseUrl]/getBindings

##### Request
    {
    }

##### Reponse
    {
      "bindings": [
        array of binding objects
      ]
    }


# WS STOMP messaging

### bindingTopic
    {
      "operation": operation type,
      "binding": binding object
    }

---

# Objects

### "operation" type
"operation" type one of the following strings:
- CREATE
- UPDATE
- DELETE


### "binding" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| tag | String | |
| ipPrefix | String | |
| source | String | |
| peerSequence | String | |



##### Samples
    {
    	"bindings": [{
    		"ipPrefix": "2.2.2.2/32",
    		"tag": 15,
    		"source": "172.21.170.196",
    		"peerSequence": "172.21.170.196"
    	}]
    }

    {
    	"operation": "CREATE",
    	"binding": {
    		"ipPrefix": "3.3.3.3/32",
    		"tag": 8,
    		"source": "172.21.170.196",
    		"peerSequence": "172.21.170.196"
    	}
    }
