import asyncio
import json
import signal
import sys
import time
import logging
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp
from config import Config
from pxgrid import PxgridControl


logger = logging.getLogger(__name__)


async def subscribe_loop(config, secret, ws_url, topic):
    '''
    Simple subscription loop just to display whatever events arrive.
    '''
    logger.debug('starting subscription to %s at %s', topic, ws_url)
    ws = WebSocketStomp(ws_url, config.node_name, secret, config.ssl_context)
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic)
    try:
        while True:
            message = json.loads(await ws.stomp_read_message())
            print(json.dumps(message, indent=2, sort_keys=True), file=sys.stdout)
            sys.stdout.flush()
    except asyncio.CancelledError as e:
        pass
    logger.debug('shutting down listener...')
    await ws.stomp_disconnect('123')
    await asyncio.sleep(2.0)
    await ws.disconnect()
        

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
    service_lookup_response = pxgrid.service_lookup('com.cisco.ise.session')
    service = service_lookup_response['services'][0]
    pubsub_service_name = service['properties']['wsPubsubService']
    topic = service['properties']['sessionTopic']

    # lookup for pubsub service
    service_lookup_response = pxgrid.service_lookup(pubsub_service_name)
    pubsub_service = service_lookup_response['services'][0]
    pubsub_node_name = pubsub_service['nodeName']
    secret = pxgrid.get_access_secret(pubsub_node_name)['secret']
    ws_url = pubsub_service['properties']['wsUrl']

    # setup main loop, including ctrl-c to cancel handling    
    loop = asyncio.get_event_loop()
    main_task = asyncio.ensure_future(subscribe_loop(config, secret, ws_url, topic))
    loop.add_signal_handler(signal.SIGINT, main_task.cancel)
    loop.add_signal_handler(signal.SIGTERM, main_task.cancel)
    try:
        loop.run_until_complete(main_task)
    except:
        pass
