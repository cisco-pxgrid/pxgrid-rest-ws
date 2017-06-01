# Service: com.cisco.ise.config.profiler
This is ISE Profiler configuration

# Service properties
| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/config/profiler |
| wsPubsubService | | com.cisco.ise.pubsub |
| topic | | /topic/com.cisco.ise.config.profiler |

---

# REST APIs

### POST [restBaseUrl]/config/getProfiles
##### Request
    {      
    }

##### Reponse
    {
      "profiles": [
        array of profile object      
      ]
    }

---

# WS STOMP

### profileTopic
    {
      "operation": operation type,
      "profile": profile object
    }


---

# Objects

### "operation" type
"operation" type one of the following strings:
- CREATE
- UPDATE
- DELETE

### "profile" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id | String | |
| name | String | |
| fullName | String | |

##### samples
    {  
       "profiles":[  
          {  
             "id":"fe8c7cc0-8bff-11e6-996c-525400b48521",
             "name":"2Wire-Device",
             "fullName":"2Wire-Device"
          },
          {  
             "id":"fed21140-8bff-11e6-996c-525400b48521",
             "name":"3Com-Device",
             "fullName":"3Com-Device"
          }
        ]
    }
