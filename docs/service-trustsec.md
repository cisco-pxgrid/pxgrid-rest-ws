# Service: com.cisco.ise.trustsec
This is ISE TrustSec service. Currently, it provides the status of SGACL downloads.

# Service properties
| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| wsPubsubService | | com.cisco.ise.pubsub |
| policyDownloadTopic | | /topic/com.cisco.ise.trustsec.policy.download |

---

# WS STOMP messaging

### policyDownloadTopic
    {
      "policyDownloads":
      [
        array of policyDownload objects
      ]
    }


---
# Objects

### "policyDownload" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| timestamp     | Datetime      | The time this record was created in ISE. |
| serverName    | string | ISE server name where data is recorded |
| status | string | SUCCESS or FAILURE |
| failureReason | string | |
| nasIpAddress  | string | |
| matrixName | string | |
| rbaclSourceList  | string | |
| policies | Array of "policy" object | |


### "policy" object
| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| sourceSgt | integer | |
| sourceSgtGenerationId | string | |
| destinationSgt | integer | |
| destinationSgtGenerationId | string | |
| sgaclName | string | |
| sgaclGenerationId | string | |


##### Samples
    {
    	"policyDownloads": [{
    		"timestamp": "2017-05-30T14:25:06.653-07:00",
    		"serverName": "pxgrid-002",
    		"status": "SUCCESS",
    		"nasIpAddress": "192.168.113.3",
    		"rbaclSourceList": "000F-08",
    		"matrixName": "Production",
    		"policies": [{
    			"sourceSgt": 5,
    			"destinationSgt": 15,
    			"destinationSgtGenerationId": "09",
    			"sgaclName": "bhargavsgacl",
    			"sgaclGenerationId": "1"
    		}, {
    			"sourceSgt": 15,
    			"destinationSgt": 15,
    			"destinationSgtGenerationId": "09",
    			"sgaclName": "bhargavsgacl",
    			"sgaclGenerationId": "1"
    		}]
    	}]
    }
