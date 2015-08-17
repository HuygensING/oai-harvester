package nl.knaw.huygens.oai;

import net.sf.saxon.tree.tiny.TinyNodeImpl;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class OaiHarvester {
	protected static int numberOfRecords = 0;

	protected String metadataPrefix;
	protected String set;
	protected int numThreads = 1;
	protected String identifier = null;
	protected String serviceUrl;
	private List<HarvestRunner> harvestRunners = new ArrayList<>();

	public OaiHarvester(int numThreads, String serviceUrl, String metadataPrefix, String set) {
		this.numThreads = numThreads;
		this.metadataPrefix = metadataPrefix;
		this.set = set;
		this.serviceUrl = serviceUrl;
	}


	public void start() throws Exception {
		if(identifier != null) {
			getRecord(identifier);
			return;
		}
		List<Thread> threads = new ArrayList<>();
		for(int i = 0; i < numThreads; i++) {
			HarvestRunner harvestRunner = new HarvestRunner();
			harvestRunners.add(harvestRunner);
		}

		String resumptionToken = "init";
		int batchCount = -1;
		try {
			while (!resumptionToken.isEmpty()) {
				if(++batchCount >= numThreads) { batchCount = 0; }
				String query;
				if (resumptionToken.equals("init")) {
					query = String.format("verb=ListIdentifiers&set=%s&metadataPrefix=%s",
						URLEncoder.encode(set, "UTF-8"),
						URLEncoder.encode(metadataPrefix, "UTF-8"));
				} else {
					query = String.format("verb=ListIdentifiers&resumptionToken=%s",
						URLEncoder.encode(resumptionToken, "UTF-8"));
				}

				URL url = new URL(serviceUrl + "?" + query);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setUseCaches(true);
				connection.setRequestProperty("Connection", "Keep-Alive");
				InputStream is = connection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				is.close();
				InputSource inputSource = new InputSource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
				InputSource inputSource1 = new InputSource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
				InputSource inputSource2 = new InputSource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));

				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				xpath.setNamespaceContext(new OAINamespaceContext());
				XPathExpression resumptionExpression = xpath.compile("//oai:resumptionToken/text()");
				XPathExpression identifiersExpression = xpath.compile("//oai:header[not(@status) or @status != 'deleted']/oai:identifier/text()");
				XPathExpression deletedIdentifiersExpression = xpath.compile("//oai:header[@status = 'deleted']/oai:identifier/text()");
				List<TinyNodeImpl> identifiers = (ArrayList<TinyNodeImpl>) identifiersExpression.evaluate(inputSource, XPathConstants.NODESET);
				List<TinyNodeImpl> deletedIdentifiers = (ArrayList<TinyNodeImpl>) deletedIdentifiersExpression.evaluate(inputSource1, XPathConstants.NODESET);
				harvestRunners.get(batchCount).addIdentifiers(identifiers);
				harvestRunners.get(batchCount).addDeletedIdentifiers(deletedIdentifiers);
				numberOfRecords += identifiers.size();
				resumptionToken = resumptionExpression.evaluate(inputSource2).trim();
				this.onResumptionToken(resumptionToken);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.onHarvestComplete(numberOfRecords);
		for(HarvestRunner harvestRunner : harvestRunners) {
			Thread t = new Thread(harvestRunner);
			t.start();
			threads.add(t);
		}

		for(Thread t : threads) {
			t.join();
		}
	}


	private class HarvestRunner implements Runnable {
		private List<TinyNodeImpl> identifiers = new ArrayList<>();
		private List<TinyNodeImpl> deletedIdentifiers = new ArrayList<>();

		@Override
		public void run() {
			onRunnerStart(identifiers.size());
			Iterator<TinyNodeImpl> deletedIt = deletedIdentifiers.iterator();
			while(deletedIt.hasNext()) {
				TinyNodeImpl identifier = deletedIt.next();
				try {
					handleDeletedRecord(identifier.getStringValue().trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
				deletedIt.remove();
			}

			Iterator<TinyNodeImpl> it = identifiers.iterator();
			while (it.hasNext()) {
				TinyNodeImpl identifier = it.next();
				try {
					getRecord(identifier.getStringValue().trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
				it.remove();
			}
			onRunnerDone();
		}

		public void addIdentifiers(List<TinyNodeImpl> identifiers) {
			this.identifiers.addAll(identifiers);
		}

		public void addDeletedIdentifiers(List<TinyNodeImpl> deletedIdentifiers) {
			this.deletedIdentifiers.addAll(deletedIdentifiers);
		}
	}

	public abstract void onHarvestComplete(int numberOfRecords);
	public abstract void onRunnerStart(int amount);
	public abstract void onRunnerDone();
	public abstract void onResumptionToken(String resumptionToken);
	public abstract void getRecord(String identifier) throws Exception;
	public abstract void handleDeletedRecord(String identifier) throws Exception;
}
