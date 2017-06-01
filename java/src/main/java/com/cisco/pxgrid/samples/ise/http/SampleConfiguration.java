package com.cisco.pxgrid.samples.ise.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public class SampleConfiguration {
    protected final static String PROP_HOSTNAMES="PXGRID_HOSTNAMES";
    protected final static String PROP_USERNAME="PXGRID_USERNAME";
    protected final static String PROP_PASSWORD="PXGRID_PASSWORD";
    protected final static String PROP_GROUP="PXGRID_GROUP";
    protected final static String PROP_DESCRIPTION="PXGRID_DESCRIPTION";
    protected final static String PROP_KEYSTORE_FILENAME="PXGRID_KEYSTORE_FILENAME";
    protected final static String PROP_KEYSTORE_PASSWORD="PXGRID_KEYSTORE_PASSWORD";
    protected final static String PROP_TRUSTSTORE_FILENAME="PXGRID_TRUSTSTORE_FILENAME";
    protected final static String PROP_TRUSTSTORE_PASSWORD="PXGRID_TRUSTSTORE_PASSWORD";

    private String[] hostnames;
    private String username;
    private String password;
    private String[] groups;
    private String description;
    private SSLContext sslContext;

    private String keystoreFilename;
    private String keystorePassword;
    private String truststoreFilename;
    private String truststorePassword;
    
	public SampleConfiguration() throws GeneralSecurityException, IOException {
		load();
		print();
	}
    
    public String getUserName() {
		return username;
	}

    public void setUsername(String username) {
		this.username = username;
	}

	public String[] getGroups() {
		return groups;
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
    
    private void load() throws GeneralSecurityException, IOException {
        String hostnameProperty = System.getProperty(PROP_HOSTNAMES);
        username = System.getProperty(PROP_USERNAME);
        password = System.getProperty(PROP_PASSWORD);
        String group_property = System.getProperty(PROP_GROUP);
        description = System.getProperty(PROP_DESCRIPTION);

        keystoreFilename = System.getProperty(PROP_KEYSTORE_FILENAME);
        keystorePassword = System.getProperty(PROP_KEYSTORE_PASSWORD);
        truststoreFilename = System.getProperty(PROP_TRUSTSTORE_FILENAME);
        truststorePassword = System.getProperty(PROP_TRUSTSTORE_PASSWORD);
       
        if (hostnameProperty == null || hostnameProperty.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_HOSTNAMES);
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_USERNAME);
        if (truststoreFilename == null || truststoreFilename.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_TRUSTSTORE_FILENAME);
        if (truststorePassword == null || truststorePassword.isEmpty()) throw new IllegalArgumentException("Missing " + PROP_TRUSTSTORE_PASSWORD);

        hostnames = hostnameProperty.split(",");
        
        if (group_property != null && !group_property.isEmpty()) {
        		groups = group_property.split(",");
        }
        
        if (description != null) {
                if (description.isEmpty()) description = null;
                else description = description.trim();
        }

        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(getKeyManagers(), getTrustManagers(), null);
    }
    
    public void setupAuth(HttpsURLConnection https) throws GeneralSecurityException, IOException {
    		Authenticator.setDefault(new MyAuthenticator());
    }

    private class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(username, password.toCharArray()));
        }
    }

	private KeyManager[] getKeyManagers() throws IOException, GeneralSecurityException {
		if (keystoreFilename == null || keystoreFilename.isEmpty())
			return null;

		KeyStore ks = keystoreFilename.endsWith(".p12") ? KeyStore.getInstance("pkcs12") : KeyStore.getInstance("JKS");
		FileInputStream in = new FileInputStream(keystoreFilename);
		ks.load(in, keystorePassword.toCharArray());
		in.close();
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, keystorePassword.toCharArray());
		KeyManager[] mngrs = kmf.getKeyManagers();

		if (mngrs == null || mngrs.length == 0) {
			throw new GeneralSecurityException("no key managers found");
		}

		if (mngrs[0] instanceof X509KeyManager == false) {
			throw new GeneralSecurityException("key manager is not for X509");
		}

		return new KeyManager[] { new SampleX509KeyManager((X509KeyManager) mngrs[0]) };
	}

	private TrustManager[] getTrustManagers() throws IOException, GeneralSecurityException {
		FileInputStream in = new FileInputStream(truststoreFilename);

		KeyStore ks = null;
		if(truststoreFilename.endsWith(".pem")) {
			ks = KeyStore.getInstance("JKS");
			ks.load(null, null);
			CertificateFactory certFac = CertificateFactory.getInstance("X.509");
			Collection<? extends Certificate> certs = certFac.generateCertificates(in);
			int i = 0;
			for(Certificate c : certs) {
				ks.setCertificateEntry("trust-" + i, c);
			}
		} else if(truststoreFilename.endsWith(".p12")) {
			ks = KeyStore.getInstance("pkcs12");
			ks.load(in, truststorePassword.toCharArray());
		} else {
			ks = KeyStore.getInstance("JKS");
			ks.load(in, truststorePassword.toCharArray());
		}
		
		in.close();

		Enumeration<String> e = ks.aliases();
		boolean hasCertEntries = false;

		while (e.hasMoreElements()) {
			String alias = e.nextElement();

			if (ks.isCertificateEntry(alias)) {
				hasCertEntries = true;
			}
		}

		if (hasCertEntries == false) {
			e = ks.aliases();

			while (e.hasMoreElements()) {
				String alias = e.nextElement();

				if (ks.isKeyEntry(alias)) {
					Certificate[] chain = ks.getCertificateChain(alias);

					for (int i = 0; i < chain.length; ++i) {
						ks.setCertificateEntry(alias + "." + i, chain[i]);
					}
				}
			}
		}

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		TrustManager[] tms = tmf.getTrustManagers();

		if (tms == null || tms.length == 0) {
			throw new GeneralSecurityException("no trust managers found");
		}

		if (tms[0] instanceof X509TrustManager == false) {
			throw new GeneralSecurityException("trust manager is not for X509");
		}

		return new TrustManager[] { new SampleX509TrustManager((X509TrustManager) tms[0]) };
	}
	
	private static class SampleX509KeyManager implements X509KeyManager {

		private X509KeyManager mngr;

		public SampleX509KeyManager(X509KeyManager mngr) {
			this.mngr = mngr;
		}

		@Override
		public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
			String alias = mngr.chooseClientAlias(arg0, arg1, arg2);

			if (alias == null) {
				alias = mngr.chooseClientAlias(arg0, null, arg2);

				if (alias == null) {
					throw new RuntimeException("no client certificate found ...");
				}
			}

			return alias;
		}

		@Override
		public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
			throw new RuntimeException("Not implemented");
		}

		@Override
		public X509Certificate[] getCertificateChain(String arg0) {
			return mngr.getCertificateChain(arg0);
		}

		@Override
		public String[] getClientAliases(String arg0, Principal[] arg1) {
			return mngr.getClientAliases(arg0, null);
		}

		@Override
		public PrivateKey getPrivateKey(String arg0) {
			return mngr.getPrivateKey(arg0);
		}

		@Override
		public String[] getServerAliases(String arg0, Principal[] arg1) {
			throw new RuntimeException("Not implemented");
		}
	}

	private static class SampleX509TrustManager implements X509TrustManager {
		private X509TrustManager mngr;

		public SampleX509TrustManager(X509TrustManager mngr) {
			this.mngr = mngr;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			try {
				mngr.checkServerTrusted(arg0, arg1);
			} catch (CertificateException e) {
				throw new CertificateException("Server certificate is not trusted:" + arg0[0].getSubjectX500Principal(),
						e);
			}
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return mngr.getAcceptedIssuers();
		}
	}

    private void print() {
        System.out.println("------- properties -------");
        System.out.print("  hostnames=");
        for (String hostname : hostnames) System.out.print(hostname + " ");
        System.out.println();
        System.out.println("  username=" + username);
        System.out.println("  password=" + password);
        System.out.print("  groups=");
        for (String group : groups) System.out.print(group + " ");
        System.out.println();
        System.out.println("  description=" + description);
        System.out.println("  keystoreFilename=" + keystoreFilename);
        System.out.println("  keystorePassword=" + keystorePassword);
        System.out.println("  truststoreFilename=" + truststoreFilename);
        System.out.println("  truststorePassword=" + truststorePassword);
        System.out.println("--------------------------");
    }
}
