------------------------------------------------------
pxGrid Python Samples
------------------------------------------------------
The pxGrid python samples demonstrate how to develop pxGrid clients in python.

* Before running samples:
    - Install latest Python 3.6: https://www.python.org/downloads/
    - Install necessary python packages using pip: ~$ pip3 install -r requirements.txt

* Description of samples:
    - session_subscribe.py:                 subscribes to the session topic and prints incoming notifications
    - session_query_by_ip.py:               performs a query on the session topic using a given IP address
    - session_query_all.py:                 downloads all current sessions

* Arguments for samples
  -h, --help            show this help message and exit
  -a HOSTNAME, --hostname HOSTNAME
                        pxGrid controller host name (multiple ok)
  -n NODENAME, --nodename NODENAME
                        Client node name
  -w PASSWORD, --password PASSWORD
                        Password (optional)
  -d DESCRIPTION, --description DESCRIPTION
                        Description (optional)
  -c CLIENTCERT, --clientcert CLIENTCERT
                        Client certificate chain pem filename (optional)
  -k CLIENTKEY, --clientkey CLIENTKEY
                        Client key filename (optional)
  -p CLIENTKEYPASSWORD, --clientkeypassword CLIENTKEYPASSWORD
                        Client key password (optional)
  -s SERVERCERT, --servercert SERVERCERT
                        Server certificate pem filename
  -f FILTER, --filter FILTER
                       Server Side Filter (optional)
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
