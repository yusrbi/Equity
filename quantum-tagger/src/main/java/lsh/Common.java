package lsh;

import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;

public class Common {
	// for n-grams of names
	private static HashMap<String, Integer> shingles = new HashMap<String, Integer>();
	private static int keys = 1;
//	private static int k = 2;
//	private static int l = 10;
//	
//	/*
//	 * hashcode a block of k values
//	 */
//	private static int[] getHashCode(int[] signature) {
//		int[] hashCodes = new int[l];
//		for (int i = 0; i < l; i++) {
//			hashCodes[i] = 0;
//			for (int j = i * k; j < (i + 1) * k; j++)
//				hashCodes[i] += signature[j];
//		}
//		return hashCodes;
//	}
	
	
	public static Counter getCounterAtTokenLevel(String s) {
		// s = s.replaceAll("['-()*&^%$#@!;]","");
		OpenIntIntHashMap entries = new OpenIntIntHashMap();
		// Counter counter = new Counter(s);
		int totalCount = 0;
		StringTokenizer tokenizer = new StringTokenizer(s);
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
//			if (tok.length() < 3) {
				// if tok has less than 3 characters
				// then take tok as a shingle
				String shingle = tok;
				Integer key = shingles.get(shingle);
				if (key == null) {
					// slogger_.info(shingle + " : " + keys);
					shingles.put(shingle, keys);
					// counter.incrementCount(keys);
					entries.put(keys, entries.get(keys) + 1);
					totalCount++;
					keys++;
				} else {
					// counter.incrementCount(key.intValue());
					entries.put(key.intValue(), entries.get(key.intValue()) + 1);
					totalCount++;
				}
				continue;
//			}
//			for (int i = 0; i < tok.length() - 2; i++) {
//				// String shingle = tok; //"";
//				String shingle = "";
//				for (int j = i; j < Math.min(tok.length(), i + 3); j++)
//					shingle += tok.charAt(j);
//				shingle = shingle.toLowerCase();
//				Integer key = shingles.get(shingle);
//				if (key == null) {
//					// slogger_.info(shingle + " : " + keys);
//					shingles.put(shingle, keys);
//					// counter.incrementCount(keys);
//					entries.put(keys, entries.get(keys) + 1);
//					totalCount++;
//					keys++;
//				} else {
//					// counter.incrementCount(key.intValue());
//					entries.put(key.intValue(), entries.get(key.intValue()) + 1);
//					totalCount++;
//				}
//			}
		}
		// Counter counter = new Counter(s);
		// insert into counter from hashmap here...

		// return new Counter(s, entries, totalCount);

		int[] keys = new int[entries.size()];
		IntArrayList l = entries.keys();
		for (int i = 0; i < entries.size(); i++)
			keys[i] = l.get(i);

		// sort keys arrays
		Arrays.sort(keys);
		int[] vals = new int[keys.length];
		for (int i = 0; i < keys.length; i++)
			vals[i] = entries.get(keys[i]);

		entries = null;
		return new Counter(s, keys, vals, totalCount);
	}
	
	public static Counter getCounter(String s) {
		// s = s.replaceAll("['-()*&^%$#@!;]","");
	  String str = s.replaceAll("_", " ");
		OpenIntIntHashMap entries = new OpenIntIntHashMap();
		// Counter counter = new Counter(s);
		int totalCount = 0;
		StringTokenizer tokenizer = new StringTokenizer(str);
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
			if (tok.length() < 3) {
				// if tok has less than 3 characters
				// then take tok as a shingle
				String shingle = tok;
				Integer key = shingles.get(shingle);
				if (key == null) {
					// slogger_.info(shingle + " : " + keys);
					shingles.put(shingle, keys);
					// counter.incrementCount(keys);
					entries.put(keys, entries.get(keys) + 1);
					totalCount++;
					keys++;
				} else {
					// counter.incrementCount(key.intValue());
					entries.put(key.intValue(), entries.get(key.intValue()) + 1);
					totalCount++;
				}
				continue;
			}
			for (int i = 0; i < tok.length() - 2; i++) {
				// String shingle = tok; //"";
				String shingle = "";
				for (int j = i; j < Math.min(tok.length(), i + 3); j++)
					shingle += tok.charAt(j);
				shingle = shingle.toLowerCase();
				Integer key = shingles.get(shingle);
				if (key == null) {
					// slogger_.info(shingle + " : " + keys);
					shingles.put(shingle, keys);
					// counter.incrementCount(keys);
					entries.put(keys, entries.get(keys) + 1);
					totalCount++;
					keys++;
				} else {
					// counter.incrementCount(key.intValue());
					entries.put(key.intValue(), entries.get(key.intValue()) + 1);
					totalCount++;
				}
			}
		}
		// Counter counter = new Counter(s);
		// insert into counter from hashmap here...

		// return new Counter(s, entries, totalCount);

		int[] keys = new int[entries.size()];
		IntArrayList l = entries.keys();
		for (int i = 0; i < entries.size(); i++)
			keys[i] = l.get(i);

		// sort keys arrays
		Arrays.sort(keys);
		int[] vals = new int[keys.length];
		for (int i = 0; i < keys.length; i++)
			vals[i] = entries.get(keys[i]);

		entries = null;
		return new Counter(s, keys, vals, totalCount);
	}
	
	
}
