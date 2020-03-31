import asyncio
from asyncio.tasks import FIRST_COMPLETED
from asyncio.futures import CancelledError
from pxgrid import PxgridControl
from config import Config
import json
import sys
import time
import logging
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp
from signal import SIGINT, SIGTERM


logger = logging.getLogger(__name__)

#
# definitions of service names possible when this script was was written
# or updated
#
SERVICE_NAMES = [
    "com.cisco.ise.mdm",
    "com.cisco.ise.trustsec",
    "com.cisco.ise.config.trustsec",
    "com.cisco.ise.session",
    "com.cisco.ise.config.anc",
    "com.cisco.endpoint.asset",
    "com.cisco.ise.radius",
    "com.cisco.ise.system",
    "com.cisco.ise.sxp",
    "com.cisco.ise.config.profiler",
    "com.cisco.ise.pubsub",
]


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
    try:
        while True:
            message = json.loads(await ws.stomp_read_message())
            print(json.dumps(message, indent=2, sort_keys=True), file=sys.stdout)
            sys.stdout.flush()
    except CancelledError as e:
        pass
    logger.debug('shutting down listener...')
    await ws.stomp_disconnect('123')
    await asyncio.sleep(2.0)
    await ws.disconnect()
        

if __name__ == '__main__':

    #
    # this will parse all the CLI options, and there **must** be EITHER
    # a '--services' OR '--subscribe'
    #
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
        for stomp_mod in ['stomp', 'ws_stomp']:
            s_logger = logging.getLogger(stomp_mod)
            handler.setFormatter(logging.Formatter('%(asctime)s:%(name)s:%(levelname)s:%(message)s'))
            s_logger.addHandler(handler)
            s_logger.setLevel(logging.DEBUG)


    #
    # if we jst have a request for services and no hostname, we can only
    # list out the services we know about
    #
    if config.services and (not config.hostname):
        print("Known services:")
        for service in sorted(SERVICE_NAMES):
            print('    %s' % service)
        sys.exit(0)

    #
    # if we at least have a hostname, we can move forward and set up the
    # px grid control object and look at either deeper service discovery
    # or just subscribing to what we're asked to subscribe to
    #
    pxgrid = PxgridControl(config=config)

    #
    # in case we need to go appropve in the ISE UI
    #
    while pxgrid.account_activate()['accountState'] != 'ENABLED':
        time.sleep(60)

    # lookup for session service
    if config.services:
        slr_responses = []
        for service in SERVICE_NAMES:
            service_lookup_response = pxgrid.service_lookup(service)
            slr_responses.append(service_lookup_response)

            #
            # log for debug
            #
            slr_string = json.dumps(service_lookup_response, indent=2, sort_keys=True)
            logger.debug('service %s lookup response:', service)
            slr_string = json.dumps(service_lookup_response, indent=2, sort_keys=True)
            logger.debug('service lookup response:')
            for s in slr_string.splitlines():
                logger.debug('  %s', s)

        #
        # dump all services as a json array pretty-printed
        #
        print(json.dumps(slr_responses, indent=2, sort_keys=True))
        sys.exit(0)

    # if we drop through to here, we must be subscribing, so do some initial
    # checks to make sure we have enough parameters
    if config.service is None or config.topic is None:
        logger.error('must have a service and a topic!')
        sys.exit(1)

    #
    # now subscribe
    #
    service_lookup_response = pxgrid.service_lookup(config.service)
    slr_string = json.dumps(service_lookup_response, indent=2, sort_keys=True)
    logger.debug('service lookup response:')
    for s in slr_string.splitlines():
        logger.debug('  %s', s)
    service = service_lookup_response['services'][0]
    pubsub_service_name = service['properties']['wsPubsubService']
    try:
        topic = service['properties'][config.topic]
    except KeyError as e:
        logger.debug('invald topic %s', config.topic)
        possible_topics = [k for k in service['properties'].keys() if k != 'wsPubsubService' and k != 'restBaseUrl' and k != 'restBaseURL']
        logger.debug('possible topic handles: %s', ', '.join(possible_topics))
        sys.exit(1)

    service_lookup_response = pxgrid.service_lookup(pubsub_service_name)
    pubsub_service = service_lookup_response['services'][0]
    pubsub_node_name = pubsub_service['nodeName']
    secret = pxgrid.get_access_secret(pubsub_node_name)['secret']
    ws_url = pubsub_service['properties']['wsUrl']

    # This has been added for if a non-standard port is being used via an
    # API gateway like Kong. pxGrid still returns an embedded port number.
    #
    # TODO: remove when appropriate
    #
    ws_url = ws_url.replace('8910', str(config.port))
    
    loop = asyncio.get_event_loop()
    main_task = asyncio.ensure_future(subscribe_loop(config, secret, ws_url, topic))
    loop.add_signal_handler(SIGINT, main_task.cancel)
    loop.add_signal_handler(SIGTERM, main_task.cancel)
    try:
        loop.run_until_complete(main_task)
    except:
        pass

