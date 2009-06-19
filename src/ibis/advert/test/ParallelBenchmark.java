package ibis.advert.test;

import ibis.advert.Advert;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelBenchmark {
	private static final String SERVER = "google://bbn230.appspot.com/";
	private static final String USER   = "ibisadvert@gmail.com";
	private static int   TRIES         = 10;

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);

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

	}

}
