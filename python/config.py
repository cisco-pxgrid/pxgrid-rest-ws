import argparse
import ssl
import warnings


class Config:
    def __init__(self):
        self.__ssl_context = None
        parser = argparse.ArgumentParser()
        parser.add_argument('-a', '--hostname', help='pxGrid controller host name (multiple ok)', action='append')
        parser.add_argument('--port', help='pxGrid controller port', default=8910)
        parser.add_argument('-n', '--nodename', help='Client node name')
        parser.add_argument('-w', '--password', help='Password (optional)')
        parser.add_argument('-d', '--description',
                            help='Description (optional)')
        parser.add_argument(
            '-c', '--clientcert', help='Client certificate chain pem filename (optional)')
        parser.add_argument('-k', '--clientkey',
                            help='Client key filename (optional)')
        parser.add_argument('-p', '--clientkeypassword',
                            help='Client key password (optional)')
        parser.add_argument('-s', '--servercert',
                            help='Server certificates pem filename')
        parser.add_argument('--service', type=str,
                            help='Service name')
        parser.add_argument('--topic', type=str,
                            help='Topic to subscribe to')
        parser.add_argument(
            '--subscribe', action='store_true',
            help='set up a subscription')
        parser.add_argument(
            '--services', action='store_true',
            help='List out supported services')
        parser.add_argument(
            '-v', '--verbose', action='store_true',
            help='Verbose output if relevant')

        self.config = parser.parse_args()

    @property
    def subscribe(self):
        return self.config.subscribe

    @property
    def services(self):
        return self.config.services

    @property
    def verbose(self):
        return self.config.verbose

    @property
    def hostname(self):
        return self.config.hostname

    @property
    def port(self):
        return self.config.port

    @property
    def node_name(self):
        return self.config.nodename

    @property
    def password(self):
        if self.config.password is not None:
            return self.config.password
        else:
            return ''

    @property
    def service(self):
        return self.config.service

    @property
    def topic(self):
        return self.config.topic

    @property
    def description(self):
        return self.config.description

    @property
    def ssl_context(self):
        if self.__ssl_context == None:
            self.__ssl_context = ssl.create_default_context()
            if self.config.clientcert is not None:
                self.__ssl_context.load_cert_chain(
                    certfile=self.config.clientcert,
                    keyfile=self.config.clientkey,
                    password=self.config.clientkeypassword)
            if self.config.servercert:
                self.__ssl_context.load_verify_locations(cafile=self.config.servercert)
            else:
                warnings.warn("check_hostname and cert not used; unsafe for production use")
                self.__ssl_context.check_hostname = False
                self.__ssl_context.verify_mode = ssl.CERT_NONE
        return self.__ssl_context
