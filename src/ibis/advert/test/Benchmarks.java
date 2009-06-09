package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmarks {
	private static final String SERVER = "google://bbn230.appspot.com/";
	private static final String USER   = "ibisadvert@gmail.com";
	private static int   TRIES         = 5;
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
		byte[] b = new byte[1000];
		MetaData md = new MetaData();
		
		md.put("key1", "value1");
		md.put("key2", "value2");
		md.put("key3", "value3");

		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.add(b, md, "/home/benchmarks/" + add);
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
	
	private static void connectivity(Advert advert) {
		long startTime = System.currentTimeMillis();
		while ((startTime + MS_TO_MEASURE) <= System.currentTimeMillis()) {
			/* do something */
		}
	}
	
	private static void conn_speed(Advert advert) {
		byte[] b = new byte[1000000 - 250000];
		MetaData md = new MetaData();
		
		md.put("key1", "value1");
		md.put("key2", "value2");
		md.put("key3", "value3");
		md.put("key4", "value3");
		md.put("key5", "value3");
		md.put("key6", "value3");
		md.put("key3", "value3");
		md.put("key3", "value3");

		long startTime = System.currentTimeMillis();
		try {
			advert.add(b, md, "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		
		/* Calculate max, min, mean. */
		System.out.println(1000000 / (stopTime - startTime) + "kB/s");
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
			System.err.println("Malformed URI");
			System.exit(-1);
		}
		
		/* Start benchmarks. */
//		advert_create(advertUri, args[0]);
//		round_trip_add(advert);
//		round_trip_del(advert);
		
//		connectivity(advert);
		
		conn_speed(advert);
	}
}
