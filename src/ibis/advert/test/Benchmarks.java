package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.AppEngineResourcesException;
import ibis.advert.AuthenticationException;
import ibis.advert.MetaData;
import ibis.advert.RequestTooLargeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmarks {
	private static final String SERVER = "google://bbn230.appspot.com/";
	private static final String USER   = "ibisadvert@gmail.com";
	private static int   TRIES         = 10;
	private static long  MS_TO_MEASURE = 1000;

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);
	
	private static void advert_create(URI u, String p) {
		Advert advert = null;
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert = new Advert(u, USER, p);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stopTime = System.currentTimeMillis();
			times[i] = (stopTime - startTime);
			tot += times[i];
			
			if (times[i] < min) {
				min = times[i];
			}
			if (times[i] > max) {
				max = times[i];
			}
		}
		
		/* Calculate max, min, mean. */
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = " + min + "ms, Maximum = " + max +
				"ms, Average = " + tot/TRIES + "ms");		
	}
	
	private static void round_trip_add(Advert advert) {
		/* File sizes: 1kB, 10kB, 100kB, 1MB. */
		byte[] b = new byte[730];
		MetaData md = new MetaData();
		
		for (int i=0; i<1; i++) {
			md.put("key" + i, "value" + i);
		}

		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.add(b, null, "/home/benchmarks/" + add);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stopTime = System.currentTimeMillis();
			times[i] = (stopTime - startTime);
			tot += times[i];
			
			if (times[i] < min) {
				min = times[i];
			}
			if (times[i] > max) {
				max = times[i];
			}
			add++;
		}
		
		/* Calculate max, min, mean. */
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = " + min + "ms, Average = " + tot/TRIES + 
				"ms, Maximum = " + max + "ms");
	}
	
	private static void round_trip_get(Advert advert) {
		/* File sizes: 1kB, 10kB, 100kB, 1MB. */
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.get("/home/benchmarks/" + add);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stopTime = System.currentTimeMillis();
			times[i] = (stopTime - startTime);
			tot += times[i];
			
			if (times[i] < min) {
				min = times[i];
			}
			if (times[i] > max) {
				max = times[i];
			}
			add++;
		}
		
		/* Calculate max, min, mean. */
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = " + min + "ms, Average = " + tot/TRIES + 
				"ms, Maximum = " + max + "ms");
	}
	
	private static void round_trip_del(Advert advert) {
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.delete("/home/benchmarks/" + add);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stopTime = System.currentTimeMillis();
			times[i] = (stopTime - startTime);
			tot += times[i];
			
			if (times[i] < min) {
				min = times[i];
			}
			if (times[i] > max) {
				max = times[i];
			}
			add++;
		}
		
		/* Calculate max, min, mean. */
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = " + min + "ms, Maximum = " + max +
				"ms, Average = " + tot/TRIES + "ms");
	}
	
	private static void round_trip_find(Advert advert) {
		/* File sizes: 1kB, 10kB, 100kB, 1MB. */
		MetaData md = new MetaData();
		
		for (int i=0; i<100; i++) {
			md.put("key" + i, "value" + i);
		}

		char add = 'a';
		for (int i=0; i<TRIES; i++) {
			try {
				advert.find(md);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			add++;
		}
		
		/* Calculate max, min, mean. */
		System.out.println("done.");
	}
	
	private static void connectivity(Advert advert) {
		long startTime = System.currentTimeMillis();
		while ((startTime + MS_TO_MEASURE) <= System.currentTimeMillis()) {
			/* do something */
		}
	}
	
	private static void speedTest() {
		/*measure ping/up/down*/
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		
		
		
		for (int i=0; i<TRIES; i++) { //try 10 times
			try {
				byte[] b = new byte[1000000];
				String s = new String(b);
				URL url = new URL("http://bbn230.appspot.com/ping/down");
				HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
				
				/* Setting headers. */
//				httpc.setRequestMethod("POST");
				httpc.setRequestMethod("GET");
//				httpc.setDoInput(true);
			    httpc.setDoOutput(true);
//			    httpc.setUseCaches(false);
			    
			    /* Connecting and POSTing. */
			    
			    long startTime = System.currentTimeMillis();
			    
				httpc.connect();
//				OutputStreamWriter osw = new OutputStreamWriter(httpc.getOutputStream());
//				
//				/* Writing JSON data. */
//				for (int j=0; j<TRIES; j++) {
//					osw.write(s);
//				}
//				osw.flush();
//				osw.close();
				
				/* Retrieving response headers. */
				httpc.getResponseCode();
				BufferedReader in = new 
			    		BufferedReader(new InputStreamReader(httpc.getInputStream()));
			    
		        while ((in.readLine()) != null) {
		        }
				
				long stopTime = System.currentTimeMillis();
				times[i] = (stopTime - startTime);
				System.out.println(times[i]);
				tot += times[i];
				
				if (times[i] < min) {
					min = times[i];
				}
				if (times[i] > max) {
					max = times[i];
				}
				
			    httpc.disconnect();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = " + min + "ms, Average = " + tot/TRIES + 
				"ms, Maximum = " + max + "ms");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* Parse args. */
		if (args.length != 1) {
			logger.error("***Usage: AdvertTests <passwd>");
		}
		
		/* Create advert object. */
		Advert advert = null;
		URI advertUri = null;
		try {
			advertUri = new URI(SERVER);
			advert = new Advert(advertUri, USER, args[0]);
			logger.info("Advert object created.");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Start benchmarks. */
//		advert_create(advertUri, args[0]);
		round_trip_add(advert);
//		round_trip_get(advert);
//		round_trip_del(advert);
//		round_trip_find(advert);
		
//		connectivity(advert);
		
//		speedTest();
	}
}
