import os
import io
import ssl
import json
import time
import pprint
import asyncio
import aiohttp
import configparser
import base64
import urllib.request
import urllib.error


def load_properties_file():
    print('Loading config values from properties.ini file...')
    config = configparser.ConfigParser()
    config.read(os.path.dirname(os.path.realpath(__file__)) + '/properties.ini')
    pprint.pprint(config.items('SAMPLE_PROPERTIES'))
    print('Done config loading values from properties.ini file!\n')
    return config.get('SAMPLE_PROPERTIES', 'pxgrid_servers').split(','),\
        config.get('SAMPLE_PROPERTIES', 'client_username'),\
        config.get('SAMPLE_PROPERTIES', 'client_groups').split(','),\
        config.get('SAMPLE_PROPERTIES', 'client_description'),\
        config.get('SAMPLE_PROPERTIES', 'client_ca_trust_folderpath'),\
        config.get('SAMPLE_PROPERTIES', 'client_cert_pem_filepath'),\
        config.get('SAMPLE_PROPERTIES', 'client_cert_key_filepath'),\
        config.get('SAMPLE_PROPERTIES', 'client_cert_password')


def create_ssl_context(cert_file, key_file, password, ca_truststore):
    print('Creating SSL Context... ')
    context = ssl.create_default_context(purpose=ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain(certfile=cert_file, keyfile=key_file, password=password)
    context.load_verify_locations(capath=ca_truststore)
    print('Done creating SSL Context!\n')
    return context


def send_rest_request(hostname, url_suffix, context, username, payload, content_type='json', access_secret=''):
    address = url_suffix
    if 'https' not in address:
        address = 'https://' + hostname + ':8910/pxgrid/control/' + url_suffix
    print(address)
    b64 = base64.b64encode((username + ':' + access_secret).encode()).decode()
    data = None
    if payload is not None:
        data = str.encode(payload)
    rest_request = urllib.request.Request(url=address, data=data)
    rest_request.add_header('Content-Type', 'application/' + content_type)
    rest_request.add_header('Accept', 'application/' + content_type)
    rest_request.add_header('Authorization', 'Basic ' + b64)
    try:
        rest_response = urllib.request.urlopen(rest_request, context=context)
        return rest_response.read().decode()
    except urllib.error.HTTPError as err:
        print(err)


def perform_account_activate(hostname, context, username, description, groups):
    print('Activating client account...')
    account_state = ''
    json_response = None
    while 'ENABLED' not in account_state:
        payload = json.dumps({'description': description, 'groups': groups})
        raw_response = send_rest_request(hostname, 'AccountActivate', context, username, payload)
        json_response = json.loads(raw_response)
        account_state = json_response['accountState']
        if 'PENDING' in account_state:
            print('Server approval is still pending. Waiting 10 sec before retrying...')
            time.sleep(10)
    print('Client account has been activated! Server pxGrid version: ' + json_response['version'] + '\n')


def perform_service_lookup(hostname, context, service_name, username):
    print('Performing service look up to retrieve Websocket URL...')
    payload = json.dumps({'name': service_name})
    raw_response = send_rest_request(hostname, 'ServiceLookup', context, username, payload)
    json_response = json.loads(raw_response)
    print('Response: ' + json.dumps(json_response) + '\n')
    return json_response


def perform_ws_service_lookup(hostname, context, service_name, username):
    json_response = perform_service_lookup(hostname, context, service_name, username)
    service = json_response['services'][0]  # should be cycling through services and choosing
    return service['nodeName'], service['properties']['WSURL']


def perform_access_secret_retrieval(hostname, context, username, node_name):
    print('Retrieving Access Secret...')
    payload = json.dumps({'peerNodeName': node_name})
    raw_response = send_rest_request(hostname, 'AccessSecret', context, username, payload)
    response = json.loads(raw_response)
    print('Received secret: ' + response['secret'] + '\n')
    return response['secret']


async def perform_ws_subscribe(ws_url, topic_list, context, username, access_secret):
    print('Performing Subscribe over Websocket...')
    ssl_connector = aiohttp.TCPConnector(ssl_context=context)
    main_loop = asyncio.get_event_loop()
    async with aiohttp.ClientSession(loop=main_loop, connector=ssl_connector) as client:
        b64 = base64.b64encode((username + ':' + access_secret).encode()).decode()
        async with client.ws_connect(ws_url, headers={'Authorization': 'Basic ' + b64}) as ws:
            print('Sending subscribe STOMP frame...')
            for t in topic_list:
                subscribe_frame = StompFrame()
                subscribe_frame.set_command('SUBSCRIBE')
                subscribe_frame.set_header('destination', t)
                subscribe_frame.set_header('id', 'my-id')  # should be using a better id
                out_stream = io.StringIO()
                subscribe_frame.write(out_stream)
                ws.send_bytes(out_stream.getvalue().encode())
            print('Subscribed and ready to receive notifications!')

            print('Listening... (CTRL-C to quit)')
            while True:
                message = await ws.receive()
                in_stream = io.StringIO(message.data.decode())
                message_frame = StompFrame()
                message_frame.parse(in_stream)
                print('Received notification: ' + json.dumps(json.loads(message_frame.content[:-1])))


def handle_shutdown():
    print('Closing websocket connection...')
    for task in asyncio.Task.all_tasks():
        task.cancel()


class StompFrame:
    __author__ = 'alei'

    def __init__(self):
        self.headers = {}
        self.content = None
        self.command = None

    def set_command(self, command):
        self.command = command

    def set_content(self, content):
        self.content = content

    def set_header(self, key, value):
        self.headers[key] = value

    def write(self, out):
        out.write(self.command)
        out.write('\n')
        for key in self.headers:
            out.write(key)
            out.write(':')
            out.write(self.headers[key])
            out.write('\n')
        out.write('\n')
        if self.content is not None:
            out.write(self.content)
        out.write('\0')

    def parse(self, in_stream):
        self.command = in_stream.readline().rstrip("\r\n")
        for line in in_stream:
            line = line.rstrip("\r\n")
            if line in "":
                break
            (name, value) = line.split(":")
            self.headers[name] = value
        self.content = in_stream.read()

    def __repr__(self):
        from pprint import pformat
        return pformat(vars(self), indent=4)

