import signal
from python.common import *

if __name__ == '__main__':
    print('~ ~ ~ Subscribe to Session Topic Sample Started ~ ~ ~\n')

    pxgrid_servers,\
        client_username,\
        client_groups,\
        client_description,\
        client_ca_trust_folderpath,\
        client_cert_pem_filepath,\
        client_cert_key_filepath,\
        client_cert_password = load_properties_file()

    ssl_context = create_ssl_context(
        cert_file=client_cert_pem_filepath,
        key_file=client_cert_key_filepath,
        password=client_cert_password,
        ca_truststore=client_ca_trust_folderpath)

    perform_account_activate(
        hostname=pxgrid_servers[0],
        context=ssl_context,
        username=client_username,
        description=client_description,
        groups=client_groups)

    service_node, ws_url = perform_ws_service_lookup(
        hostname=pxgrid_servers[0],
        context=ssl_context,
        service_name='com.cisco.ise.pubsub',
        username=client_username)

    secret = perform_access_secret_retrieval(
        hostname=pxgrid_servers[0],
        context=ssl_context,
        username=client_username,
        node_name=service_node)

    loop = asyncio.get_event_loop()
    loop.add_signal_handler(signal.SIGINT, handle_shutdown)
    try:
        loop.run_until_complete(
            perform_ws_subscribe(
                ws_url,
                ['/topic/com.cisco.ise.session'],
                ssl_context,
                client_username,
                secret))
    except asyncio.CancelledError:
        print('Done.\n')
    finally:
        loop.close()

    print('~ ~ ~ Subscribe to Session Topic Sample Finished ~ ~ ~')

