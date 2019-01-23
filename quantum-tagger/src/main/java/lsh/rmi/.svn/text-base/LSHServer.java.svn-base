package lsh.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;


public interface LSHServer extends Remote {
  
  public Map<String, List<String>> getSimilarName(String name) throws RemoteException;
  public Map<String, List<String>> getSimilarName(String name, String filter) throws RemoteException;
}
