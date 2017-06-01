# Service: com.cisco.ise.config.trustsec
This provides information for TrustSec configuration.

# Service properties

| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/config/trustsec |
| wsPubsubService | | com.cisco.ise.pubsub |
| securityGroupTopic | | /topic/com.cisco.ise.config.trustsec.security.group |

---

# REST APIs

### POST [restBaseUrl]/getSecurityGroups
##### Request
    {      
      // Returns all if ID not specified
      "id": string (optional)
    }

##### Reponse
    {
      "securityGroups": [
        array of securityGroup object      
      ]
    }

---

### POST [restBaseUrl]/getSecurityGroupAcls
##### Request
    {
      // Returns all if ID not specified
      "id": string (optional)
    }

##### Reponse
    {
      "securityGroupAcls": [
        array of securityGroupAcl object      
      ]
    }

---

### POST [restBaseUrl]/getEgressPolicies
##### Request
    {
    }

##### Reponse
    {
      "egressPolicies": [
        array of egressPolicy object      
      ]
    }

---

### POST [restBaseUrl]/getEgressMatrices
##### Request
    {
    }

##### Reponse
    {
      "egressMatrices": [
        array of egressMatrix object      
      ]
    }

---

# WS STOMP

### securityGroupTopic
    {
      "operation": operation type,
      "securityGroup": securityGroup object      
    }

---

# Objects

### "operation" type
"operation" type one of the following strings:
- CREATE
- UPDATE
- DELETE

### "securityGroup" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id | string | |
| name | string | |
| description | string | |
| tag | integer | |

---

### "securityGroupAcl" object

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id | String | SGACL ID |
| name | String | |
| description | String | |
| ipVersion | String | IPV4 or IPV6 |
| acl | String | |
| generationId | String | |

---

### "egressPolicy" object
| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id | string | |
| name | string | |
| matrixId | string | Matrix this policy belongs to |
| status | string | ENABLE,MONITOR |
| description | string | |
| sourceSecurityGroupId | string | |
| destinationSecurityGroupId | string | |
| sgaclIds | array of strings | IDs of the SGACLs being used |

---

### "egressMatrix" object
| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| id | string | |
| name | string | |
| description | string | |
| monitorAll | boolean | |

---

##### Samples
    {
    	"securityGroups": [{
    		"id": "92adf9f0-8c01-11e6-996c-525400b48521",
    		"name": "Unknown",
    		"description": "Unknown Security Group",
    		"tag": 0
    	}, {
    		"id": "92bb1950-8c01-11e6-996c-525400b48521",
    		"name": "ANY",
    		"description": "Any Security Group",
    		"tag": 65535
    	}, {
    		"id": "934557f0-8c01-11e6-996c-525400b48521",
    		"name": "Auditors",
    		"description": "Auditor Security Group",
    		"tag": 9
    	}]
    }


    {
    	"securityGroupAcls": [{
    		"id": "92919850-8c01-11e6-996c-525400b48521",
    		"name": "Deny IP",
    		"description": "Deny IP SGACL",
    		"ipVersion": "IPV4",
    		"acl": "deny ip",
    		"generationId": "0"
    	}, {
    		"id": "92951ac0-8c01-11e6-996c-525400b48521",
    		"name": "Permit IP",
    		"description": "Permit IP SGACL",
    		"ipVersion": "IPV4",
    		"acl": "permit ip",
    		"generationId": "0"
    	}]
    }
    

    {
    	"egressPolicies": [{
    		"id": "92c1a900-8c01-11e6-996c-525400b48521",
    		"name": "ANY-ANY",
    		"description": "Default egress rule",
    		"status": "ENABLED",
    		"sourceSecurityGroupId": "92bb1950-8c01-11e6-996c-525400b48521",
    		"destinationSecurityGroupId": "92bb1950-8c01-11e6-996c-525400b48521",
    		"sgaclIds": ["92951ac0-8c01-11e6-996c-525400b48521"],
    		"matrixId": "9fa3a33a-329e-43cb-a4cf-7bd38df16e7b"
    	}, {
    		"id": "8edb3f11-373f-11e7-bc34-0242ae4776c4",
    		"name": "BYOD-Auditors",
    		"status": "ENABLED",
    		"sourceSecurityGroupId": "935d4cc0-8c01-11e6-996c-525400b48521",
    		"destinationSecurityGroupId": "934557f0-8c01-11e6-996c-525400b48521",
    		"sgaclIds": ["130b3d00-36df-11e7-bc34-0242ae4776c4"],
    		"matrixId": "9fa3a33a-329e-43cb-a4cf-7bd38df16e7b"
    	}, {
    		"id": "640add50-36df-11e7-bc34-0242ae4776c4",
    		"name": "Auditors-BYOD",
    		"description": "test1",
    		"status": "ENABLED",
    		"sourceSecurityGroupId": "934557f0-8c01-11e6-996c-525400b48521",
    		"destinationSecurityGroupId": "935d4cc0-8c01-11e6-996c-525400b48521",
    		"sgaclIds": ["92919850-8c01-11e6-996c-525400b48521"],
    		"matrixId": "9fa3a33a-329e-43cb-a4cf-7bd38df16e7b"
    	}]
    }


    {
       "egressMatrices": [{
          "id": "9fa3a33a-329e-43cb-a4cf-7bd38df16e7b",
          "name": "Production",
          "monitorAll": false
        }, {
          "id": "f58b05eb-04ab-4283-8b13-998eda207147",
          "name": "TestMatrix",
          "description": "Test Matrix only",
          "monitorAll": false
       }]
    }

