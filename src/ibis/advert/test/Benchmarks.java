package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmarks {
	private static final String SERVER = "google://bbn230.appspot.com/";
	private static final String USER   = "ibisadvert@gmail.com";
	private static int TRIES           = 5;

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);
	
	private static void round_trip_add(Advert advert) {
		/* File sizes: 1kB, 10kB, 100kB, 1MB. */
		byte[] b = new byte[1000];
		MetaData md = new MetaData();
		
		md.put("key1", "value1");
		md.put("key2", "value2");
		md.put("key3", "value3");
		
		long[] runTime = new long[5];
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.add(b, md, "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stopTime = System.currentTimeMillis();
			runTime[i] = stopTime - startTime;
		}
		
		/* Calculate max, min, mean. */
		
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
		try {
			URI advertUri = new URI(SERVER);
			advert = new Advert(advertUri, USER, args[0]);
			logger.info("Advert object created.");
		}
		catch (Exception e) {
			System.err.println("Malformed URI");
			System.exit(-1);
		}
		
		/* Start benchmarks. */
		round_trip_add(advert);

	}
}
