from pxgrid import PxgridControl
from config import Config
import urllib.request
import base64
import time
import json_stream


def query_and_stream_print(config, secret, url, payload):
    print('query url=' + url + ' payload=' + payload)
    handler = urllib.request.HTTPSHandler(context=config.get_ssl_context())
    opener = urllib.request.build_opener(handler)
    req = urllib.request.Request(url=url, data=str.encode(payload))
    req.add_header('Content-Type', 'application/json')
    req.add_header('Accept', 'application/json')
    credentials = f"{config.get_node_name()}:{secret}"
    b64 = base64.b64encode(credentials.encode('utf-8')).decode('utf-8')
    req.add_header('Authorization', 'Basic ' + b64)
    resp = opener.open(req)

    if resp.status == 200:
        session_count = 0
        data = json_stream.load(resp)
        for session in data['sessions'].persistent():
            session_count += 1
            print(f'Session {session_count}: {dict(session)}')
        print(f'Total sessions processed: {session_count}')
    else:
        print('Error response status=' + str(resp.status))


if __name__ == '__main__':
    config = Config()
    pxgrid = PxgridControl(config=config)

    while pxgrid.account_activate()['accountState'] != 'ENABLED':
        time.sleep(60)

    # lookup for session service
    service_lookup_response = pxgrid.service_lookup('com.cisco.ise.session')
    service = service_lookup_response['services'][0]
    node_name = service['nodeName']
    url = service['properties']['restBaseUrl'] + '/getSessions'

    secret = pxgrid.get_access_secret(node_name)['secret']

    if config.get_filter() is not None:
        query_and_stream_print(config, secret, url,
                               '{"filter": "' + config.get_filter() + '"}')
    else:
        query_and_stream_print(config, secret, url, '{}')
