import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graph.GraphAlgorithms;
import loader.DocumentLoader;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

public class GraphAlgorithmTest {
	private static Logger slogger_ = LoggerFactory.getLogger(GraphAlgorithmTest.class);
	
	public static  void main(String[] arg){
		//0,7,3,1,4,8,9,6,2,5
		Vector vector = new DenseVector(10);
		vector.set(0, -6);
		vector.set(1, 2);
		vector.set(2, 10);
		vector.set(3, 1);
		vector.set(4, 3);
		vector.set(5, 15);
		vector.set(6, 7);
		vector.set(7, 0);
		vector.set(8, 5);
		vector.set(9, 6);
		
		GraphAlgorithms.order = new LinkedList<Integer>();
		GraphAlgorithms.order(vector);
		
	GraphAlgorithms.reorder(vector);
		
		
		vector.set(7, 15);
		vector.set(9, 7);
		//0,3,1,4,8,6,9,2,7,5
		GraphAlgorithms.reorder(vector);
		
		
		Matrix w = new DenseMatrix(2,2);
		w.set(0,0, 0.2);
		w.set(0,1, 0.8);
		w.set(1,0, 0.5);
		w.set(1,1, 0.5);
		
		Vector teleport = new DenseVector(2);
		teleport.set(0,0);
		teleport.set(1,1);
		Vector result = GraphAlgorithms.RWR(w, teleport,0.5, 100, 0);
		slogger_.info(result.toString());
		
		
		
	}

}
