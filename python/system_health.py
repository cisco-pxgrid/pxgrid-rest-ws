# 3rd-party modules
import urllib.request
import base64
import time
import json
from datetime import datetime, timezone, timedelta

# project specific modules
from pxgrid import PxgridControl
from config import Config


def query(config, secret, url, payload):
    print('query url=' + url)
    print('  request=' + payload)
    handler = urllib.request.HTTPSHandler(context=config.get_ssl_context())
    opener = urllib.request.build_opener(handler)
    rest_request = urllib.request.Request(url=url, data=str.encode(payload))
    rest_request.add_header('Content-Type', 'application/json')
    rest_request.add_header('Accept', 'application/json')
    b64 = base64.b64encode((config.get_node_name() + ':' + secret).encode()).decode()
    rest_request.add_header('Authorization', 'Basic ' + b64)
    rest_response = opener.open(rest_request)
    response = json.loads(rest_response.read().decode())
    print(f'  response status={str(rest_response.getcode())}')
    print(f'  response content={json.dumps(response, sort_keys=True, indent=2)}')

if __name__ == '__main__':
    config = Config()
    pxgrid = PxgridControl(config=config)

    while pxgrid.account_activate()['accountState'] != 'ENABLED':
        time.sleep(60)

    # lookup for session service
    service_lookup_response = pxgrid.service_lookup('com.cisco.ise.system')
    service = service_lookup_response['services'][0]
    node_name = service['nodeName']
    url = service['properties']['restBaseUrl'] + '/getHealths'

    secret = pxgrid.get_access_secret(node_name)['secret']

    delta = datetime.utcnow().replace(tzinfo=timezone.utc).astimezone() - timedelta(minutes=5)

    #query(config, secret, url, '{}')
    query(config, secret, url, f'{{"startTimestamp": "{delta.isoformat()}"}}')
    #query(config, secret, url, f'{{"startTimestamp": "2020-04-10T22:03:14.841678+02:00"}}')
