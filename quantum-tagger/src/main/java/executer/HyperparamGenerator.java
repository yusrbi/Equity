package executer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import graph.Edge;
import utils.FileUtils;



public class HyperparamGenerator {

	private String experiemnt_id;
	private static int count =0;
	private double[] hyper_param; 
	private List<double[]>  hyperparams_ls;
	private Random rand = null;
	private static int start =0;
	private HyperparamGenerator(){
		rand = new Random();
		rand.setSeed(777);//11
	}
	private static HyperparamGenerator generator =null;
	public static HyperparamGenerator getHyperparamGenerator(){
		if(generator==null)
			generator = new HyperparamGenerator();
		return generator;
	}
	
	public String getExperiemnt_id() {
		return experiemnt_id;
	}

	public double[] getHyper_param() {
		return hyper_param;
	}

	public void  generateNextHyperparams(){
		count++;
		experiemnt_id= String.format("Hyperparams:", count);
	}

	/**
	 * Generate n number of random hyperprams 
	 * at which each hyperparam is bewteen 0 and 1 and the sum of all the hyperparams is 1
	 * @param count
	 */
	public void generate(int count) {
		
		hyperparams_ls= new ArrayList<double[]>(count);
		double[] new_params;
		for(int i =0; i<count; i++ ){
			new_params = generate_new();//generate_new();
			hyperparams_ls.add(new_params);
		}
		return;
	}

	private double[] generate_new() {
		double[] params = new double[6];
		double sum=0.0;		
		params[start] = rand.nextDouble();
		for(int i=0; i<6;i++){
			if(i==start)
				continue;
			params[i] = rand.nextDouble();
			sum += params[i];
		}
		for(int i=0; i<6;i++){
			if(i==start)
				continue;
			params[i] = (params[i]/sum)*(1-params[start]);
		}
		start++;
		start = start%6;
		return params;
	}
	private double[] generate_new_guided() {
		double[] params = new double[6];
		double sum=0.0;
		double weight = 0.3 + rand.nextDouble() *0.3; // sample a number between 0.3, 0.6 inclusive
		//sample the same row, same column, and mentiion-candidate
		params[Edge.TYPE.SAME_ROW.getVal()] = rand.nextDouble();
		params[Edge.TYPE.SAME_COLUMN.getVal()] = rand.nextDouble();
		params[Edge.TYPE.MENTION_CANDIDATE.getVal()] = rand.nextDouble();
		
		sum = params[Edge.TYPE.SAME_ROW.getVal()] 
				+ params[Edge.TYPE.SAME_COLUMN.getVal()] 
						+ params[Edge.TYPE.MENTION_CANDIDATE.getVal()];
		
		params[Edge.TYPE.SAME_ROW.getVal()] =( params[Edge.TYPE.SAME_ROW.getVal()] /sum ) * weight;
		params[Edge.TYPE.SAME_COLUMN.getVal()] = (params[Edge.TYPE.SAME_COLUMN.getVal()] /sum) * weight;
		params[Edge.TYPE.MENTION_CANDIDATE.getVal()] = (params[Edge.TYPE.MENTION_CANDIDATE.getVal()] /sum) * weight;
		
				
		//sample the other three 
		params[Edge.TYPE.SIMILAR_SURFACE.getVal()] = rand.nextDouble();
		params[Edge.TYPE.HEADER_CELL.getVal()] = rand.nextDouble();
		params[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = rand.nextDouble();
		
		sum = params[Edge.TYPE.HEADER_CELL.getVal()]
				+ params[Edge.TYPE.SIMILAR_SURFACE.getVal()] 
				+ params[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()];
		
		params[Edge.TYPE.SIMILAR_SURFACE.getVal()] =( params[Edge.TYPE.SIMILAR_SURFACE.getVal()] /sum ) * (1-weight);
		params[Edge.TYPE.HEADER_CELL.getVal()] = (params[Edge.TYPE.HEADER_CELL.getVal()] /sum) * (1-weight);
		params[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = (params[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] /sum) * (1-weight);
		
		return params;
	}

	public List<double[]> getHyperparams() {
		
		return hyperparams_ls;
	}

	public void writeHyperparamsToFile(String file_name) throws IOException {
		StringBuilder fileContent = new StringBuilder();
		for(double [] hyperparams : hyperparams_ls){
			fileContent.append(toString(hyperparams)+"\n");
		}
		FileUtils.writeFileContent(new File(file_name), fileContent.toString());
		return;
	}
	private static String toString(double[] hyperparams) {
		StringBuilder str = new StringBuilder();
		for(double hyperparam : hyperparams){
			str.append(String.valueOf(hyperparam)+",");
		}
		return str.toString();
	}
	
}
