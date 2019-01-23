package executer;

import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;

public class Experiment {
	public SETTINGS_ID settings;
	public EXPERIMENT_ID experiment;
	public String id ;
	public Experiment(SETTINGS_ID settings, EXPERIMENT_ID exp, String id){
		this.settings = settings;
		this.experiment = exp;
		this.id = id;
	}

	public boolean isFull(){
		switch(experiment){
		case full_src:
		case full_dest:
			return true;
		case part_src:
		case part_dest_cohen_p:
		case part_dest_orig:
		case part_dest_cohen:
			return false;
		default:
			return true;
				
		}
	}
	public boolean isSource(){
		switch(experiment){
		case full_src:
		case part_src:		
			return true;
		case full_dest:
		case part_dest_cohen_p:
		case part_dest_orig:
		case part_dest_cohen:
			return false;
		default:
			return true;
				
		}
	}
}
