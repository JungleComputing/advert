/*
 * Created 22 May 2009 by Bas Boterman.
 */

package ibis.advert;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used as {@link Thread} for keeping the connection with the
 * Google App Engine alive. It wil sleep for a certain amount of time
 * (determined by the Cookie's <code>expire</code> field before renewing its
 * cookie.
 * 
 * @author Bas
 *
 */

public class PersistentAuthentication extends Thread {
	private String cookie = null;
	private String server = null;
	
	final static Logger logger = LoggerFactory.getLogger(PersistentAuthentication.class);
	
	private static final String GOOGLE_FORMAT = "EEE, d-MMMMM-yyyy HH:mm:ss z";
	private static final double EXPIRATION_FACTOR = .9;
	private static final int    MAX_RETRIES   = 3;
	
	/**
	 * Constructor. Uses the current cookie plus the server address to
	 * initialize.
	 * 
	 * @param cookie
	 * 		A {@link String} containing the current cookie.
	 * @param server
	 * 		A {@link String} containing the server address.
	 */
	PersistentAuthentication(String cookie, String server) {
		setDaemon(true);
		this.cookie = cookie;
		this.server = server;
		logger.info("KeepAlive intialized.");
	}
	
	/**
	 * Sets the current cookie to the value as passed in <code>cook</code>.
	 * 
	 * @param cook
	 * 		A {@link String} containing the new value of the most recent 
	 * 		cookie.
	 */
	public void setCookie(String cook) {
		cookie = cook;
	}
	
	/**
	 * Public function which returns the most recent authentication cookie 
	 * known.
	 * 
	 * @return
	 * 		A {@link String} containing the most recent cookie.
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * Private function which parses the <code>expire</code> field in the 
	 * cookie, and returns the Epoch value.
	 * 
	 * @return
	 * 		A {@link long} containing the Epoch value of the date just
	 * 		parsed, or <code>-1</code> if parsing failed.
	 */
	private long getDateFromCookie() {
		String[] cookieArray = getCookie().split(";");
		String[] dateArray   = cookieArray[1].split("=");
		
		SimpleDateFormat sdf = new SimpleDateFormat(GOOGLE_FORMAT);
		
		if (dateArray != null && dateArray.length == 2 
				&& dateArray[1] != null && !dateArray[1].equals("")) {
			try {
				return sdf.parse(dateArray[1]).getTime();
			}
			catch (ParseException pe) {
				//TODO: Parse Exception
				pe.printStackTrace();
			}
		}
		return -1;	
	}
	
	/**
	 * Private function which sends a <code>NOOP</code> (i.e. a dummy-command)
	 * to the App Engine, in order to receive a renewed cookie.
	 * 
	 * @return
	 * 		A {@link String} containing the renewed cookie, or 
	 * 		<code>null</code> if renewing the cookie failed. 
	 */
	private String noop() {
		URL url;
		try {
			url = new URL("http://" + server + "/");

			HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
			
			/* Setting headers. */
			httpc.setRequestMethod("GET");
			httpc.setRequestProperty("Cookie", cookie); /* Current cookie. */
		    httpc.setUseCaches(false);
		    
		    /* Connecting and POSTing. */
			httpc.connect();
			
			/* Retrieving response headers. */
			int httpc_rc = httpc.getResponseCode();
		    if (httpc_rc >= HttpURLConnection.HTTP_BAD_REQUEST) { /* fail */
		    	return null;
		    }
		    
			for (int i = 0; httpc.getHeaderField(i) != null; i++) {
				if (httpc.getHeaderFieldKey(i) != null && 
					httpc.getHeaderFieldKey(i).equals("Set-Cookie")) {
					return httpc.getHeaderField(i); /* Renew Cookie. */
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null; /* fail */
	}
	
	/**
	 * Main function that is called when the {@link Thread} starts. This
	 * function infinitely calculates the time to sleep (until 
	 * <code>GRACE_PERIOD</code> before the cookie would expire), and then
	 * calls <code>noop()</code> to renew the cookie, which is sent to the
	 * parent.
	 */
	public void run() {
		logger.info("Thread started.");
		String noop = null;
		while (true) {
			/* Calculate time to wait. */
			int retries = 0;
			long now    = new Date().getTime();
			long exp    = getDateFromCookie();
			long slp = new Double((exp - now) * EXPIRATION_FACTOR).longValue();
			
			try {
				/* Wait until cookie almost expires. */
				logger.debug("Sleeping {} ms.", slp);
				Thread.sleep(slp);
			} catch (InterruptedException e) {
				return; /* Thread was interrupted, we quit. */
			}
			
			/* Check if cookie was already refreshed. */
			if (exp < getDateFromCookie()) {
				continue;
			}
			
			/* Refresh cookie */
			logger.info("Calling noop().");
			
			noop = noop();
			if (noop != null && !noop.equals("")) {
				setCookie(noop);
			}
			else if (retries++ > MAX_RETRIES) {
				logger.warn("Failed to refresh cookie. Returning...");
				return;
			}
			logger.debug("New cookie: {}", getCookie());
		}
	}
}
