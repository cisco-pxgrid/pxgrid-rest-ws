Convert ISE pxGrid generated certificates from PEM to JKS using jks-converter:
  1. Generate key and certificates in PEM format from ISE Administration->pxGrid services->Certificate page
  2. Run "docker run -p 5000:80 alei121/jks-converter"
  3. Go to http://localhost:5000
  4. Drop all generated key and certificates to the page
  5. Enter password and download jks.zip
  6. The jks.zip contains keycert.jks and trust.jks

Running SessionSubscribe sample with maven:

`mvn exec:java -Dexec.mainClass="com.cisco.pxgrid.samples.ise.SessionSubscribe" -Dexec.args="-a <hostname> -u <nodename> -k <keycert.jks> -p <keycert.jks password> -t <trust.jks> -q <trust.jks password>"`



