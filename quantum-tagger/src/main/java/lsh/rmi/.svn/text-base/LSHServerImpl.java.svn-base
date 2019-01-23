package lsh.rmi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lsh.Common;
import lsh.LSHTable;

public class LSHServerImpl implements LSHServer {
	private LSHTable lsh;
	private Map<String, List<String>> predicate2entityPairs;
//	private boolean expandPredicateViaSimilarWords = true;
//	private double thresholdToExpandPredicate = 0.5;
//	private boolean removeStopwords = false;
	

	public LSHServerImpl() {
		lsh = new LSHTable(2, 8, 100, 999999999, 0.7); // k = 2 is the size of the hash value: hash1(+)hash2 and l = 8 is the number of time we do hashing
		// increase k to increase precision but decrease recall; increase l to increase recall but decrease precision.
		// init
		init();
	}

	// private void init() {
	// System.out.print("Setting up lsh...");
	// String yagoFact = "./data/yago/yagoFacts.tsv";
	// int counter = 0;
	// try {
	// // extract all possible mentions
	// for(String line: Utils.getContent(yagoFact)) {
	// if(++counter % 500000 == 0) {
	// System.out.print(counter + "...");
	// }
	// if(counter == 1)
	// continue; // first line has no content
	// String str[] = line.split("\t");
	// String fact = str[2].substring(1, str[2].length()-1);
	// lsh.put(Common.getCounter(standardize(fact)));
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// System.out.println("Done!");
	// }

	private void init() {
		predicate2entityPairs = new HashMap<String, List<String>>();
		System.out.print("Setting up LSH...");
		String factSource = "./data/entityRelations";
		int counter = 0;
		try {
			// extract all possible mentions
			FileInputStream fis = new FileInputStream(factSource);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader bufReader = new BufferedReader(isr);

			String line;
			while (true) {
				line = bufReader.readLine();
				if (line == "" || line == null)
					break;
//				if (counter == 1)
//					continue; // first line has no content
				String str[] = line.split("\t");
				String fact = str[1];
				if(fact.length() < 2)
					continue;
//				lsh.put(Common.getCounter(standardize(fact)));
				String cleanPredicate = cleanPredicate(fact);
				if(cleanPredicate != null) {
					lsh.put(Common.getCounter(cleanPredicate));
					// update map
					List<String> tmpList = predicate2entityPairs.get(cleanPredicate);
					if(tmpList == null) {
						tmpList = new ArrayList<String>();
						predicate2entityPairs.put(cleanPredicate, tmpList);
					}
					tmpList.add(str[0] + "\t" + str[2]);
					if (++counter % 500000 == 0) {
						System.out.print(counter + "...");
					}
				}
			}

			isr.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	/**
	 * clean the predicate. E.g. ignore too long predicate, etc.
	 * @param predicate
	 * @return
	 */
	private String cleanPredicate(String predicate) {
		String res = predicate.trim();
		if(res.startsWith("'s"))
			return null;
		res = res.replaceAll("\\[\\[", " ");
		res = res.replaceAll("\\]\\]", " ");
//		String[] tokens = res.split(" ");
		if(res.length() > 50)
			return null;
//		/**
//		 * remove stopwords
//		 */
//		if(removeStopwords) {
//			res = "";
//			for(String token: tokens)
//				if(DataStore.isStopWord(token) == false)
//					res += token + " ";
//		}
		return res;
	}
	
	
	
	

	/**
	 * isAffiliatedTo -> is affiliated to
	 * 
	 * @param fact
	 * @return
	 */
	@SuppressWarnings("unused")
	private String standardize(String fact) {
		String res = "";
		for (int i = 0; i < fact.length(); i++) {
			char ch = fact.charAt(i);
			if ('A' <= ch && ch <= 'Z') {
				res += " " + (char) (ch - 'A' + 'a');
			} else {
				res += ch;
			}
		}
		return res;
	}

	@Override
	public Map<String, List<String>> getSimilarName(String name) throws RemoteException {
		System.out.println("query: " + name);
		List<String> queries = new ArrayList<String>();
		queries.add(name);
//		if(expandPredicateViaSimilarWords) {
//			queries.addAll(Utils.getSimilarWords(name, thresholdToExpandPredicate).keySet());
//		}
		
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		for(String query: queries) {
			for(String similarPredicate: lsh.deduplicate(Common.getCounter(query.toLowerCase())))
				res.put(similarPredicate, predicate2entityPairs.get(similarPredicate));
		}
			
		System.out.println("retrieved " + res.size() + " similar predicates.");
		return res;
	}
	
	@Override
	public Map<String, List<String>> getSimilarName(String name, String filter) throws RemoteException {
		System.out.println("query: " + name + "\t filter: " + filter);
		List<String> queries = new ArrayList<String>();
		queries.add(name);
//		if(expandPredicateViaSimilarWords) {
//			queries.addAll(Utils.getSimilarWords(name, thresholdToExpandPredicate).keySet());
//		}
		
//		Set<String> interestingEntities = DataStore.getEntitiesForMention(filter);
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		for(String query: queries) {
			for(String similarPredicate: lsh.deduplicate(Common.getCounter(query.toLowerCase()))) {
				List<String> filPreds = new ArrayList<String>();
				for(String pred: predicate2entityPairs.get(similarPredicate)) {
//					String[] str = pred.split("\t");
//					if(interestingEntities.contains(str[0]) || interestingEntities.contains(str[1]))
						filPreds.add(pred);
				}
				res.put(similarPredicate, filPreds);
			}
		}
		System.out.println("retrieved " + res.size() + " similar predicates.");
		return res;
	}
	
	
	/***
	 * call system command from java
	 */
	public static final class ExecutorTask implements Runnable {
		private String word;
		private String output;
		
		public ExecutorTask(String word) {
			this.word = word;
		}

		@Override
		public void run() {
			try {
				Process process = Runtime.getRuntime().exec("./similar vectors.bin " + word);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s = null;
				while ((s = stdInput.readLine()) != null) {
//				    System.out.println(s);
				    output += s + "\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}
	}
	
}
