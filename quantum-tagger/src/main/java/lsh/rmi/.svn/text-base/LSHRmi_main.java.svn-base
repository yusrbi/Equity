package lsh.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;



public class LSHRmi_main {
  public static void main(String args[]) throws Exception {
    String host;
    if(args.length < 1) {
      host = "localhost"; // default
    }
    else {
      host = args[0];
    }
    @SuppressWarnings("unused")
	boolean textRelation = true;
    if(args.length > 1)
    	textRelation = Boolean.parseBoolean(args[1]);
    LSHServer server = null;
//    if(textRelation)
    	server = new LSHServerImpl();
//    else {
//    	System.out.println("Setting parsed-relation server...");
//    	server = new LSHParsedRelationServer();
//    }
    LSHServer stub = (LSHServer) UnicastRemoteObject.exportObject(server, 0);

    // bind the remote object's stub in the registry
    LocateRegistry.createRegistry(52378);
    LocateRegistry.getRegistry(52378).bind("LSHServer_" + host, stub);
    System.out.println("LSHServer started at " + host + "!");
  }
}
