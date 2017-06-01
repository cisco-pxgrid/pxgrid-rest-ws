------------------------------------------------------
pxGrid Python Samples
------------------------------------------------------
The pxGrid python samples demonstrate how to develop pxGrid clients in python.

* Description of each script:
    - common.py:                            contains common functions used by all sample scripts
    - get_session_by_ip_address_sample.py:  performs a query on the session topic using a given IP address
    - get_session_by_mac_address_sample.py: performs a query on the session topic using a given MAC address
    - get_sessions_sample.py:               downloads all current sessions
    - get_usergroup_by_username_sample.py:  performs a query on the userGroup topic using a given username
    - get_usergroups_sample.py:             downloads all current userGroups
    - properties.ini:                       configuration information for the samples
    - subscribe_session_topic_sample.py:    subscribes to the session topic and prints incoming notifications

* Setup:
    - Install latest Python 3.6: https://www.python.org/downloads/
    - Install necessary python packages using pip: ~$ python3 -m pip install <package-name>
        - Needed packages:
            - aiohttp
            - asyncio
            - configparser

* Generate pxGrid Certificates from ISE:
    - Navigate to ISE Admin GUI via any web browser and login
    - Navigate to Administration -> pxGrid Services
    - Click on the Certificates tab
    - Fill in the form as follows:
        - I want to:                   Generate a single certificate (without a certificate signing request)
        - Common Name (CN):            {fill in any name}
        - Certificate Download Format: Certificate in Privacy Enhanced Electronic Mail (PEM) format, key in PKCS8 PEM format (including certificate chain)
        - Certificate Password:        {fill in a password}
        - Confirm Password:            {fill in the same password as above}
    * Click the 'Create' button. A zip file should download to your machine
    * Extract the downloaded file. In the rest of this document, we'll refer to the extracted folder as /path/to/12345678_cert/

* Populate the properties.ini file as follows:
    [SAMPLE_PROPERTIES]
    pxgrid_servers = {fill in your ISE server name}
    client_username = {fill in any name for your client}
    client_groups = {fill in a group name, e.g. Session}
    client_description = {fill in any description for your client or leave this blank}
    client_ca_trust_folderpath = /path/to/12345678_cert
    client_cert_pem_filepath = /path/to/12345678_cert/{the .cer file whose name matches the CN entered in the previous section}
    client_cert_key_filepath = /path/to/12345678_cert/{the .key file whose name matches the CN entered in the previous section}
    client_cert_password = {the password entered in the previous section}

* Run any of the sample scripts: ~$ python subscribe_session_topic_sample.py
    - Sample Output:
        ~$ python subscribe_session_topic_sample.py
        ~ ~ ~ Subscribe to Session Topic Sample Started ~ ~ ~

        Loading config values from properties.ini file...
        [('pxgrid_servers', 'pxgrid.cisco.com'),
         ('client_username', 'python_sample_client'),
         ('client_groups', 'Session'),
         ('client_description', 'Python Sample Client'),
         ('client_ca_trust_folderpath', '/Users/admin/Desktop/12345678_cert'),
         ('client_cert_pem_filepath',
          '/Users/admin/Desktop/12345678_cert/py_sample_.cer'),
         ('client_cert_key_filepath',
          '/Users/admin/Desktop/12345678_cert/py_sample_.key'),
         ('client_cert_password', '153C1sc0!')]
        Done config loading values from properties.ini file!

        Creating SSL Context...
        Done creating SSL Context!

        Activating client account...
        https://pxgrid.cisco.com:8910/pxgrid/control/AccountActivate
        Client account has been activated! Server pxGrid version: 2.0.0.4

        Performing service look up to retrieve Websocket URL...
        https://pxgrid.cisco.com:8910/pxgrid/control/ServiceLookup
        Response: {"services": [{"name": "com.cisco.ise.pubsub", "nodeName": "ise-admin-pxgrid", "properties": {"wsUrl": "wss://pxgrid.cisco.com:8910/pxgrid/ise/pubsub", "WSURL": "wss://pxgrid.cisco.com:8910/pxgrid/ise/pubsub"}}]}

        Retrieving Access Secret...
        https://pxgrid.cisco.com:8910/pxgrid/control/AccessSecret
        Received secret: J8A3baTlB5tA3B43

        Performing Subscribe over Websocket...
        Sending subscribe STOMP frame...
        Subscribed and ready to receive notifications!
        Listening... (CTRL-C to quit)

