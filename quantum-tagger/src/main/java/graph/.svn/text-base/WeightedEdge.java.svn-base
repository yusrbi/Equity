// File: WeightedEdge.java
package graph;
/**
 * This class implements the Edge interface. It supports no additional
 * functionality.
 */
public class WeightedEdge extends Edge {

    /**
     * The edge's weight.
     */
    protected double weight;

    /**
     * Class constructor.
     */
    public WeightedEdge(Node target, double weight, TYPE type, boolean re_scale) {
    	super(target, type);
    	if(re_scale)
    		this.weight = weight *  hyper_param[type.getVal()];
    	else
    		this.weight = weight;
        
    }
    
   /**
    * 
    * @param target
    * @param id
    * @param weight
    * @param type
    * @param re_scale in case of adding an edge from a potential edge do not rescale it,
    *  as it is already scaled with the hyperparams
    */
    public WeightedEdge( Node target, int id, double weight, TYPE type, boolean re_scale) {
    	super(target,id, type);
    	if(re_scale)
    		this.weight = weight *  hyper_param[type.getVal()];
    	else
    		this.weight = weight;
    }
    /**
     * Copy constructor.
     */
    public WeightedEdge(WeightedEdge e) {
	super(e);
        this.weight = e.weight;
    }
    /**
     * Returns the weight of the edge
     */
    public double weight() {
        return weight;
    }

	@Override
	public void rescale() {
		if(type.getVal() ==6)
			return;
		this.weight = this.weight * hyper_param[type.getVal()];
		
	}


}
