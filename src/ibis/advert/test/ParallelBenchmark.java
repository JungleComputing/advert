package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains 4 advert server functions which can be tested for
 * parallel benchmarking purposes. The function that are called can be altered
 * by setting the constant variables below. Also, you should alter the location
 * and username of your advert server. Password is given on the command line for
 * security purposes.
 * 
 * @author Bas
 *
 */

public class ParallelBenchmark {
	private static final String  SERVER    = "google://jondoe.appspot.com/";
	private static final String  USER      = "jondoe@gmail.com";
	private static final int     TRIES     = 10;
	private static final boolean TEST_ADD  = true;
	private static final boolean TEST_GET  = true;
	private static final boolean TEST_FIND = true;
	private static final boolean TEST_DEL  = true;

	final static Logger logger = LoggerFactory.getLogger(ParallelBenchmark.class);

	/**
	 * Find matching {@link MetaData} from the datastore <code>TRIES</code>
	 * number of times. And time each individual action. The results are printed
	 * to a file in the form <code>min,avg,max</code>.
	 *  
	 * @param advert
	 * 		The advert server to connect to.
	 */
	private static void find(Advert advert) {
		String host = null;
		String  pid = null;
		MetaData md = new MetaData();
		
		/* Setup path name. */
		try {
			host = InetAddress.getLocalHost().getHostName();
			pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];		
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		/* Setup Meta Data. */
		for (int i=0; i<2; i++) {
			md.put("key" + i, "value" + i);
		}
		
		/* Setup Timing. */
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		
		/* Start timers. */
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.find(md);
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
		
		try {
			FileWriter fw = new FileWriter(host + "." + pid + ".txt");
			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("min,avg,max\n");
			bw.write(min + "," + tot/TRIES + "," + max + "\n");
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete a 1kB {@link Advert} object from the datastore <code>TRIES</code>
	 * number of times. And time each individual action. The results are printed
	 * to a file in the form <code>min,avg,max</code>.
	 *  
	 * @param advert
	 * 		The advert server to connect to.
	 */
	private static void del(Advert advert) {
		String host = null;
		String  pid = null;
		
		/* Setup path name. */
		try {
			host = InetAddress.getLocalHost().getHostName();
			pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];		
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		/* Setup Timing. */
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		
		/* Start timers. */
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.delete("/home/pbenchmarks/" + host + "/" + pid + "/" + add);
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
		
		try {
			FileWriter fw = new FileWriter(host + "." + pid + ".txt");
			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("min,avg,max\n");
			bw.write(min + "," + tot/TRIES + "," + max + "\n");
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a 1kB {@link Advert} object from the datastore <code>TRIES</code>
	 * number of times. And time each individual action. The results are printed
	 * to a file in the form <code>min,avg,max</code>.
	 *  
	 * @param advert
	 * 		The advert server to connect to.
	 */
	private static void get(Advert advert) {
		String host = null;
		String  pid = null;
		
		/* Setup path name. */
		try {
			host = InetAddress.getLocalHost().getHostName();
			pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];		
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		/* Setup Timing. */
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		
		/* Start timers. */
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
		
		try {
			FileWriter fw = new FileWriter(host + "." + pid + ".txt");
			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("min,avg,max\n");
			bw.write(min + "," + tot/TRIES + "," + max + "\n");
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a 1kB {@link Advert} object to the datastore <code>TRIES</code>
	 * number of times. And time each individual action. The results are printed
	 * to a file in the form <code>min,avg,max</code>.
	 *  
	 * @param advert
	 * 		The advert server to connect to.
	 */
	private static void add(Advert advert) {
		byte[]    b = new byte[73000];
		MetaData md = new MetaData();
		String host = null;
		String  pid = null;
		
		/* Setup path name. */
		try {
			host = InetAddress.getLocalHost().getHostName();
			pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];		
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		/* Setup Meta Data. */
		for (int i=0; i<5; i++) {
			md.put("key" + i, "value" + i);
		}

		/* Setup Timing. */
		long min = Long.MAX_VALUE;
		long max = 0;
		long tot = 0;
		long[] times = new long[TRIES];
		char add = 'a';
		
		/* Start timers. */
		for (int i=0; i<TRIES; i++) {
			long startTime = System.currentTimeMillis();
			try {
				advert.add(b, null, "/home/pbenchmarks/" + host + "/" + pid + "/" + add);
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
		
		/* Calculate max, min, avg. */
		try {
			FileWriter fw = new FileWriter(host + "." + pid + ".txt");
			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("min,avg,max\n");
			bw.write(min + "," + tot/TRIES + "," + max + "\n");
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main function which starts a class performing parallel benchmarks. This
	 * should be started in parallel, and preferable call only one function at
	 * the time (depending on what you would like to measure.
	 * 
	 * @param args
	 * 		Arguments that should be parsed: <code>password</code>
	 */
	public static void main(String[] args) {
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
		if (TEST_ADD) {
			add(advert);
		}
		if (TEST_GET) {
			get(advert);
		}
		if (TEST_FIND) {
			find(advert);
		}
		if (TEST_DEL) {
			del(advert);
		}
	}
}
