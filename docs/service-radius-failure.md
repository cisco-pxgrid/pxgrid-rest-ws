# Service: com.cisco.ise.radius
This service provides information about Radius Failures.

# Service properties

| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/radius |
| wsPubsubService | | com.cisco.ise.pubsub |
| failureTopic | | /topic/com.cisco.ise.radius.failure |

---

# REST APIs

### POST [restBaseUrl]/getFailures
##### Request
    {
      // Last one hour if not specified
      "startTimestamp": ISO8601 Datetime (optional)
    }

##### Reponse
    {
      "failures": [ 
        array of failure objects
      ]
    }

---

### POST [restBaseUrl]/getFailureById
##### Request
    {
      "id": ID of the entry (required)
    }

##### Reponse
    {
      failure objects
    }

---

# WS STOMP messaging

### failureTopic
    {
      "failures": [ 
        array of failure objects
      ]
    }

---

# Objects

### "failure" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id            | string | |
| timestamp     | Datetime      | The time this record was created in ISE. |
| failureReason | string        | |
| userName      | string        | |
| serverName    | string | ISE server name where failure occured |
| authenticationProtocol | string | |
| deviceType    | string | |
| location      | string | |
| callingStationId | string | |
| auditSessionId | string | |
| nasIpAddress  | string | |
| nasPortId | string | |
| nasPortType | string | |
| ipAddresses   | array of string | |
| macAddress | string | |
| messageCode | integer | |
| destinationIpAddress | string | |
| userType | string | |
| accessService | string | |
| identityStore | string | |
| identityGroup | string | |
| authenticationMethod | string | |
| authenticationProtocol | string | |
| serviceType | string | |
| networkDeviceName | string | |
| deviceType | string | |
| location | string | |
| selectedAznProfiles | string | |
| postureStatus | string | |
| ctsSecurityGroup | string | |
| response | string | |
| responseTime | number | |
| executionSteps | string | |
| credentialCheck | string | |
| endpointMatchedProfile | string | |
| mdmServerName | string | |
| policySetName | string | |
| authorizationRule | string | |
| mseResponseTime | time | |
| mseServerName | string | |
| originalCallingStationId | string | |


##### Samples

    "failure" objects
    {  
       "failures":[  
          {  
             "id":"1494300801107032",
             "timestamp":"2017-05-08T20:56:34.379-07:00",
             "failureReason":"22040 Wrong password or invalid shared secret",
             "messageCode":5400,
             "userName":"user1",
             "serverName":"pxgrid-001",
             "auditSessionId":"101",
             "ipAddresses":[  
                "1.2.3.4"
             ],
             "nasIpAddress":"172.21.170.242",
             "nasName":"DefaultNetworkDevice",
             "callingStationId":"00:11:22:33:44:55",
             "originalCallingStationId":"00:11:22:33:44:55",
             "userType":"User",
             "accessService":"Default Network Access",
             "identityStore":"Internal Users",
             "authenticationMethod":"PAP_ASCII",
             "authenticationProtocol":"PAP_ASCII",
             "deviceType":"All Device Types",
             "location":"All Locations",
             "response":"{RadiusPacketType\u003dAccessReject; AuthenticationResult\u003dFailed; }",
             "responseTime":325,
             "executionSteps":[  
                "11001",
                "11017",
                "11049",
                "15049",
                "15008",
                "15041",
                "15048",
                "22072",
                "15013",
                "24210",
                "24212",
                "22040",
                "22057",
                "22061",
                "11003"
             ],
             "credentialCheck":"PAP_ASCII",
             "policySetName":"Default",
             "mseResponseTime":0
          }
       ]
    }

