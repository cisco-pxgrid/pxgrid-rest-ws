# Service: com.cisco.ise.system
This is ISE System Health service.

# Service properties
| Name          | Description   | Example       |
| ------------- | ------------- | ------------- | 
| restBaseUrl | | https://ise-host1:8910/pxgrid/ise/system |

---
# REST APIs

### POST [restBaseUrl]/getHealths

##### Request
    {
      // All nodes if not present
      "nodeName": string (optional),
      // Last 1 hour if not present
      "startTime": Datetime (optional)
    }

##### Reponse
    {
      "healths": [
        array of syshealth objects
      ]
    }

---

### POST [restBaseUrl]/getPerformances

##### Request
    {
      // All nodes if not present
      "nodeName": string (optional),
      // Last 1 hour if not present
      "startTime": Datetime (optional)
    }
##### Reponse
    {
      "performances": [
        array of performance objects
      ]
    }

---

# Objects

### "health" object

TODO To be completed...

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| timestamp     | Datetime      | The time this record was created in ISE. |
| serverName    | string | ISE server name where data is recorded |
| ioWait | number | Percentage of I/O wait for the last 5 minute? |
| cpuUsage | number | Percentage of CPU usage for the last 5 minute? |
| memoryUsage | number | Percentage of total memory usage |
| diskUsageRoot | number | Percentage of disk space usage of root directory |
| diskUsageOpt | number | Percentage of disk space usage of opt directory |
| loadAverage | number | The average number of jobs in the run queue for the last minute |
| networkSent | number | Bytes received since last?? |
| networkReceived | number | Bytes received since last?? |


### "performance" object

TODO need duration and units!!

| Name          | Type          | Description   |
| ------------- | ------------- | ------------- |
| timestamp     | Datetime      | The time this record was created in ISE. |
| serverName | string | ISE server name where data is recorded |
| radiusRate | number | Radius transaction per second. e.g. 0.02 |
| radiusCount | number | Radius requests count. "59". Should this be number of transactions? what is the duration??? |
| radiusLatency | number | Average latency per request. of duration? Unit? e.g. 2.44 |


##### Sample

    "health" object
    {  
       "healths":[  
          {  
             "timestamp":"2017-05-10T15:21:14.294-07:00",
             "serverName":"pxgrid-001",
             "ioWait":0.3,
             "cpuUsage":41.93,
             "memoryUsage":71.72,
             "diskUsageRoot":14.0,
             "diskUsageOpt":18.0,
             "loadAverage":2.1,
             "networkSent":2587,
             "networkReceived":93292
          },
          {  
             "timestamp":"2017-05-10T15:26:34.375-07:00",
             "serverName":"pxgrid-001",
             "ioWait":0.3,
             "cpuUsage":10.5,
             "memoryUsage":71.75,
             "diskUsageRoot":14.0,
             "diskUsageOpt":18.0,
             "loadAverage":1.3,
             "networkSent":5417854,
             "networkReceived":622182
          },
          {  
             "timestamp":"2017-05-10T15:31:53.816-07:00",
             "serverName":"pxgrid-001",
             "ioWait":0.3,
             "cpuUsage":7.12,
             "memoryUsage":71.92,
             "diskUsageRoot":14.0,
             "diskUsageOpt":18.0,
             "loadAverage":0.6,
             "networkSent":2511451,
             "networkReceived":580965
          },
          {  
             "timestamp":"2017-05-10T15:43:52.564-07:00",
             "serverName":"pxgrid-001",
             "ioWait":0.3,
             "cpuUsage":42.15,
             "memoryUsage":72.11,
             "diskUsageRoot":14.0,
             "diskUsageOpt":18.0,
             "loadAverage":2.2,
             "networkSent":849611,
             "networkReceived":174900
          }
       ]
    }

    "performance" object
    {  
       "performances":[  
          {  
             "timestamp":"2017-05-10T18:06:15.011336-07:00",
             "serverName":"pxgrid-001",
             "radiusRate":0.0,
             "radiusCount":0,
             "radiusLatency":0.0
          }
       ]
    }
