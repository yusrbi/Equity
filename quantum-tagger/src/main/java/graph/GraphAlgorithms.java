package graph;



import java.util.LinkedList;


import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;


public class GraphAlgorithms {

	public static LinkedList<Integer> order =  null;
	////////////////////////////////////Main Method/////////////////////////////////////
	public static Vector RWR(Matrix w, Vector teleport,
			double rwr_gamma, int max_itr, double alpha) {
		order = new LinkedList<Integer>();
		Vector r_note = new DenseVector(teleport);		
		r_note = r_note.scale(1-rwr_gamma);
		Vector s_temp = new DenseVector(r_note.size());
		Vector s = new DenseVector(teleport);
		for(int i=0; i<max_itr ; i++){	//max_itr
			/**
		     * <code>y = A<sup>T</sup>*x</code>
		     * 
		     * @param x
		     *            Vector of size <code>A.numRows()</code>
		     * @param y
		     *            Vector of size <code>A.numColumns()</code>
		     * @return y
		     */
			w.transMult(rwr_gamma, s ,s_temp); // trans multiply: W_transpose * S
			s= s_temp.copy();
			s = s.add(r_note);
//			if((i+1)%20 ==0 && converges(s)){
//				System.out.println("iteration: " + i);
//				break;
//			}		
		}
		return s;
	}
	
	//////////////////////////////////////END of USED CODE ////////////////////////////////
	
	public static Vector RWR_NOT_USED(Matrix w, Vector teleport,
			double rwr_gamma, int max_itr, double alpha) {// tested
		order = new LinkedList<Integer>();
		//use alph to make the matrix fully connected, hence irreducible 
//		w = w.scale(alpha);
//		Matrix  e = new DenseMatrix(w.numRows(), w.numColumns());
//		init(e,(((double)1)/((double)w.numRows())));
//		w = w.add(e.scale(1-alpha));
//		e = null;		
		Vector r = new DenseVector(teleport);		
		r = r.scale(1-rwr_gamma);
		Vector r_temp = new DenseVector(r.size());
		Vector s = new DenseVector(r);
		for(int i=0; i< max_itr; i++){
			//w.mult(rwr_gamma, r,r_temp); //TODO, why not to take the transpose here--> it is symetric anyway--> no it is not if normalized ??!
			w.transMult(rwr_gamma, r,r_temp); // trans multiply
			// R_transpose * W
			//r = r_temp.copy();
			s = s.add(r);
			r = s.copy();
			if((i+1)%20 ==0 && converges(s)){
				break;
			}
		//	slogger_.info("Iteration: " + i );
		}
		return s;
	}
	
	public static Vector RWR_directed_variant(Matrix w, Vector teleport, Vector d_0,
			double rwr_gamma, int max_itr, double alpha) {
		order = new LinkedList<Integer>();
		
		Vector r = new DenseVector(teleport);		
		r = r.scale(1-rwr_gamma);
		Vector r_temp = new DenseVector(r.size());
		Vector s = new DenseVector(r);
		Vector d =  new DenseVector(d_0);
		
		for(int i=0; i< max_itr; i++){
			w.transMult(rwr_gamma, r,r_temp); // trans multiply			
			r = r_temp.copy();
			for(int j=0; j< r.size(); j++){
				if(r.get(j)>0){
					if(d.get(j) == 0){
						d.set(j,i+1);
					}else{
						d.set(j, Math.min(d.get(j), i+1));
					}					
					if(d.get(j) < i+1){
						r.set(j,0);
					}
				}
			}
			s = s.add(r);
//			if((i+1)%20  ==0 && converges(s)){
//				break;
//			}
		}
		return s;
	}
	
	

	private static boolean converges( Vector s) {
		if(order.size() == s.size()){
			//reorder using bubble sort 
			int changes = reorder(s);
			if(changes==0)
				return true;
		}else{
			order(s);
		}				
		return false;
	}
	
	public static int reorder( Vector s) {//tested
		int  count =0, pos=0, crnt=0;
		for(int i=0; i< order.size()-1; i++){//insertion sort on a linked list
			crnt =order.get(i+1);
			pos = i+1;
			while(pos >0 && s.get(order.get(pos-1)) < s.get(crnt) ){
				pos--;
				
			}
			if(pos != i+1){
				count++;
				order.remove(i+1);
				order.add(pos, crnt);
			}
				
		}

	//	slogger_.info(order.toString());
		return count;
	}

//	private static int reorder( Vector s) {
//		int temp =0, count =0;// bubble sort on a linked list --> bad
//		for(int i=0; i< order.size(); i++){
//			for(int j =i+1; j < order.size(); j++){
//				if(s.get(order.get(i)) < s.get(order.get(j))){
//					temp = order.get(i);
//					order.set(i, order.get(j));
//					order.set(j, temp);
//					count ++;
//				}
//			}
//		}
//		return count;
//	}

	public static void order( Vector s) {//tested
		int pos=0;
		double val1=0;
		for(int i=0; i < s.size(); i++){
			val1= s.get(i);
			pos =0;
			while(pos< order.size() && s.get(order.get(pos)) > val1){
				pos++;
			}
			order.add(pos,i);
		}		
		//slogger_.info(order.toString());
	}

}
