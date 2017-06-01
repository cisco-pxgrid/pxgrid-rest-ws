# Cisco ISE services with pxGrid support

Cisco ISE provides services via REST and WebSocket with pxGrid support:
- The REST APIs use POST method with JSON request and response.
- WebSocket is used as a Pubsub messaging system, where [STOMP](https://stomp.github.io/) is being used as the messaging protocol.
- "Datetime" fields are ISO 8601 format
- HTTP status code 204 signifies item not found

The followings are reference guides for each service:
- [Session Directory](service-session)
- [System Health](service-system-health)
- [Radius Failure](service-radius-failure)
- [TrustSec](service-trustsec)
- [TrustSec SXP](service-sxp)
- [TrustSec configuration](service-config-trustsec)
- [Profiler configuration](service-config-profiler)

---

### Using REST and WS 

The followings are examples to demostrate how to use pxGrid to configure REST and WS, which applies to all services.

Refer to [pxGrid consumer guide](pxgrid-consumer) for information regarding `ServiceLookup` and `AccessSecret` APIs.

##### Session Directory getSessionByIpAddress
```
Request URL: [restBaseUrl]/getSessionByIpAddress
Request Method: POST
Content-Type: application/json
Accept: application/json
Authorization: Basic [nodeName]:[secret]
```

| Label | Description |
| ----- | ----------- |
| `[restBaseUrl]` | Obtain by `ServiceLookup` of `com.cisco.ise.session` |
| `[nodeName]` | pxGrid node name |
| `[secret]` | Obtain via `AccessSecret` |

---

##### Session Directory subscription to session topic
```
WS URL: [wsUrl]
Authorization: Basic [nodeName]:[secret]
STOMP: SUBSCRIBE [sessionTopic]
```

| Label | Description |
| ----- | ----------- |
| `[wsPubsubService]` and `[sessionTopic]` | Obtain by `ServiceLookup` of `com.cisco.ise.session` |
| `[wsUrl]` | Obtain by `ServiceLookup` of `[wsPubsubService]` |
| `[nodeName]` | pxGrid node name |
| `[secret]` | Obtain via `AccessSecret` |
