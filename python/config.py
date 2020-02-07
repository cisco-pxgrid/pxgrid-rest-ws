import argparse
import ssl


class Config:
    def __init__(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('-a', '--hostname', help='pxGrid controller host name (multiple ok)', action='append')
        parser.add_argument('--port', help='pxGrid controller port', default=8910)
        parser.add_argument('-n', '--nodename', help='Client node name')
        parser.add_argument('-w', '--password', help='Password (optional)')
        parser.add_argument('-d', '--description', help='Description (optional)')
        parser.add_argument('-c', '--clientcert', help='Client certificate chain pem filename (optional)')
        parser.add_argument('-k', '--clientkey', help='Client key filename (optional)')
        parser.add_argument('-p', '--clientkeypassword', help='Client key password (optional)')
        parser.add_argument('-s', '--servercert', help='Server certificates pem filename')
        self.config = parser.parse_args()

    def get_host_name(self):
        return self.config.hostname

    def get_port(self):
        return self.config.port

    def get_node_name(self):
        return self.config.nodename

    def get_password(self):
        if self.config.password is not None:
            return self.config.password
        else:
            return ''

    def get_description(self):
        return self.config.description

    def get_ssl_context(self):
        context = ssl.create_default_context(purpose=ssl.Purpose.CLIENT_AUTH)
        if self.config.clientcert is not None:
            context.load_cert_chain(certfile=self.config.clientcert,
                                    keyfile=self.config.clientkey,
                                    password=self.config.clientkeypassword)
        context.load_verify_locations(capath=self.config.servercert)
        return context
