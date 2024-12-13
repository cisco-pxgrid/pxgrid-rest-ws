import asyncio
import json
import time

from config import Config
from pxgrid import PxgridControl
from websockets import ConnectionClosed
from ws_stomp import WebSocketStomp


async def subscribe_loop(config, secret, ws_url, topic):
    ws = WebSocketStomp(ws_url, config.get_node_name(),
                        secret, config.get_ssl_context())
    await ws.connect()
    await ws.stomp_connect(pubsub_node_name)
    await ws.stomp_subscribe(topic, config.get_filter())
    print("Press <Ctrl-C> to disconnect")
    while True:
        try:
            message = await ws.stomp_read_message()
            message_json = json.loads(message)
            print("message=" + json.dumps(message_json))
        except ConnectionClosed:
            print('Websocket connection closed')
            break
        except asyncio.CancelledError:
            await ws.stomp_disconnect('123')
            # wait for receipt
            await asyncio.sleep(3)
            await ws.disconnect()
            print("Websocket connection disconnected")
            break


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

    asyncio.run(subscribe_loop(config, secret, ws_url, topic))
