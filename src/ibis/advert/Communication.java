/*
 * Created 24 Mar. 2009 by Bas Boterman.
 */

package ibis.advert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the main functionality for communication from and to the
 * Google App Engine.
 * 
 * @author bbn230
 */

class Communication {
	final static Logger logger = LoggerFactory.getLogger(Communication.class);

	private static final int MAX_RETRIES  = 3; /* max number of retries */
	private static final String CLIENTLOGIN = 
		"https://www.google.com/accounts/ClientLogin";
	
	private String  server; /* server connecting to */
	private PersistentAuthentication pAuth; /* keep cookie alive */
	private Boolean pub;    /* denotes if server is public */
	
	/**
	 * Constructor of Communications class for connecting to a public server.
	 * 
	 * @param server
	 * 		{@link String} containing the location of Advert server to connect to.
	 */
	Communication(String server) {
		this.server = server;
		pub = true;
		logger.info("Connected to {}.", server);
	}
	
	/**
	 * Constructor of communications class for connecting to an authenticated
	 * server.
	 * 
	 * @param server
	 * 		{@link String} containing the location of Advert server to connect to.
	 * @param user
	 * 		{@link String} containing the Google Account to be authenticated with.
	 * @param passwd
	 * 		{@link String} containing the corresponding password.
	 * @throws MalformedURLException
	 * 		URL is malformed.
	 * @throws ProtocolException
	 * 		Wrong protocol is applied.
	 * @throws IOException
	 * 		I/O to server failed.
	 * @throws AuthenticationException
	 * 		Authentication to server failed.
	 */
	Communication(String server, String user, String passwd) 
	  throws MalformedURLException, ProtocolException, IOException, 
	  AuthenticationException {
		this.server = server;
		pub = false;
		authenticate(user, passwd);
		logger.info("Authenticated to {}.", server);
	}
	
	/**
	 * Function to authenticate to the Google App Engine, and starting the 
	 * cookie daemon at the {@link PersistentAuthentication} class.
	 * @param user
	 * 		{@link String} containing the Google Account to be authenticated with.
	 * @param passwd
	 * 		{@link String} containing the corresponding password.
	 * @throws MalformedURLException
	 * 		URL is malformed.
	 * @throws ProtocolException
	 * 		Wrong protocol is applied.
	 * @throws IOException
	 * 		I/O to server failed.
	 * @throws AuthenticationException
	 * 		Authentication to server failed.
	 */
	void authenticate(String user, String passwd) 
	  throws MalformedURLException, ProtocolException, IOException, 
	  AuthenticationException {
		URL url                 = null;
	    HttpURLConnection httpc = null;
		
		/* Create URL object and open connection to ClientLogin. */
		url   = new URL(CLIENTLOGIN);
		httpc = (HttpURLConnection) url.openConnection();
	  
	    /* Set properties of the connection. */
		httpc.setRequestMethod("POST");
	    httpc.setDoInput(true);
	    httpc.setDoOutput(true);
	    httpc.setUseCaches(false);
	    httpc.setRequestProperty("Content-Type", 
	                             "application/x-www-form-urlencoded");
	  
	    /* Form the POST parameters. */
	    StringBuilder content = new StringBuilder();
		content.append("Email=").append(URLEncoder.encode(user, "UTF-8"));
	    content.append("&Passwd=").append(URLEncoder.encode(passwd, "UTF-8"));
	    content.append("&service=").append(URLEncoder.encode("ah", "UTF-8"));
	    content.append("&source=").append(URLEncoder.encode("IBIS Advert", 
	    		                                            "UTF-8"));
	
	    /* Write output. */
	    OutputStream outputStream = httpc.getOutputStream();
	    outputStream.write(content.toString().getBytes("UTF-8"));
	    outputStream.close();
	  
	    /* Retrieve the output. */
	    InputStream inputStream;
	    logger.debug("ClientLogin response code: {}.", 
	    		httpc.getResponseCode());
	    if (httpc.getResponseCode() == HttpURLConnection.HTTP_OK) {
	    	inputStream = httpc.getInputStream();
	    } 
	    else {
	    	throw new AuthenticationException("ClientLogin failed: " + 
	    			user + "/********");
	    }
	    
	    /* Retrieve body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(inputStream));

		String inputLine = null;
		String authid    = null;

	    logger.debug("ClientLogin response body:");
		/* Extract auth token. */
	    while ((inputLine = in.readLine()) != null) {
		    logger.debug(inputLine);
			if (inputLine.startsWith("Auth")) {
				authid = inputLine.split("=")[1];
				break;
			}
		}
		
		in.close();
		
		/* Setting up a new connection to App Engine. */
		url = new URL("http://" + server + "/_ah/login?continue=https://" + 
				server + "/&auth=" + authid);
		httpc = (HttpURLConnection) url.openConnection();
		
	    httpc.setRequestMethod("GET");
	    httpc.setUseCaches(false);
	    
	    logger.debug("GAE response code: {}.", httpc.getResponseCode());	    
	    if (!(httpc.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || 
	    	  httpc.getResponseCode() == HttpURLConnection.HTTP_OK)) {
	    	throw new AuthenticationException();
	    }
		
		/* Retrieving cookie. */
	    logger.debug("GAE header fields:");
	    String cookie = null;
		for (int i = 0; httpc.getHeaderField(i) != null; i++) {
			logger.debug("{} - {}", httpc.getHeaderFieldKey(i), 
					httpc.getHeaderField(i));
			if (httpc.getHeaderFieldKey(i) != null && 
				httpc.getHeaderFieldKey(i).equals("Set-Cookie")) {
				cookie = httpc.getHeaderField(i);
			}
		}
		
		if (cookie == null) {
			throw new AuthenticationException("Retrieving App Engine " + 
					"authentication failed: no cookie");
		}
		else {
			/* Get expiration time and start NOOP thread. */
			logger.info("Starting Persistent Authentication thread.");
			pAuth = new PersistentAuthentication(cookie, server);
			pAuth.start();
		}
	}
	
	/**
	 * This function parses a server-generated error to a human readable format.
	 * @param message
	 * 		A {@link String}, containing the error message as thrown by the 
	 * 		server (most likely HTML).
	 * @return
	 * 		A {@link String}, containing a human readable error message, or
	 * 		<code>Unknown error</code>, if the error message could not be 
	 * 		parsed.
	 */
	private String parseError(String message) {
		String[] result = message.split("\n");
		return result[result.length - 2];
	}
	
	/**
	 * Function to send a payload {@link String} to a certain function, 
	 * specified by {@link ext}.
	 * @param ext
	 * 		{@link String} containing the server path to send the payload to.
	 * @param payload
	 * 		Message body to be sent as {@link String}.
	 * @return
	 * 		A {@link String} returning the server response (if any), or 
	 * 		otherwise an error message, if things went wrong.
	 * @throws MalformedURLException
	 * 		URL is malformed.
	 * @throws IOException
	 * 		I/O to server failed.
	 * @throws AuthenticationException
	 * 		Authentication to server failed.
	 * @throws AppEngineResourcesException
	 * 		The Advert server ran out of resources. Try again in 24H.
	 * @throws NoSuchElementException
	 * 		The Advert Server could not find the element requested.
	 * @throws RequestTooLargeException
	 * 		The Request sent was too large.
	 * @throws Exception
	 * 		Another Server error than the above is returned. Details are in 
	 * 		the error message.
	 */
	String httpSend(String ext, String payload) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		String  result  = null;
		int     retries = 0;
		
		do {
		    URL url = new URL("http://" + server + ext);
			HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
			
			/* Setting headers. */
			httpc.setRequestMethod("POST");
			if (!pub) { /* Authenticated server. */
				httpc.setRequestProperty("Cookie", pAuth.getCookie());
			}
			httpc.setDoInput(true);
		    httpc.setDoOutput(true);
		    httpc.setUseCaches(false);
		    
		    /* Connecting and POSTing. */
			httpc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(httpc.getOutputStream());
			
			/* Writing JSON data. */
			osw.write(payload);
			osw.flush();
			osw.close();
			
			BufferedReader in = null;
			
			/* Retrieving response headers. */
			int httpc_rc = httpc.getResponseCode();
			logger.info("HTTP Send() response code: {}", httpc_rc);
		    if (httpc_rc < HttpURLConnection.HTTP_BAD_REQUEST) { /* success */
		    	in = new 
		    		BufferedReader(new InputStreamReader(httpc.getInputStream()));
			} else { /* client/server error */
				in = new 
					BufferedReader(new InputStreamReader(httpc.getErrorStream()));
			}
		    
		    logger.debug("httpSend() header fields:");
			for (int i = 0; httpc.getHeaderField(i) != null; i++) {
				logger.debug("{} - {}", httpc.getHeaderFieldKey(i), 
						httpc.getHeaderField(i));
				if (httpc.getHeaderFieldKey(i) != null && 
					httpc.getHeaderFieldKey(i).equals("Set-Cookie")) {
					pAuth.setCookie(httpc.getHeaderField(i)); /* Renew. */
				}
			}		    
		    
		    StringBuilder body = new StringBuilder();
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	        	body.append(inputLine);
	        	body.append("\n");
	        }
		    
		    httpc.disconnect();
		    
		    result = body.toString();
		    
		    logger.debug("HTTP Send() response body:");
		    logger.debug(result);
	
		    /* Check status codes. */
			switch (httpc_rc) {
				case HttpURLConnection.HTTP_OK:
					/*fall through*/
				case HttpURLConnection.HTTP_CREATED:
				  	/*fall through*/
				case HttpURLConnection.HTTP_RESET:
					return result;
				case HttpURLConnection.HTTP_FORBIDDEN:
					throw new AuthenticationException(result);
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new NoSuchElementException(result);
				case HttpURLConnection.HTTP_REQ_TOO_LONG:
					/*fall through*/
				case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
					throw new RequestTooLargeException(result);
				case HttpURLConnection.HTTP_UNAVAILABLE:
					throw new AppEngineResourcesException(result);
				case HttpURLConnection.HTTP_INTERNAL_ERROR:
					retries++; /* increment number of retries */
					break;
				default: /* anything else */
					throw new Exception(result);
			}
		} while (retries < MAX_RETRIES); /* done MAX_RETRIES times */
		throw new Exception(parseError(result)); /* parsing internal error */
	}
	


	/**
	 * Function to call when communication class is destroyed. Cookie Daemon 
	 * thread will be interrupted accordingly.
	 */
	public void end() {
		if (!pub) {
			pAuth.interrupt();
		}
	}
}