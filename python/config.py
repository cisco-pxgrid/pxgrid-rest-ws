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
        parser.add_argument('--insecure', action='store_true',
                            help='Allow insecure server connections when using SSL')
        parser.add_argument('--service', type=str,
                            help='Service name')
        parser.add_argument('--topic', type=str,
                            help='Topic to subscribe to')
        parser.add_argument(
            '--subscribe', action='store_true',
            help='set up a subscription')
        parser.add_argument(
            '--subscribe-all', action='store_true',
            help='subscribe to ALL nodes discovered')
        parser.add_argument(
            '--session-dedup', action='store_true',
            help='run the sessionTopic de-duplicating subscriber')
        parser.add_argument(
            '--services', action='store_true',
            help='List out supported services')
        parser.add_argument(
            '--service-details', type=str,
            help='List out details of a specific service')
        parser.add_argument(
            '--ip', type=str,
            help='Optional IP address for queries')
        parser.add_argument(
            '--start-timestamp', type=str,
            help='Optional startTimestamp for queries')

        # options for applying and clearing ANC policies
        g = parser.add_mutually_exclusive_group()
        g.add_argument(
            '--apply-anc-policy', action='store_true',
            help='Apply ANC policy with a policy name, MAC address and NAS IP address')
        g.add_argument(
            '--clear-anc-policy', action='store_true',
            help='Clear ANC policy with a policy name, MAC address and NAS IP address')
        parser.add_argument(
            '--mac-address', type=str,
            help='Optional MAC address for ANC policies')
        parser.add_argument(
            '--anc-policy', type=str,
            help='Optional ANC policy name')
        parser.add_argument(
            '--nas-ip-address', type=str,
            help='Optional NAS IP address for ANC policies')
        parser.add_argument(
            '-v', '--verbose', action='store_true',
            help='Verbose output if relevant')

        self.config = parser.parse_args()

    @property
    def subscribe(self):
        return self.config.subscribe

    @property
    def subscribe_all(self):
        return self.config.subscribe_all

    @property
    def session_dedup(self):
        return self.config.session_dedup

    @property
    def services(self):
        return self.config.services

    @property
    def service_details(self):
        return self.config.service_details

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
    def ip(self):
        return self.config.ip

    @property
    def start_timestamp(self):
        return self.config.start_timestamp

    @property
    def apply_anc_policy(self):
        return self.config.apply_anc_policy
        
    @property
    def clear_anc_policy(self):
        return self.config.clear_anc_policy
        
    @property
    def mac_address(self):
        return self.config.mac_address

    @property
    def anc_policy(self):
        return self.config.anc_policy

    @property
    def nas_ip_address(self):
        return self.config.nas_ip_address

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
            elif self.config.insecure:
                self.__ssl_context.check_hostname = False
                self.__ssl_context.verify_mode = ssl.CERT_NONE
        return self.__ssl_context
