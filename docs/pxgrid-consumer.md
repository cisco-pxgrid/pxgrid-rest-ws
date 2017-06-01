# Consumer using pxGrid

pxGrid nodes connect to pxGrid Controller to perform control operations that facilitates communications between consumer nodes and provider nodes.

This guide discuss REST APIs use by consumer nodes.

# REST APIs

The REST APIs are all POST methods using JSON. 'Content-Type' and 'Accept' are 'application/json'


| Name | Description |
| ---- | ----------- |
| AccountActivation | Activate once at consumer startup |
| ServiceLookup | Get properties like URLs, topic... etc |
| AccessSecret | Get unique secret between 2 nodes |

---

### AccountActivate
```
https://<pxgrid-hostname>:8910/pxgrid/control/AccountActivate
```

Consumer must activate the account before proceeding to other APIs.
The 'accountState' in the response can be PENDING, DISABLED or ENABLED.
ISE UI will show the new account in PENDING state for administrator to approve.

For PENDING state, the recommended wait time between AccountActivate calls is at least 60 seconds.
For DISABLED state, the recommended wait time between AccountActivate calls is at least 5 minutes.

##### Request sample
```javascript
{
    "description":"MyApp 1.0"
}
```
##### Response sample
```javascript
{
    "accountState":"PENDING",
    "version":"2.0.0.2"
}
```

---
 
### ServiceLookup
```
https://<pxgrid-hostname>:8910/pxgrid/control/ServiceLookup
```

ServiceLookup returns a list of nodes providing the service.
It contains a set of properties specific to the service. The properties can be URLs, topic names...etc.
If service is not available, the ['services'] will be an empty array.

##### Request sample
```javascript
{
    "name":"com.cisco.ise.pubsub"
}
```
##### Response sample
```javascript
{
    "services":[
        {
            "name":"com.cisco.ise.pubsub",
            "nodeName":"ise-admin-pxgrid-002",
            "properties":
            {
                "wsUrl":"wss://pxgrid-002.cisco.com:8910/pxgrid/ise/pubsub"
            }
        }
    ]
}
```

---

### AccessSecret
```
https://<pxgrid-hostname>:8910/pxgrid/control/AccessSecret
```

AccessSecret is a unique secret between a Consumer and Provider pair.
The use of the secret is dictated by the implementation of the Provider.
In the case of ISE, the secret is used as the pasword of HTTP Basic Auth.

##### Request sample
```javascript
{
    "peerNodeName":"ise-admin-pxgrid-002"
}
```

##### Response sample
```javascript
{
    "secret":"oWhgNC7oNpaulpJ6"
}
```

---
# pxGrid account authentication
pxGrid account can be authenticated by password or certificate.

Password-based authentication requires AccountCreate first to obtain a password, and then perform AccountActivate using the credentials obtained.

Certificate-based authentication requires the use of a certificate that is trusted by ISE. Perform AccountActiviate using the client certificate, fill the Basic Auth header with username only.

Subsequence REST call will use Basic Auth header with "[username]:[password]" for password-based authentication, or "[username]:" for certificated-based authentication.


### AccountCreate
```
https://<pxgrid-hostname>:8910/pxgrid/control/AccountCreate
```

For password-based authentication only. AccountCreate feature is disabled by default and has to be enabled via ISE UI.

This REST call does not require authentication.

It creates an account for the node and returns a generated password for subsequence REST calls.

##### Request sample
```javascript
{
    "nodeName":"MyName01"
}
```
##### Response sample
```javascript
{
    "nodeName":"MyName01",
    "password":"P9nEaNX0cyA4DRBr"
}
```
