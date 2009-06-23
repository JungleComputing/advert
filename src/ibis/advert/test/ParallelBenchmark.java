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

public class ParallelBenchmark {
	private static final String SERVER = "google://bbn230.appspot.com/";
	private static final String USER   = "ibisadvert@gmail.com";
	private static final int    TRIES  = 10;

	final static Logger logger = LoggerFactory.getLogger(ParallelBenchmark.class);

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
	 * @param args
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
		
		//add(advert);
		get(advert);
	}
}
