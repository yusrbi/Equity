// File: Edge.java
package graph;
/**
 * This abstract class specifies a generic edge. 
 * Each edge has an id, a source and a target. It also has a weight
 * function, which is left unspecified, to be determined by later
 * implementations. 
 */
public abstract class  Edge {
	public static enum TYPE {
		SIMILAR_SURFACE(0), SAME_ROW(1), SAME_COLUMN(2), HEADER_CELL(3), MENTION_CANDIDATE(4), CANDIDATE_CANDIDATE(5), ANCHOR_EDGE(6);
	    private int val;
	    TYPE(int val) {
	        this.val = val;
	    }
	    public int getVal() {
	        return val;
	    }
	    public static int getCount(){
	    	return 7;
	    }
	} 
	protected TYPE type; 
	protected static double[] hyper_param; 
    public static double[] getHyper_param() {
		return hyper_param;
	}

	public static void setHyper_param(double[] hyper_param) {
		Edge.hyper_param = hyper_param;
	}

	/**
     *  An edge counter, to ensure uniqueness.
     */
    private static int counter = 0;	

    /** 
     * The unique id of the edge
     */
    protected int id;

    /**
     * The target node of the edge
     */
    protected Node target;
    
    

   /**
    *  Constructor with no id. The id is guaranteed to be unique if
    *  only this constructor is used.
    */
    public Edge( Node target, TYPE type)
    {
    	this.type = type;
	    this.target = target;
	    this.id = counter++;
    }

   /**
    *  Constructor with id. The id of edges is not necessarily unique.
    *  This method is useful for checking edge equality.
    */
    public Edge( Node target, int id, TYPE type)
    {
    	this.target = target;
	    this.id = id;
	    this.type = type; 
    }

   /**
    *  Copy Constructor.
    */
    public Edge(Edge e)
    {

	    this.target = e.target;
	    this.id = e.id;
    }
    /** 
     * Return the edge's id 
     */
 
    public int id()
    {
	    return id;
    }
    

    /**
     * Returns the target node of the edge
     */
    public Node target()
    {
	    return target;
    }

    /**
     * Returns true if two edge objects are equal. 
     * Edges are uniquely identified by the three fields id,source and 
     * target.   
     */
    public boolean equals(Object o)
    {
	    if (!(o instanceof Edge)) return false;
	    Edge e = (Edge) o;	
	    
	    return (id == e.id  && target == e.target);
    }
    
    /**
     * Returns the weight of the edge
     */
    public abstract double weight();

    /**
     * String representation
     */
    public String toString()
    {
	    return "(" + id +") "+"-->" +target + "; " + weight() ;
    }

	public static void reset_counter() {
		counter =0;
		
	}

	public static long get_counter() {
		
		return counter;
	}

	public abstract void rescale();
   

}
