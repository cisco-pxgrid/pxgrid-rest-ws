package com.cisco.pxgrid.samples.ise.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SampleConfiguration {
    private String[] hostnames;
    private String nodeName;
    private String password;
    private String description;
    private String keystoreFilename;
    private String keystorePassword;
    private String truststoreFilename;
    private String truststorePassword;

    private SSLContext sslContext;
    private Options options = new Options();

	public SampleConfiguration() {
		options.addOption("a", "hostname", true, "Host name (multiple ok)");
		options.addOption("u", "nodename", true, "Node name");
		options.addOption("w", "password", true, "Password");
		options.addOption("d", "description", true, "Description (optional)");
		options.addOption("k", "keystorefilename", true, "Keystore .jks filename (optional)");
		options.addOption("p", "keystorepassword", true, "Keystore password (required if keystore filename used)");
		options.addOption("t", "truststorefilename", true, "Truststore .jks filename");
		options.addOption("q", "truststorepassword", true, "Truststore password");
	}
    
    public String getNodeName() {
		return nodeName;
	}

    public String[] getHostnames() {
		return hostnames;
	}

    public String getPassword() {
		return password;
	}

    public String getDescription() {
		return description;
	}

    public SSLContext getSSLContext() {
    		return sslContext;
    }
    
    public Options getOptions() {
		return options;
	}
    
    private KeyManager[] getKeyManagers() throws IOException, GeneralSecurityException {
        if (keystoreFilename == null) {
        		return null;
        }
		KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream in = new FileInputStream(keystoreFilename);
        ks.load(in, keystorePassword.toCharArray());
        in.close();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keystorePassword.toCharArray());
        return kmf.getKeyManagers();
    }
    
	private TrustManager[] getTrustManagers() throws IOException, GeneralSecurityException {
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream in = new FileInputStream(truststoreFilename);
		ks.load(in, truststorePassword.toCharArray());
		in.close();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		return tmf.getTrustManagers();
	}

	public void parse(String[] args) throws ParseException, IOException, GeneralSecurityException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		hostnames = cmd.getOptionValues("a");
		nodeName = cmd.getOptionValue("u");
		password = cmd.getOptionValue("w");
		description = cmd.getOptionValue("d");
		keystoreFilename = cmd.getOptionValue("k");
		keystorePassword = cmd.getOptionValue("p");
		truststoreFilename = cmd.getOptionValue("k");
		truststorePassword = cmd.getOptionValue("p");
       
        if (hostnames == null) throw new IllegalArgumentException("Missing host name");
        if (nodeName == null) throw new IllegalArgumentException("Missing node name");
        if (truststoreFilename == null) throw new IllegalArgumentException("Missing truststore filename");
        if (truststorePassword == null) throw new IllegalArgumentException("Missing truststore password");

        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(getKeyManagers(), getTrustManagers(), null);

        // Print parse result
        System.out.println("------ config ------");
		for (Option option : options.getOptions()) {
			String[] values = cmd.getOptionValues(option.getOpt());
			if (values != null) {
				for (String value : values) {
					System.out.println("  " + option.getLongOpt() + " = " + value);
				}
			}
			else {
				System.out.println("  " + option.getLongOpt() + " = (not specified)");
			}
		}
        System.out.println("--------------------");
	}	
}
