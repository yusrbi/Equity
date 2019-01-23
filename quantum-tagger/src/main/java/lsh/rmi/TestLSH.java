package lsh.rmi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import lsh.Common;
import lsh.LSHTable;


public class TestLSH {
	public static void main(String args[]) throws Exception {
		Set<String> concepts = new HashSet<String>();
		try{
			BufferedReader reader = new BufferedReader( new FileReader("/home/yusra/workspace/quantum-tagger/data/abstraction_16.list"));
			String line;
			while ((line = reader.readLine())!= null){
				String concept = URLDecoder.decode(line.trim().replace('_', ' '),"utf-8");		
				concepts.add(concept);
			}
			
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Loaded: " + concepts.size() + " strings.");
		// init lsh
		LSHTable lsh = new LSHTable(2, 15, 100, 999999999, 0.5);
		// load data
		int counter = 0;
		for(String concept: concepts) {
			if(++counter % 10000 == 0)
				System.out.println(counter);
			lsh.put(Common.getCounter(concept));
		}
		System.out.println("Done!");
		// test
		Scanner in = new Scanner(System.in);
		for(int i =0; i< 100; i++){
			String input = in.nextLine();
			List<String> res = lsh.deduplicate(Common.getCounter(input));
			System.out.println("Got: " + res.size());
			for(String output: res)
				System.out.println(output);
		}
		in.close();
	}
}
