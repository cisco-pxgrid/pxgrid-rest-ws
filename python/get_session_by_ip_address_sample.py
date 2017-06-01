from python.common import *

if __name__ == '__main__':
    print('~ ~ ~ getSessionByIpAddress Sample Started ~ ~ ~\n')

    pxgrid_servers, \
        client_username, \
        client_groups, \
        client_description, \
        client_ca_trust_folderpath, \
        client_cert_pem_filepath, \
        client_cert_key_filepath, \
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

    while True:
        print('Enter IP address or \'quit\' to disconnect: ', end='', flush=True)
        ip = input()

        if ip is None or len(ip) == 0 or 'quit' in ip:
            break
        else:
            response = perform_service_lookup(
                hostname=pxgrid_servers[0],
                context=ssl_context,
                service_name='com.cisco.ise.session',
                username=client_username)

            if response['services'] is None or len(response['services']) == 0:
                print('No services returned...')
                continue

            for service in response['services']:
                secret = perform_access_secret_retrieval(
                    hostname=pxgrid_servers[0],
                    context=ssl_context,
                    username=client_username,
                    node_name=service['nodeName'])

                print('Performing query by IP (' + ip + ')...')
                response = send_rest_request(
                    hostname=pxgrid_servers[0],
                    url_suffix=service['properties']['restBaseURL'] + '/getSessionByIpAddress',
                    context=ssl_context,
                    username=client_username,
                    payload=json.dumps({'ipAddress': ip}),
                    access_secret=secret)
                if response is not None:
                    print('Response: ' + response + '\n')

    print('\n~ ~ ~ getSessionByIpAddress Sample Finished ~ ~ ~')

