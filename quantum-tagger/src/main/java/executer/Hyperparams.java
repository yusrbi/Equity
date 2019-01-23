package executer;

import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;

public class Hyperparams {

	public static enum EXPERIMENT_ID {
			full_src(0), full_dest(1), part_src(2), part_dest_cohen_p(3), part_dest_orig(4), part_dest_cohen(5);
			private int value;
			EXPERIMENT_ID (int value){
				this.value = value;
			}
			public int getValue(){
				return value;
			}
			
			};
		
			public static enum SETTINGS_ID { DEFAULT(0), NO_TABLE_STRUCTURE(1), NO_SAME_SURFACE(2), 
				NO_CAND_CAND(3), NO_STRCT_CAND(4), NO_STRCT_SAMES(5), NO_CAND_SAMES(6), NONE(7);
				private int value;
				SETTINGS_ID(int value){
					this.value= value;
				}
				public int getValue(){
					return this.value;
				}
			}
			
			public static final double [][] hyperpararms = {//selected 
					{0.16,	0.16,	0.16,	0.16,	0.16,	0.16},
					//{0.0021439385,	0.5024741554,	0.4895596056,	0.0015857075,	0.0000082687,	0.0021683243}, /* full starting from the source at which all configurations of hyper params are the same*/
					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
					{0.0285824913,	0.1713885687,	0.2086288838,	0.0120955493,	0.216108702,	0.273195805}, ////24 
				//23	{0.042591965,	0.8238787765,	0.0138329695,	0.0136594215,	0.0729906139,	0.0330462537}, /* part starting form the source */
					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
					};
			//To come
//			public static final double [][] hyperpararms = {//22
//					{0.0021439385,	0.0024741554,	0.9895596056,	0.0015857075,	0.0020682687,	0.0021683243}, /* full starting from the source at which all configurations of hyper params are the same*/
//					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//					{0.1312847915,	0.0089609063,	0.1462929513,	0.0961049688,	0.3884901121,	0.2288662699}, /* part starting form the source */
//					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//					};
			
//			public static final double [][] hyperpararms = {//21
//					{0.2005655489,	0.0147455265,	0.2208433853,	0.5111194423,	0.0303753225,	0.0223507745}, /* full starting from the source at which all configurations of hyper params are the same*/
//					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//					{0.219032868,	0.0074446795,	0.032591314,	0.1027111041,	0.4026991373,	0.235520897}, /* part starting form the source */
//					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//					};
//			public static final double [][] hyperpararms = {//19
//					{0.0285824913,	0.1713885687,	0.2086288838,	0.0120955493,	0.216108702,	0.273195805	}, /* full starting from the source at which all configurations of hyper params are the same*/
//					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//					{0.0606979623,	0.2799709238,	0.2511932025,	0.0269466837,	0.0610733598,	0.2201178679}, /* part starting form the source */
//					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//					};
//			
//			public static final double [][] hyperpararms = {//20
//					{0.0067013709,0.0396723005,0.8746762054,0.0416109626,0.0189251716,0.018413989	}, /* full starting from the source at which all configurations of hyper params are the same*/
//					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//					{0.1690426412,	0.243408976,	0.0288789587,	0.070556227,	0.3242754137,	0.1638377834}, /* part starting form the source */
//					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//					};
//			public static final double [][] hyperpararms = {//17
//					{0.08,	0.20,	0.20,	0.11,	0.25,	0.15}, /* full starting from the source at which all configurations of hyper params are the same*/
//					{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//					{0.04,	0.04,	0.04,	0.05,	0.80,	0.03}, /* part starting form the source */
//					{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//					{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//					{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//					};
//	public static final double [][] hyperpararms = {
//			{0.1285824913,	0.0713885687,	0.0986288838,	0.1120955493,	0.416108702,	0.173195805	}, /* full starting from the source at which all configurations of hyper params are the same*/
//			{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//			{0.0007464163,	0.0010406655,	0.0009827392,	0.0007648367,	0.0006247981,	0.9958405443}, /* part starting form the source */
//			{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//			{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//			{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//			};
//	public static final double [][] hyperpararms = {//16
//			{0.2024182123,	0.1767827673,	0.119262856,	0.0965917027,	0.4010871263,	0.0038573355}, /* full starting from the source at which all configurations of hyper params are the same*/
//			{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//			{0.0334306472,	0.0219921221,	0.0219556302,	0.0120812707,	0.8718591934,	0.0386811365}, /* part starting form the source */
//			{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//			{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//			{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//			};
//	public static final double [][] hyperpararms = {//15,
//			{0.2024182123,	0.1767827673,	0.119262856,	0.0965917027,	0.4010871263,	0.0038573355}, /* full starting from the source at which all configurations of hyper params are the same*/
//			{0.0115213517,	0.0119383456,	0.9527803668,	0.0078182143,	0.0140625947,	0.0018791269}, /*full starting from the destination*/
//			{0.1096238305,  0.0199853533,   0.1503359355,   0.0228488527,   0.6970886627,   0.0001173654}, /* part starting form the source */
//			{0.1176980722,	0.0174667367,	0.067016794,	0.6939839386,	0.0771197833,	0.0267146752}, /* (avg) part starting from the destination cohen_plus */
//			{0.3576849555,	0.0023778482,	0.177455116,	0.2536850575,	0.0053815744,	0.2034154484}, /*part starting from the destination and ending at the source*/
//			{0.2524182123,	0.0367827673,	0.099262856,	0.1865917027,	0.2810871263,	0.1438573355}, /* part cohen constructions (aggregating at the anchor nodes, infinity is not considered*/ 
//			};
//	
	public static final double [][] settings = {
			{1,1,1,1,1,1}, //DEFAULT-all in
			{1,0,0,0,1,1}, //no table strcture
			{0,1,1,1,1,1}, // no same-surface
			{1,1,1,1,1,0}, // no candidate-candidate 
			{1,0,0,0,1,0}, // NO_STRCT_CAND(4)
			{0,0,0,0,1,1},// NO_STRCT_SAMES(5)
			{0,1,1,1,1,0},//NO_CAND_SAMES(6)
			{0,0,0,0,1,0}, //NONE(7) 
	};
	public static double[] getHyperparams(EXPERIMENT_ID exp_id, SETTINGS_ID settings2) {
		double[] new_params = null;
		if(settings2.equals(SETTINGS_ID.DEFAULT))
			return hyperpararms[exp_id.getValue()];
		else{
			new_params = hyperpararms[exp_id.getValue()].clone();
			for(int i=0; i< new_params.length; i++){
				new_params[i] *= settings[settings2.getValue()][i];
			}
			double sum = 0;
			for(int i=0; i< new_params.length; i++){
				sum+=  new_params[i];
			}
			for(int i=0; i< new_params.length; i++){
				new_params[i] /= sum;
			}
		}
		return new_params;
	}
	
	
}
