import base64
import json
import urllib.request
import logging

logger = logging.getLogger(__name__)


class PxgridControl:
    def __init__(self, config):
        self.config = config

    def send_rest_request(self, url_suffix, payload):
        logger.debug('send_rest_request %s', url_suffix)
        url = 'https://{}:{}/pxgrid/control/{}'.format(
            self.config.hostname[0],
            self.config.port,
            url_suffix)
        json_string = json.dumps(payload)
        handler = urllib.request.HTTPSHandler(context=self.config.ssl_context)
        opener = urllib.request.build_opener(handler)
        rest_request = urllib.request.Request(
            url=url, data=str.encode(json_string))
        rest_request.add_header('Content-Type', 'application/json')
        rest_request.add_header('Accept', 'application/json')
        username_password = '%s:%s' % (self.config.node_name, self.config.password)
        b64 = base64.b64encode(username_password.encode()).decode()
        rest_request.add_header('Authorization', 'Basic ' + b64)
        rest_response = opener.open(rest_request)
        response = rest_response.read().decode()
        return json.loads(response)

    def account_activate(self):
        logger.debug('account_activate')
        payload = {}
        if self.config.description is not None:
            payload['description'] = self.config.description
        return self.send_rest_request('AccountActivate', payload)

    def service_lookup(self, service_name):
        logger.debug('service_lookup %s', service_name)
        payload = {'name': service_name}
        return self.send_rest_request('ServiceLookup', payload)

    def service_register(self, service_name, properties):
        logger.debug('service_register %s', service_name)
        payload = {'name': service_name, 'properties': properties}
        return self.send_rest_request('ServiceRegister', payload)

    def get_access_secret(self, peer_node_name):
        logger.debug('get_access_secret %s', peer_node_name)
        payload = {'peerNodeName': peer_node_name}
        return self.send_rest_request('AccessSecret', payload)
