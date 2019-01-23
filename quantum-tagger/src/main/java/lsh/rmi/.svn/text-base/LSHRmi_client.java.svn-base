package lsh.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public class LSHRmi_client {
	
	private LSHServer server;
	
	public LSHRmi_client() {
		String host = "139.19.52.65";
		try {
			Registry registry = LocateRegistry.getRegistry(host, 52378);
			server = (LSHServer) registry.lookup("LSHServer_" + host);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testQuery(String query) throws Exception {
		Map<String, List<String>> pred2pairEntities = server.getSimilarName(query);
		int counter = 0;
		if (pred2pairEntities != null) {
			for (String pred : pred2pairEntities.keySet()) {
				System.out.println(pred);
				for (String p : pred2pairEntities.get(pred)) {
					System.out.println(p);
					counter++;
				}
				System.out.println("-------------");
			}
		}
		System.out.println(counter);
	}
	

	public static void main(String args[]) throws Exception {
		new LSHRmi_client().testQuery(args[0]);
	}
}
