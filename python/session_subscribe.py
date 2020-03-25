import asyncio
import json
import signal
import sys
import time
from asyncio.tasks import FIRST_COMPLETED

from config import Config
from pxgrid import PxgridControl
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp


async def future_read_message(ws, future):
    try:
        message = await ws.stomp_read_message()
        future.set_result(message)
    except ConnectionClosed:
        print('Websocket connection closed')

async def subscribe_loop(config, secret, ws_url, topic):
    ws = WebSocketStomp(ws_url, config.get_node_name(), secret, config.get_ssl_context())
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic)
    print("Ctrl-C to disconnect...")
    while True:
        future = asyncio.Future()
        future_read = future_read_message(ws, future)
        try:
            await asyncio.wait([future_read], return_when=FIRST_COMPLETED)
        except asyncio.CancelledError:
            await ws.stomp_disconnect('123')
            # wait for receipt
            await asyncio.sleep(3)
            await ws.disconnect()
            break
        else:
            message = json.loads(future.result())
            print("message=" + json.dumps(message))


if __name__ == '__main__':
    config = Config()
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

    loop = asyncio.get_event_loop()
    subscribe_task = asyncio.ensure_future(subscribe_loop(config, secret, ws_url, topic))

    # Setup signal handlers
    loop.add_signal_handler(signal.SIGINT, subscribe_task.cancel)
    loop.add_signal_handler(signal.SIGTERM, subscribe_task.cancel)

    # Event loop
    loop.run_until_complete(subscribe_task)
