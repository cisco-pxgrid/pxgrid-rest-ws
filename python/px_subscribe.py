import asyncio
from asyncio.tasks import FIRST_COMPLETED
from pxgrid import PxgridControl
from config import Config
import json
import sys
import time
import logging
import threading
import textwrap
import hashlib
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp
from signal import SIGINT, SIGTERM


#
# the global logger
#
logger = logging.getLogger(__name__)


#
# lock for deduplicating session events received
#
dedup_lock = threading.Lock()


#
# dictionary for storing event keys in
#
# TODO: this really needs a cleaner to remove old events
#
event_keys = {}


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
    "com.cisco.endpoint",
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


async def default_subscription_loop(config, secret, pubsub_node_name, ws_url, topic):
    '''
    Simple subscription loop just to display whatever events arrive.
    '''
    logger.debug('default_subscription_loop: starting subscription to %s at %s', topic, ws_url)
    ws = WebSocketStomp(ws_url, config.node_name, secret, config.ssl_context)
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic)
    try:
        while True:
            message = json.loads(await ws.stomp_read_message())
            logger.debug('[%s] message received', pubsub_node_name)
            print(json.dumps(message, indent=2, sort_keys=True), file=sys.stdout)
            sys.stdout.flush()
    except asyncio.CancelledError as e:
        pass
    logger.debug('shutting down listener...')
    await ws.stomp_disconnect('123')
    await asyncio.sleep(2.0)
    await ws.disconnect()


async def session_dedup_loop(config, secret, pubsub_node_name, ws_url, topic):
    '''
    Subscription loop specifically for ISE pxGrid sessionTopic events. The
    logic for de-duplication is based around callingStationId, timestamp and
    event content. Multiple events may have the same callimgStationId and 
    timestamp, but attribute changes, like profiling determining the operating
    system for a device, may result in events that have the same timestamp but
    different contents.
    
    The algorithm in this routine takes this into account, and will "de-
    duplicate" the events (i.e. tell you when a duplicate event arrived). It
    uses MD5 (for speed) on a key-sorted dump of the event (which ensures that
    duplicate events are detected by the hash digest differing.)
    '''
    logger.debug('session_dedup_loop: starting subscription to %s at %s', topic, ws_url)
    assert topic == '/topic/com.cisco.ise.session', '%s is not the sessionTopic'

    ws = WebSocketStomp(ws_url, config.node_name, secret, config.ssl_context)
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic)
    try:
        while True:
            message = json.loads(await ws.stomp_read_message())
            logger.debug('[%s] message received', pubsub_node_name)
            with dedup_lock:
                for s in message['sessions']:
                    event_text = json.dumps(s, indent=2, sort_keys=True)
                    event_hash = hashlib.md5(event_text.encode()).hexdigest()
                    event_key = '{}:{}:{}'.format(
                        s['callingStationId'], s['timestamp'], event_hash)
                    if event_keys.get(event_key):
                        event_keys[event_key]['count'] = event_keys[event_key]['count'] + 1
                        print('duplicate mac:timestamp:hash event, count {}'.format(
                            event_keys[event_key]['count']))
                        print('    --> {}'.format(ws_url))
                    else:
                        event_keys[event_key] = {}
                        event_keys[event_key]['count'] = 1
                        event_keys[event_key]['time'] = time.time()
                        event_keys[event_key]['event'] = event_text
                        event_keys[event_key]['md5'] = event_hash
                        print('{}\nevent from {}'.format('-' * 75, ws_url))
                        print(json.dumps(s, indent=2, sort_keys=True))
            sys.stdout.flush()
    except asyncio.CancelledError as e:
        pass
    logger.debug('shutting down listener...')
    await ws.stomp_disconnect('123')
    await asyncio.sleep(2.0)
    await ws.disconnect()


# subscribe to topic on ALL service nodes returned
async def run_subscribe_all(task_list):
    logger.debug('run_subscribe_all')
    if len(task_list) > 0:
        try:
            return await asyncio.gather(*task_list)
        except asyncio.CancelledError as e:
            for t in task_list:
                t.cancel()
            return await asyncio.gather(*task_list)


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
        for stomp_mod in ['stomp', 'ws_stomp', 'pxgrid']:
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

    # get the details of a specific service and then exit
    if config.service_details:

        # first, the basic service
        service_lookup_response = pxgrid.service_lookup(config.service_details)
        print(json.dumps(service_lookup_response, indent=2, sort_keys=True))

        # check if any of tje services have a "wsPubsubService", and, if so,
        # also list out those services
        if "services" in service_lookup_response:
            topics = []
            for s in service_lookup_response['services']:
                pubsub_service = s['properties'].get('wsPubsubService')
                if pubsub_service:
                    for p, v in s['properties'].items():
                        if 'topic' in p.lower():
                            topics.append({p: v, 'wsPubsubService': pubsub_service})
                    break
            
            # lookup the pubsub service if there is one
            pubsub_slr = pxgrid.service_lookup(pubsub_service)
            if pubsub_slr:
                print(json.dumps(pubsub_slr, indent=2, sort_keys=True))

        # now exit
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

    # lookup the pubsub service
    service_lookup_response = pxgrid.service_lookup(pubsub_service_name)

    # select the subscription loop
    subscription_loop = default_subscription_loop
    if config.session_dedup:
        subscription_loop = session_dedup_loop

    if not config.subscribe_all:

        # just subscribe to first pubsub service node returned
        pubsub_service = service_lookup_response['services'][0]
        pubsub_node_name = pubsub_service['nodeName']
        secret = pxgrid.get_access_secret(pubsub_node_name)['secret']
        ws_url = pubsub_service['properties']['wsUrl']

        loop = asyncio.get_event_loop()
        main_task = asyncio.ensure_future(subscription_loop(config, secret, pubsub_node_name, ws_url, topic))
        loop.add_signal_handler(SIGINT, main_task.cancel)
        loop.add_signal_handler(SIGTERM, main_task.cancel)
        try:
            loop.run_until_complete(main_task)
        except:
            pass

    else:

        # create all subscription tasks
        subscriber_tasks = []
        loop = asyncio.get_event_loop()
        for pubsub_service in service_lookup_response['services']:
            pubsub_node_name = pubsub_service['nodeName']
            secret = pxgrid.get_access_secret(pubsub_node_name)['secret']
            ws_url = pubsub_service['properties']['wsUrl']
            logger.debug('creating task to subscribe to %s', ws_url)
            task = asyncio.ensure_future(subscription_loop(config, secret, pubsub_node_name, ws_url, topic))
            subscriber_tasks.append(task)

        # create the run all task and graceful termination handling
        try:
            logger.debug('Create run all task')
            run_all_task = asyncio.ensure_future(run_subscribe_all(subscriber_tasks))
            logger.debug('Add signal handlers to run all task')
            loop.add_signal_handler(SIGINT, run_all_task.cancel)
            loop.add_signal_handler(SIGTERM, run_all_task.cancel)
            loop.run_until_complete(run_all_task)
        except:
            pass
