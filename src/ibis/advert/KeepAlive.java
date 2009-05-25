package ibis.advert;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KeepAlive extends Thread {
	private String cookie = null;
	private String server = null;
	
	private static final String GOOGLE_FORMAT = "EEE, d-MMMMM-yyyy HH:mm:ss z";
	private static final long   GRACE_PERIOD  = 3600000; /* 1 hour */
	
	KeepAlive(String cookie, String server) {
		this.cookie = cookie;
		this.server = server;
	}

	private long getDateFromCookie() {
		String[] cookieArray = cookie.split(";");
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
	
	public void run() {
		while (true) {
			/* Calculate time to wait. */
			long now = new Date().getTime();
			long exp = getDateFromCookie();
			
			/* Wait until cookie almost expires. */
			try {
				Thread.sleep((exp - now) - GRACE_PERIOD);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/* Refresh cookie */
			cookie = noop();
			
			//return renewed cookie to parent
		}
	}
}
