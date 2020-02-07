import base64
import json
import urllib.request


class PxgridControl:
    def __init__(self, config):
        self.config = config

    def send_rest_request(self, url_suffix, payload):
        url = 'https://{}:{}/pxgrid/control/{}'.format(
            self.config.get_host_name()[0],
            self.config.get_port(),
            url_suffix)
        json_string = json.dumps(payload)
        handler = urllib.request.HTTPSHandler(context=self.config.get_ssl_context())
        opener = urllib.request.build_opener(handler)
        rest_request = urllib.request.Request(
            url=url, data=str.encode(json_string))
        rest_request.add_header('Content-Type', 'application/json')
        rest_request.add_header('Accept', 'application/json')
        b64 = base64.b64encode((self.config.get_node_name(
        ) + ':' + self.config.get_password()).encode()).decode()
        rest_request.add_header('Authorization', 'Basic ' + b64)
        rest_response = opener.open(rest_request)
        response = rest_response.read().decode()
        return json.loads(response)

    def account_activate(self):
        payload = {}
        if self.config.get_description() is not None:
            payload['description'] = self.config.get_description()
        return self.send_rest_request('AccountActivate', payload)

    def service_lookup(self, service_name):
        payload = {'name': service_name}
        return self.send_rest_request('ServiceLookup', payload)

    def service_register(self, service_name, properties):
        payload = {'name': service_name, 'properties': properties}
        return self.send_rest_request('ServiceRegister', payload)

    def get_access_secret(self, peer_node_name):
        payload = {'peerNodeName': peer_node_name}
        return self.send_rest_request('AccessSecret', payload)
