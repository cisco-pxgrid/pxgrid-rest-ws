from pxgrid import PxgridControl
from config import Config
import urllib.request
import base64
import time
import logging
import json

logger = logging.getLogger(__name__)


def query(config, secret, url, payload):
    handler = urllib.request.HTTPSHandler(context=config.ssl_context)
    opener = urllib.request.build_opener(handler)
    rest_request = urllib.request.Request(url=url, data=str.encode(payload))
    rest_request.add_header('Content-Type', 'application/json')
    rest_request.add_header('Accept', 'application/json')
    b64 = base64.b64encode((config.node_name + ':' + secret).encode()).decode()
    rest_request.add_header('Authorization', 'Basic ' + b64)
    rest_response = opener.open(rest_request)
    return rest_response.read().decode()


if __name__ == '__main__':
    config = Config()

    #
    # verbose logging if configured
    #
    if config.verbose:
        handler = logging.StreamHandler()
        handler.setFormatter(logging.Formatter('%(asctime)s:%(name)s:%(levelname)s:%(message)s'))
        logger.addHandler(handler)
        logger.setLevel(logging.DEBUG)

        # and set for stomp and ws_stomp modules also
        for stomp_mod in ['stomp', 'ws_stomp', 'pxgrid']:
            s_logger = logging.getLogger(stomp_mod)
            handler.setFormatter(logging.Formatter('%(asctime)s:%(name)s:%(levelname)s:%(message)s'))
            s_logger.addHandler(handler)
            s_logger.setLevel(logging.DEBUG)

    pxgrid = PxgridControl(config=config)

    while pxgrid.account_activate()['accountState'] != 'ENABLED':
        time.sleep(60)

    # lookup for session service
    service_lookup_response = pxgrid.service_lookup('com.cisco.ise.config.trustsec')
    service = service_lookup_response['services'][0]
    node_name = service['nodeName']
    url = service['properties']['restBaseUrl'] + '/getSecurityGroupAcls'

    # force port
    # url = url.replace('8910', str(config.port))

    # log url to see what we get via discovery
    logger.info('Using URL %s', url)

    secret = pxgrid.get_access_secret(node_name)['secret']
    logger.info('Using access secret %s', secret)
    resp = query(config, secret, url, '{}')
    print(json.dumps(json.loads(resp), indent=2, sort_keys=True))

