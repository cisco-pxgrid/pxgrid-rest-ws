# Service: com.cisco.ise.session
This service provides access to ISE Session Directory.
There are 2 objects that can be accessed:
  - Session
  - UserGroup

UserGroup is being separated out from Session object because of its size and static nature. 

# Service properties

| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/session |
| wsPubsubService | | com.cisco.ise.pubsub |
| sessionTopic | | /topic/com.cisco.ise.session |
| userGroupTopic | | /topic/com.cisco.ise.session.group |

---

# REST APIs

### POST [restBaseUrl]/getSessions

##### Request

```javascript
    {
      "startTimestamp": ISO8601 Datetime (optional)
    }
```

##### Reponse

```javascript
    {
      "sessions": [ 
        array of session objects
      ]
    }
```

---

### POST [restBaseUrl]/getSessionByIpAddress

##### Request

    {
      "ipAddress": string (required)
    }

##### Reponse

    {
      session object
    }

---

### POST [restBaseUrl]/getSessionByMacAddress

##### Request

    {
      "macAddress": string (required)
    }

##### Reponse

    {
      session object
    }

---

### POST [restBaseUrl]/getUserGroups

##### Request

    {
    }

##### Reponse

    {
      "userGroups": [
        array of userGroup objects
      ]
    }

---

### POST [restBaseUrl]/getUserGroupByUserName

##### Request

    {
      "userName": string (required)
    }

##### Reponse

    {
      "groups": [ 
        array of group objects
      ]
    }

---

# WS STOMP messaging

### sessionTopic

    {
      "sessions": [ 
        array of session objects
      ]
    }

### userGroupTopic

    {
      "userGroups": [
        array of userGroup objects
      ]
    }

---

# Objects

### "session" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| timestamp     | Datetime      | The time that the session record was created or updated in ISE. |
| state         | String        | <ul> <li>"AUTHENTICATING"</li><li>"AUTHENTICATED"</li><li>"POSTURED"</li><li>"STARTED"</li><li>"DISCONNECTED": Terminated session</li></ul> |
| macAddress              | String | MAC address in uppercase colon separated format XX:XX:XX:XX:XX:XX |
| ipAddresses | Array of strings | IPv4 or IPv6 addresses |
| callingStationId | String | |
| calledStationId | String | |
| auditSessionId          | String      | This is Audit Session ID generated uniquely by switch/router for a given session. |
| userName                | String      | |
| nasIpAddress            | String      | IPv4 or IPv6 address |
| nasPortId | String | |
| nasPortType | String | |
| nasIdentifier | String | |
| postureStatus | String | Posture status of the endpoint. Such as running, complete...etc |
| endpointProfile | String | |
| endpointOperatingSystem | String | |
| ctsSecurityGroup | String | This is the Trustsec security group name |
| adNormalizedUser | String | |
| adUserDomainName | String | |
| adHostDomainName | String | |
| adUserNetBiosName | String | |
| adHostNetBiosName | String | |
| adUserResolvedIdentities | String | |
| adUserResolvedDns | String | |
| adHostResolvedIdentities | String | |
| adHostResolvedDns | String | |
| providers | Arrays of string | Providers of this session information: <ul><li>WMI</li><li>Agent</li><li> Syslog</li><li>Rest</li><li>Span</li><li>DHCP</li><li>EndPoint</li> |
| endpointCheckResult | String | |
| endpointCheckTime | Datetime | |
| identitySourcePortStart | String | Start of source port range of the virtual desktop environment |
| identitySourcePortEnd | String | End of source port range of the virtual desktop environment |
| identitySourcePortFirst | String | First source port of the virtual desktop environment |
| terminalServerAgentId | String | Terminal Server Agent ID |
| isMachineAuthentication | String | Determine if this is a machine or not: <ul><li>true for a machine</li><li>false for not a machine</li><li>if not present for unknown type</li></ul> |
| serviceType | String | |
| tunnelPrivateGroupId | String | |
| airespaceWlanId | String | |
| networkDeviceProfileName | String | |
| radiusFlowType | String | |
| ssid | String | |


### "userGroup" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| userName | String | |
| groups | array of group objects | |


### "group" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| name    | String | |
| type    | String      | Type of group: <ul><li>ACTIVE_DIRECTORY</li><li>IDENTITY</li><li>EXTERNAL</li><li>INTERESTING_ACTIVE_DIRECTORY</li></ul> |

##### Samples

    "session" object
    {  
       "updateTime":"2017-04-28T07:33:34.291-07:00",
       "state":"STARTED",
       "userName":"user1",
       "callingStationId":"00:11:22:33:44:55",
       "auditSessionId":"101",
       "ipAddresses":[  
          "1.2.3.4"
       ],
       "macAddress":"00:11:22:33:44:55",
       "nasIpAddress":"172.21.170.242",
       "endpointProfile":"FreeBSD-Workstation",
       "endpointOperatingSystem":"FreeBSD 7.1-RELEASE - 9.0-CURRENT (accuracy 98%)",
       "adNormalizedUser":"user1",
       "providers":[  
          "None"
       ],
       "endpointCheckResult":"none",
       "identitySourcePortStart":0,
       "identitySourcePortEnd":0,
       "identitySourcePortFirst":0,
       "networkDeviceProfileName":"Cisco"
    }

    "userGroup" object
    {  
       "userName":"user1",
       "groups":[  
          {  
             "name":"User Identity Groups:Employee",
             "type":"IDENTITY"
          },
          {  
             "name":"Workstation",
             "type":"IDENTITY"
          }
       ]
    }

