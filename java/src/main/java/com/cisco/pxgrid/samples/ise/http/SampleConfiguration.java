package com.cisco.pxgrid.samples.ise.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SampleConfiguration {
    protected final static String PROP_HOSTNAMES="PXGRID_HOSTNAMES";
    protected final static String PROP_USERNAME="PXGRID_USERNAME";
    protected final static String PROP_NODENAME="PXGRID_NODENAME";
    protected final static String PROP_PASSWORD="PXGRID_PASSWORD";
    protected final static String PROP_DESCRIPTION="PXGRID_DESCRIPTION";
    protected final static String PROP_KEYSTORE_FILENAME="PXGRID_KEYSTORE_FILENAME";
    protected final static String PROP_KEYSTORE_PASSWORD="PXGRID_KEYSTORE_PASSWORD";
    protected final static String PROP_TRUSTSTORE_FILENAME="PXGRID_TRUSTSTORE_FILENAME";
    protected final static String PROP_TRUSTSTORE_PASSWORD="PXGRID_TRUSTSTORE_PASSWORD";

    private String[] hostnames;
    private String nodeName;
    private String password;
    private String description;
    private SSLContext sslContext;

    private String keystoreFilename;
    private String keystorePassword;
    private String truststoreFilename;
    private String truststorePassword;
    
	public SampleConfiguration() throws GeneralSecurityException, IOException {
		loadProperties();
		printProperties();
	}
    
    public String getNodeName() {
		return nodeName;
	}

    public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

    public String getDescription() {
		return description;
	}

    public SSLContext getSSLContext() {
    	return sslContext;
    }
    
    public String getPassword() {
		return password;
	}
    
    public String[] getHostnames() {
		return hostnames;
	}
    
    private void loadProperties() throws GeneralSecurityException, IOException {
        String hostnameProperty = System.getProperty(PROP_HOSTNAMES);
        nodeName = System.getProperty(PROP_NODENAME);
        if (nodeName == null) {
        	// For older scripts
            nodeName = System.getProperty(PROP_USERNAME);
        }
        password = System.getProperty(PROP_PASSWORD);
        description = System.getProperty(PROP_DESCRIPTION);

        keystoreFilename = System.getProperty(PROP_KEYSTORE_FILENAME);
        keystorePassword = System.getProperty(PROP_KEYSTORE_PASSWORD);
        truststoreFilename = System.getProperty(PROP_TRUSTSTORE_FILENAME);
        truststorePassword = System.getProperty(PROP_TRUSTSTORE_PASSWORD);
       
        if (hostnameProperty == null || hostnameProperty.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_HOSTNAMES);
        if (nodeName == null || nodeName.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_USERNAME);
        if (truststoreFilename == null || truststoreFilename.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_TRUSTSTORE_FILENAME);
        if (truststorePassword == null || truststorePassword.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_TRUSTSTORE_PASSWORD);

        hostnames = hostnameProperty.split(",");
        
        if (description != null) {
                if (description.isEmpty()) description = null;
                else description = description.trim();
        }

        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(getKeyManagers(), getTrustManagers(), null);
    }

    private KeyManager[] getKeyManagers() throws IOException, GeneralSecurityException{
        if (keystoreFilename == null || keystoreFilename.isEmpty()) return null;

		KeyStore ks = keystoreFilename.endsWith(".p12") ? KeyStore.getInstance("pkcs12") : KeyStore.getInstance("JKS");
        FileInputStream in = new FileInputStream(keystoreFilename);
        ks.load(in, keystorePassword.toCharArray());
        in.close();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keystorePassword.toCharArray());
        return kmf.getKeyManagers();
    }
    
	private TrustManager[] getTrustManagers() throws IOException, GeneralSecurityException {
		KeyStore ks = truststoreFilename.endsWith(".p12") ? KeyStore.getInstance("pkcs12") : KeyStore.getInstance("JKS");
		FileInputStream in = new FileInputStream(truststoreFilename);
		ks.load(in, truststorePassword.toCharArray());
		in.close();
		
		Enumeration<String> e = ks.aliases();
		while (e.hasMoreElements()) {
			String alias = e.nextElement();
			if (ks.isKeyEntry(alias)) {
				// Adding certs from PrivateKeyEntry as trusted
				Certificate[] certs = ks.getCertificateChain(alias);
				for (int i = 0; i < certs.length; ++i) {
					ks.setCertificateEntry(alias + "." + i, certs[i]);
				}
			}
		}
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		return tmf.getTrustManagers();
	}

	private void printProperties() {
        System.out.println("------- properties -------");
        System.out.print("  hostnames=");
        for (String hostname : hostnames) System.out.print(hostname + " ");
        System.out.println();
        System.out.println("  nodeName=" + nodeName);
        System.out.println("  password=" + password);
        System.out.println("  description=" + description);
        System.out.println("  keystoreFilename=" + keystoreFilename);
        System.out.println("  keystorePassword=" + keystorePassword);
        System.out.println("  truststoreFilename=" + truststoreFilename);
        System.out.println("  truststorePassword=" + truststorePassword);
        System.out.println("--------------------------");
    }
}
