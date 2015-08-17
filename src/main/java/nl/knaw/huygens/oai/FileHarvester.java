package nl.knaw.huygens.oai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class FileHarvester extends OaiHarvester {

	public static void main(String[] args) throws Exception {
		FileHarvester fileHarvester = new FileHarvester(Integer.parseInt(args[0]), args[1], args[2], args[3]);
		System.out.println("Starting harvest of " + args[1]);
		fileHarvester.start();
	}

	public FileHarvester(int numThreads, String serviceUrl, String metadataPrefix, String set) {
		super(numThreads, serviceUrl, metadataPrefix, set);
	}

	@Override
	public void onHarvestComplete(int numberOfRecords) {
		System.out.println(new Date().getTime() + " - HARVEST COMPLETE");
		System.out.println("Starting GetRecord of " + numberOfRecords + " records");
	}

	@Override
	public void onRunnerStart(int amount) {
		System.out.println(new Date().getTime() + " - Starting HarvestRunner with " + amount + " identifiers");
	}

	@Override
	public void onRunnerDone() {
		System.out.println(new Date().getTime() + " - HarvestRunner Done");
	}

	@Override
	public void onResumptionToken(String resumptionToken) {
		System.out.println(new Date().getTime() + " - RESUMING HARVEST WITH: " + resumptionToken);
	}

	@Override
	public void getRecord(String identifier) throws Exception {
		String filename = identifier + ".xml";
		System.out.println("Saving record: " + identifier + " to " + filename);
		String query = String.format("verb=GetRecord&identifier=%s&metadataPrefix=oai_ead_full",
					URLEncoder.encode(identifier, "UTF-8"));
		URL url = new URL(serviceUrl + "?" + query);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(true);
		connection.setRequestProperty("Connection", "Keep-Alive");
		InputStream is = connection.getInputStream();
		PrintWriter out = new PrintWriter(new FileOutputStream(new File(filename)));

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			out.println(line);
		}
		is.close();
		out.close();
	}

	@Override
	public void handleDeletedRecord(String identifier) throws Exception {
		System.err.println("Deleted record: " + identifier);
	}
}
