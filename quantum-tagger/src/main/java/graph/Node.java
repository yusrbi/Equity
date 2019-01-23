package graph;

import java.util.LinkedList;
import java.util.List;

public class Node {

	/**
	 * An node counter, to ensure uniqueness.
	 */
	private static int counter = 0;

	private static int psuedo_counter = -1;
	/**
	 * The unique id of the node
	 */
	protected int id;

	protected String label;

	public static enum TYPE {
		MENTION, SEAMANTIC_TARGET, ANCHOR
	};

	protected TYPE type;
	protected List<Edge> Edge_lst;

	// protected List<Edge> fromEdges;

	/**
	 * Constructor with no id. The id is guaranteed to be unique if only this
	 * constructor is used.
	 */
	public Node(String label, TYPE type) {

		this.type = type;
		synchronized (this) {
			if (!label.endsWith("_cand")) // do not count the _cand, they will
											// be removed anyway
				this.id = counter++;
			else
				this.id = psuedo_counter--;
		}

		this.Edge_lst = new LinkedList<Edge>();
		// this.fromEdges = new LinkedList<Edge>();
		this.label = label;
	}

	/**
	 * Return the node's id
	 */

	public int id() {
		return id;
	}

	public TYPE getType() {
		return type;
	}

	/**
	 * Returns true if two Node objects are equal. Node are uniquely identified
	 * by the three fields id,source and target.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Node))
			return false;
		Node n = (Node) o;

		return (id == n.id && type == n.type);
	}

	public String toString() {
		return type + "_" + id;
	}

	public List<? extends Edge> getEdges() {
		return this.Edge_lst;
	}

	public void addToEdge(Edge edge) {
		this.Edge_lst.add(edge);

	}

	/*
	 * public void addFromEdge(Edge edge) { this.fromEdges.add(edge);
	 * 
	 * }
	 */

	public void addEdge(Edge edge) {
		this.Edge_lst.add(edge);
		// this.fromEdges.add(edge);

	}

	/**
	 * This method will normalize the edge weights based on the type of the edge
	 * This is the right way to do it as each type of edge is calculated using a
	 * different technique and then if they are normalized all together it will
	 * not be correct
	 */
	public void normalizeEdgeWeights() {
		double[] sum = new double[Edge.TYPE.getCount()];
		double total_sum = 0.0;
		for (Edge edge : Edge_lst) {
			sum[edge.type.getVal()] += edge.weight();
			if (edge.type != Edge.TYPE.ANCHOR_EDGE)
				total_sum += edge.weight();
		}
		if (total_sum > 0) {
			for (Edge edge : Edge_lst) {
				
				 if (sum[edge.type.getVal()]> 0) {
				 ((WeightedEdge) edge).weight /= sum[edge.type.getVal()];
				 }
			}
		}
		// now each edge group will sum to one and when we scale them using the
		// hyperparams they should all sum to one or less(in case of a certain
		// edge type is missing)

	}

	public boolean hasEdge(Node node2) {
		boolean has_edge = false;
		for (Edge edge : Edge_lst) {
			if (edge.target.equals(node2)) {
				has_edge = true;
				break;
			}

		}
		return has_edge;
	}

	public static void reset_counter() {
		counter = 0;
		psuedo_counter = -1;
	}

	public boolean removeEdge(String src_label) {
		Edge edge_to_delete = null;
		for (Edge edge : Edge_lst) {
			if (edge.target.label.equals(src_label)) {
				edge_to_delete = edge;
				break;
			}
		}
		if (edge_to_delete != null) {
			Edge_lst.remove(edge_to_delete);
			return true;
		}
		return false;
	}

	public void rescaleWeights() {
		for (Edge edge : Edge_lst) {
			edge.rescale();
		}

	}

	public double getOutGoingEdgeSum() {
		double sum = 0.0;
		for (Edge edge : Edge_lst) {
			sum += edge.weight();
		}
		return sum;
	}

	public void normalizeByConstant(double constant) {

		for (Edge edge : Edge_lst) {
			if (constant > 0) {
				((WeightedEdge) edge).weight /= constant;
			}
		}

	}

	public void normalizeEdgeWeightsByTotalSum() {
		double total_sum = 0.0;
		for (Edge edge : Edge_lst) {
			if (edge.type != Edge.TYPE.ANCHOR_EDGE)
				total_sum += edge.weight();
		}
		if (total_sum > 0) {
			for (Edge edge : Edge_lst) {
				if (edge.type != Edge.TYPE.ANCHOR_EDGE) {
					((WeightedEdge) edge).weight /= total_sum;
				}
				
			}
		}
		
	}
}
