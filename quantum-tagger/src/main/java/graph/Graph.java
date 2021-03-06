// File: Graph.java
package graph;
import java.util.*;

import edu.stanford.nlp.util.Pair;
import graph.Edge.TYPE;
import knowledgebase.Candidate;
import no.uib.cipr.matrix.Vector;

/**
 * This abstract class defines a set of variables and implements some methods
 * that are common in every implementation of a directed multigraph object. 
 * It also specifies the minimum interface that a graph implementation should
 * support. Each graph is described by the number of its nodes and by the
 * set of its weighted edges. The number of nodes is determined when the
 * graph is instantiated, and does not change during the lifetime of the
 * graph. The nodes of the graph are labeled by integers 0,1,..,(n-1),
 * where n is the number of nodes. The set of edges of a graph is not fixed.
 * Initially, a graph contains no edges at all. A graph implementation
 * should support methods for inserting and removing edges. Multiple edges
 * (i.e., from the same source to the same target node) are allowed.
 */
public abstract class Graph {

    /**
     * The number of nodes of the graph.
     */
    protected int nNodes;


    /**
     * Class constructor.
     */
    public Graph(int nodes) {
        nNodes = nodes;
    }

    /**
     * Returns the number of nodes of the graph.
     */
    public abstract  int getNNodes();

    /**
     * This is method prototype adds safely an edge to the graph, when the
     * edge is given as an object. If an edge connecting the same 
     * source node with the same target *and* has the same id exists, 
     * the edge is not added to the graph. An  IndexOutOfBoundsException 
     * is thrown if the source or target  of the edge 
     * do not correspond to valid node numbers.

     * @param e
     *            the edge to be added.
     * @return true if the edge is successfully added to the graph, and false
     *         otherwise.
     */
   // public abstract boolean addEdge(Edge e);

    /**
     * This is method prototype adds an edge to the graph, when the
     * edge is given as an object. This method does not check if the
     * edge already exists in the graph. This is to be used for efficiency 
     * when it is a priori known that the edge to be inserted
     * does not already exist. An  IndexOutOfBoundsException 
     * is thrown if the source or target  of the edge 
     * do not correspond to valid node numbers.

     * @param e
     *            the edge to be added.
     * @return true if the edge is successfully added to the graph, and false
     *         otherwise.
     */
  //  protected abstract boolean addEdgeUnsafely(Edge e);

     /**
     * This is method prototype returns the edges between a 
     * source node and a target node.
     * The edges are returned in the form of a linked list of edge objects. 
     * If there are no edges between the source and target nodes, an
     * empty list is returned.   An IndexOutOfBoundsException is thrown if 
     * the source or target of the edge  do
     * not correspond to valid node numbers.

     * @param source
     *            the source node.
     * @param target
     *            the target node.
     *            
     * @return A linked list containing the edges between the source and
     * the target.
     */
   
    public abstract LinkedList<Edge> getEdges(int source);
    
     /**
     * This is method prototype returns the edges starting from a
     * source node.
     * The edges are returned in the form of a linked list of edge objects. 
     * If there are no edges adjacent to the the source node, an
     * empty list is returned.   An IndexOutOfBoundsException is thrown if 
     * the source node does not correspond to a valid node number.

     * @param source
     *            the source node.
     * @return A linked list containing the edges adjacent to the source.
     */
   
   // public abstract LinkedList<Edge> getEdges(int source, int target);
        
    /**
     * This method removes an edge given as an object. The removal happens as follows: 
     * if the graph contains  an edge with the same id
     * from source to target node, then this edge is removed and true 
     * is returned. Otherwise, the graph is not modified and false
     * is returned. An IndexOutOfBoundsException is thrown if 
     * the source or target of the edge  do not correspond to
     * valid node numbers
     * 
     * @param e
     *             the edge to remove.
     * @return      true if the edge is succesfully removed
     */
  //  public abstract boolean removeEdge(Edge e);
    
    /**
     * This method removes all edges in the graph.
     */
  //  public abstract void removeAllEdges();


    /**
     * If G has the same number of nodes as this graph then the method copies this graph to
     * graph G. Otherwise, it does nothing. G may be using a different internal
     * representation than this graph. For efficiency, this method should use
     * only the unsafe version of the edge addition method (of G).
     * 
     * @param G
     *            the graph to copy this graph to.
     * @return graph G
     */
   // public abstract Graph toAltGraphRepr(Graph G);

	public abstract void addMentionNode(String mention, Set<Candidate> candidates) ;

	public abstract void addEdge(String semanticTargetId, String semanticTargetId2,
			double weight, Edge.TYPE type) ;
	
	public abstract void addEdge(Node node1, Node node2, double weight, Edge.TYPE type) ;

	public abstract  double startRWRFromCandidates(String semanticTargetId);
	public abstract  double[] startRWRFromMention(String mentionId);

	public abstract String getPythonEdgeList(boolean write_potential_edges) ;

	public abstract void normalizeEdgeWeights();

	public abstract int getNodeId(String semanticTargetIdForGraph) ;

	public abstract void addMentionNodeAggregateCandidates(String string, Set<Candidate> candidates) ;

	public abstract void removeCycles() ;

	public abstract void expandEdges();

	public abstract void addPotentialEdge(String string, String string2, double similarity, TYPE similarSurface) ;

	public abstract void addCandidateCandidateEdgeWeight(String semanticTargetIdForGraph, String semanticTargetIdForGraph2,
			double sim);

	public abstract long getNEdges();

	public abstract void adjastEdgeWeightsUsingHyperparams();

	public abstract void addAnchorNodes();

	public abstract Pair<Double,Integer> startRWRFromCandidatesDirected(String nodeId, Vector all_candidates_teleport) ;

	public abstract void normalizeEdgeWeightsByConstant();

	public abstract void renormalize();
		

}