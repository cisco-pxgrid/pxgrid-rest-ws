import asyncio
from asyncio.tasks import FIRST_COMPLETED
from pxgrid import PxgridControl
from config import Config
import json
import sys
import time
import logging
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp


logger = logging.getLogger(__name__)


def key_enter_callback(event):
    sys.stdin.readline()
    event.set()

async def future_read_message(ws, future):
    try:
        message = await ws.stomp_read_message()
        future.set_result(message)
    except ConnectionClosed:
        logger.debug('Websocket connection closed')

async def subscribe_loop(config, secret, ws_url, topic):
    ws = WebSocketStomp(ws_url, config.node_name, secret, config.ssl_context)
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic)
    # setup keyboard callback
    # stop_event = asyncio.Event()
    # asyncio.get_event_loop().add_reader(sys.stdin, key_enter_callback, stop_event)
    # logger.debug(w"press <enter> to disconnect...")
    while True:
        # future = asyncio.Future()
        # future_read = future_read_message(ws, future)
        # await asyncio.wait([stop_event.wait(), future_read], return_when=FIRST_COMPLETED)
        # if not stop_event.is_set():
        message = json.loads(await ws.stomp_read_message())
        # message = json.loads(future.result())
        print(json.dumps(message, indent=2, sort_keys=True), file=sys.stdout)
        sys.stdout.flush()
        # else:
        #     await ws.stomp_disconnect('123')
        #     # wait for receipt
        #     await asyncio.sleep(3)
        #     await ws.disconnect()
        #     break
        

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
    pubsub_service_name = service['properties']['wsPubsubService']
    topic = service['properties']['securityGroupAclTopic']

    # lookup for trustsec object changes
    # service_lookup_response = pxgrid.service_lookup('com.cisco.ise.config.trustsec')
    # service = service_lookup_response['services'][0]
    # pubsub_service_name = service['properties']['wsPubsubService']
    # topic = service['properties']['securityGroupTopic']

    # lookup for pubsub service
    service_lookup_response = pxgrid.service_lookup(pubsub_service_name)
    pubsub_service = service_lookup_response['services'][0]
    pubsub_node_name = pubsub_service['nodeName']
    secret = pxgrid.get_access_secret(pubsub_node_name)['secret']
    ws_url = pubsub_service['properties']['wsUrl']

    ws_url = ws_url.replace('8910', str(config.port))
    
    asyncio.get_event_loop().run_until_complete(subscribe_loop(config, secret, ws_url, topic))
