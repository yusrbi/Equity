// File: AdjListsGraph.java
package graph;

import graph.Node.TYPE;

import java.util.*;
import java.util.Map.Entry;


import edu.stanford.nlp.util.Pair;
import resources.Resources;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import knowledgebase.Candidate;

/**
 * This is a graph implementation that uses adjacency lists to store edges. It
 * contains two linked-lists for every node i of the graph. The first list for
 * node i, the "from i" list, contains edges from i to other nodes. The second
 * list for node i, the "to i" list, contains edges from other nodes to i. The
 * lists are not sorted; they contain the adjacent nodes in the order of edge
 * insertion. In other words, the edge at the tail of the "from i" list of some
 * node i corresponds to the edge *with source* i that was added to the graph
 * most recently (and has not been removed, yet) and the edge at the tail of the
 * "to i" list of some node i corresponds to the edge *with target* i that was
 * added to the graph most recently (and has not been removed, yet).
 */

public class AdjListsGraph extends Graph {

	protected Map<String, Node> nodes;
	private PriorityQueue<PotentialEdge> potential_edges;

	private Map<String, Set<Candidate>> mention_candidate_map;
	private Map<String, Double> candidateCandiate_weight_map;
	Matrix w = null;
	Vector row_sum = null;
	int anchor_nodes_count = 0;

	/**
	 * Class Constructor. It initializes the number of nodes, and instantiates
	 * the vectors of adjacency lists so that each list is empty.
	 * 
	 * @param nodes
	 *            the number of nodes.
	 */
	public AdjListsGraph(int nodes) {
		super(nodes);
		this.nodes = new LinkedHashMap<String, Node>(nodes);
		potential_edges = new PriorityQueue<PotentialEdge>(new Comparator<PotentialEdge>() {
			public int compare(PotentialEdge entry1, PotentialEdge entry2) {
				return entry2.getWeight().compareTo(entry1.weight);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Graph#getEdges(int source)
	 */
	public LinkedList<Edge> getEdges(int i) {
		LinkedList<Edge> edges = new LinkedList<Edge>(this.nodes.get(i).getEdges());

		return edges;
	}

	@Override
	public void addMentionNode(String mention, Set<Candidate> candidates) {
		if (nodes.containsKey(mention))
			return;
		// if(mention.startsWith("MENTION:INDEPENDENCE OF")){
		// int i =0;
		// }
		Node src_node = new Node(mention, TYPE.MENTION);
		nodes.put(mention, src_node);
		Node dest_node = null;
		String sem_id;
		if (candidates != null) {
			for (Candidate candidate : candidates) {
				sem_id = candidate.getSemanticTargetIdForGraph();
				// if(sem_id.startsWith("MENTION:INDEPENDENCE OF")){
				// int i =0;
				// }
				if (!nodes.containsKey(sem_id)) {
					dest_node = new Node(sem_id, TYPE.SEAMANTIC_TARGET);
					nodes.put(sem_id, dest_node);
				} else {
					dest_node = nodes.get(sem_id);
				}
				addEdge(mention, sem_id, candidate.getScore(), Edge.TYPE.MENTION_CANDIDATE);
			}

		} else {
			// TODO check how to handel no candidates --> should the mention be
			// removed completely ??!
			return;
		}

	}

	@Override
	public void addEdge(String id1, String id2, double weight, Edge.TYPE type) {
		if (weight <= 0)
			return;// no need to add edge of weight zero
		// TODO check for the duplicate edges here
		Node node1 = nodes.get(id1);
		Node node2 = nodes.get(id2);
		if (node2 == null)
			return;
		Edge edge = new WeightedEdge(node2, weight, type, false);
		node1.addEdge(edge);
		edge = new WeightedEdge(node1, weight, type, false);
		node2.addEdge(edge);
	}

	@Override
	public void addEdge(Node node1, Node node2, double weight, Edge.TYPE type) {
		if (weight <= 0)
			return;
		if (node1.hasEdge(node2)) {
			return; // TODO aggregate ??
		}
		Edge edge = new WeightedEdge(node2, weight, type, false);
		node1.addEdge(edge);
		edge = new WeightedEdge(node1, weight, type, false);
		node2.addEdge(edge);
	}

	@Override
	public Pair<Double,Integer> startRWRFromCandidatesDirected(String nodeId, Vector all_candidates_teleport) {
		Node startNode = nodes.get(nodeId);
		SparseVector teleport = new SparseVector(nodes.size(), 1);
		if (w == null) {
			w = new LinkedSparseMatrix(nodes.size(), nodes.size());
			w.zero();
			fill(w);
		}
		Matrix w_s = w.copy(); // row_stochastic(w);
		teleport.set(startNode.id, 1);
		Vector results = GraphAlgorithms.RWR_directed_variant(w_s, teleport, all_candidates_teleport,
				Resources.getResources().getRwr_gamma(), Math.min(this.nodes.size(), Resources.getResources().getRwr_max_itr()), Resources.getResources().getAlpha());// Resources.getResources().getRwr_max_itr()
		return beliefIn_anchors(nodeId, results);
	}

	@Override
	public double startRWRFromCandidates(String nodeId) {
		Node startNode = nodes.get(nodeId);
		SparseVector teleport = new SparseVector(nodes.size(), 1);
		if (w == null) {
			w = new LinkedSparseMatrix(nodes.size(), nodes.size());
			w.zero();
			fill(w);
		}
		Matrix w_s = w.copy(); // row_stochastic(w);
		teleport.set(startNode.id, 1);
		Vector results = GraphAlgorithms.RWR(w_s, teleport, Resources.getResources().getRwr_gamma(), 
				 Math.min(this.nodes.size(), Resources.getResources().getRwr_max_itr()),
				Resources.getResources().getAlpha());// Resources.getResources().getRwr_max_itr()
		return beliefIn_mentions(nodeId, results);
	}

	private Pair<Double,Integer> beliefIn_anchors(String nodeId, Vector results) {
		double Belf = 0;
		int infinity_count =0;
		boolean first = true;
		for (Node node : nodes.values()) {
			if (node.type.equals(TYPE.ANCHOR)) {// over the anchor nodes
				if (!first) {
					if (results.get(node.id) > 0)
						Belf += Math.log(results.get(node.id)); // addition of
																// logs instead
																// of
					// multiplication
					// because it overflow
					else { // a zero belief is consider as a -infinity distance
						infinity_count++;
					}
				} else {
					if (results.get(node.id) > 0) {
						Belf = Math.log(results.get(node.id));
						first = false;
					} else {// a zero belief is consider as a -infinity distance
						infinity_count++;
					}

				}
			}
		}
		return new Pair<Double,Integer>(Belf,infinity_count);
	}

	private double beliefIn_mentions(String nodeId, Vector results) {
		double Belf = 0;
		boolean first = true;
		for (Node node : nodes.values()) {
			if (node.type.equals(TYPE.MENTION)) {// over the anchor nodes
				if (!first) {
					if (results.get(node.id) > 0)
						Belf += Math.log(results.get(node.id)); // addition of
																// logs instead
																// of
					// multiplication
					// because it overflow
				} else if (results.get(node.id) > 0) {
					Belf = Math.log(results.get(node.id));
					first = false;
				}
			}
		}
		return Belf;
	}

	private void fill(Matrix w) {
		Node node;
		row_sum = new DenseVector(w.numRows());
		row_sum.zero();
		for (Entry<String, Node> entry : nodes.entrySet()) {
			node = entry.getValue();
			if (node.getEdges() == null || node.getEdges().isEmpty())
				continue;
			for (Edge edge : node.getEdges()) {
				//if(edge.weight() > 0){
					w.set(node.id, edge.target.id, edge.weight());
					row_sum.add(node.id(), edge.weight());
				//}
				
			}
		}

	}

	@Override
	public String getPythonEdgeList(boolean write_potential_edges) {
		StringBuilder graph_content = new StringBuilder();
		if (nodes.size() > 1500)
			return "Too many nodes and edges :( will cause out of memeory java heap error  ";
		Node node;
		String label;
		PotentialEdge edge_pot;
		for (Entry<String, Node> entry : nodes.entrySet()) {
			node = entry.getValue();
			label = entry.getKey();
			if (node.getEdges() == null || node.getEdges().size() == 0) {
				graph_content.append(String.format(
						"%d -1 {'weight':%f, 'src_label':'%s', 'dest_label':'NO_CANDIDATES', 'src_type'='%s', dest_type='NO_CANDIDATES'} \n",
						node.id, 0.0, label, node.type));
			} else {
				for (Edge edge : node.getEdges()) {
					graph_content.append(
							String.format("%d %d {'weight':%f, 'src_label':'%s', 'dest_label':'%s', 'type'='%s' } \n", // 'src_type'='%s',
																														// dest_type='%s'
									node.id, edge.target.id, ((WeightedEdge) edge).weight, label, edge.target.label,
									edge.type));
				}
			}
		}
		if (write_potential_edges && potential_edges != null) {
			Iterator<PotentialEdge> potential_edges_itr = potential_edges.iterator();
			while (potential_edges_itr.hasNext()) {
				edge_pot = potential_edges_itr.next();
				graph_content
						.append(String.format("%d %d {'weight':%f,'src_label':'%s', 'dest_label':'%s', 'Type'='%s'} \n",
								-1, -1, edge_pot.weight, edge_pot.from, edge_pot.to, edge_pot.type));
			}

		}

		return graph_content.toString();
	}

	@Override
	public void normalizeEdgeWeights() {
		for (Node node : nodes.values()) {
			node.normalizeEdgeWeights();
		}
	}

	@Override
	public double[] startRWRFromMention(String mentionId) {
		Node startNode = nodes.get(mentionId);
		Vector teleport = new SparseVector(nodes.size(), 1);
		if (w == null) {// only fill once and reuse
			w = new LinkedSparseMatrix(nodes.size(), nodes.size());
			w.zero();
			fill(w);
		}
		teleport.set(startNode.id, 1);
		Matrix w_s = w.copy();// row_stochastic(w);
		// we have to use the irreducible matrix all the times, as we have cases
		// at which mentions have no candidates
		Vector results = GraphAlgorithms.RWR(w_s, teleport, Resources.getResources().getRwr_gamma(), 
				 Math.min(this.nodes.size(), Resources.getResources().getRwr_max_itr()),
				Resources.getResources().getAlpha());
		return Matrices.getArray(results);
	}

	

	@Override
	public int getNodeId(String semanticTargetIdForGraph) {

		return nodes.get(semanticTargetIdForGraph).id;
	}

	@Override
	public void addMentionNodeAggregateCandidates(String mention, Set<Candidate> candidates) {
		if (nodes.containsKey(mention))
			return;
		if (mention_candidate_map == null)
			mention_candidate_map = new HashMap<String, Set<Candidate>>();
		Node src_node = new Node(mention, TYPE.MENTION);
		nodes.put(mention, src_node);
		Node dest_node = null;
		if (candidates != null) {
			if (!nodes.containsKey(mention + "_cand")) {
				dest_node = new Node(mention + "_cand", TYPE.SEAMANTIC_TARGET);
				nodes.put(mention + "_cand", dest_node);
			} else {
				dest_node = nodes.get(mention + "_cand");
			}
			addEdge(mention, mention + "_cand", Edge.hyper_param[Edge.TYPE.MENTION_CANDIDATE.getVal()],
					Edge.TYPE.MENTION_CANDIDATE);
			mention_candidate_map.put(mention, candidates);

		} else {
			// no candidates sync node, will be regularized
			return;
		}

	}

	@Override
	public void removeCycles() {
		// implementation of Kruskal's
		// initially the number of edges equal t the number of mentions, which
		// is the number of nodes/2
		// the potential edges are stored in a priority queue with reversed
		// natural order, max weight is on the top
		// add edges with the highest weights
		int count = 0;
		// we can add up to (nodes/2) -1 more edges
		// initially we have n/2 components each with 2 nodes: a mention and a
		// candidate
		List<Set<Node>> components = new LinkedList<Set<Node>>();
		Set<Node> component = null;
		Node target_node;
		PotentialEdge edge;
		Set<Node> target_component = null;
		Set<Node> source_component = null;
		// init components
		for (Node node : nodes.values()) {
			if (!node.label.endsWith("_cand")) {
				component = new LinkedHashSet<Node>();
				component.add(node);
				if (node.Edge_lst != null && node.Edge_lst.size() > 0) {
					target_node = node.Edge_lst.get(0).target;
					component.add(target_node);
					count++;
				}
				components.add(component);
			}

		}
		int maxEdges = count + components.size() - 1;
		while (count <= maxEdges && potential_edges.peek() != null) {
			// pick one edge
			edge = potential_edges.poll();
			if (edge != null) {
				// check if it won't create a cycle
				source_component = findComponent(edge.from, components);
				target_component = findComponent(edge.to, components);
				if (source_component != target_component) {
					if (AddEdge(edge)) {
						// in case the edge was added successfully
						count++;
						mergeComponents(source_component, target_component, components);
					}
				}
			}
		}

	}

	private void mergeComponents(Set<Node> source_component, Set<Node> target_component, List<Set<Node>> components) {
		source_component.addAll(target_component);
		components.remove(target_component);
		return;
	}

	private boolean AddEdge(PotentialEdge potential_edge) {
		if (potential_edge.weight <= 0)
			return false;
		Node node1 = nodes.get(potential_edge.from);
		Node node2 = nodes.get(potential_edge.to);
		if (node2 == null)
			return false;
		Edge edge = new WeightedEdge(node2, potential_edge.weight, potential_edge.type, false);
		node1.addEdge(edge);
		edge = new WeightedEdge(node1, potential_edge.weight, potential_edge.type, false);
		node2.addEdge(edge);
		return true;
	}

	private Set<Node> findComponent(String from, List<Set<Node>> components) {
		for (Set<Node> component : components) {
			for (Node node : component) {
				if (node.label.equals(from))
					return component;
			}
		}
		return null;
	}

	@Override
	public void expandEdges() {
		// expand the _cand nodes into candidates
		// add the original candidates
		// add their links to the mentions
		Node node = null;
		String sem_id = null;
		for (Entry<String, Set<Candidate>> entry : mention_candidate_map.entrySet()) {
			for (Candidate candidate : entry.getValue()) {
				sem_id = candidate.getSemanticTargetIdForGraph();
				if (!nodes.containsKey(sem_id)) {
					node = new Node(sem_id, TYPE.SEAMANTIC_TARGET);
					nodes.put(sem_id, node);
				} else {
					node = nodes.get(sem_id);// should not be true
				}
				addEdge(entry.getKey(), sem_id, candidate.getScore(), Edge.TYPE.MENTION_CANDIDATE);
			}
		}
		String id, mention;
		Node anchor;
		// for each anchor node edge in the MRF
		for (int i = 1; i <= anchor_nodes_count; i++) {
			id = String.format("a_%d", i);
			anchor = nodes.get(id);
			if (anchor.Edge_lst != null) {
				Edge edge = anchor.Edge_lst.get(0);
				// should be one edge
				if (edge.target.label.endsWith("_cand")) {
					mention = edge.target.label.substring(0, edge.target.label.length() - 5);
					AddAnchorCandidatesEdges(anchor, mention_candidate_map.get(mention));
					removeAnchorSemanticTargetEdge(anchor, edge.target);
				}
			}

		}

		// for each cand-cand edge in the weight matrix add edge iff in the
		// reduced graph
		Set<Candidate> source_cand, dest_cand;
		String src_mention, dest_mention;
		Set<String> done = new LinkedHashSet<String>();
		List<String> nodes_to_remove = new LinkedList<String>();
		for (Node cand_node : nodes.values()) {
			if (cand_node.label.endsWith("_cand")) {
				for (Edge edge : cand_node.getEdges()) {
					if (edge.target.label.endsWith("_cand")) {
						src_mention = cand_node.label.substring(0, cand_node.label.length() - 5);
						dest_mention = edge.target.label.substring(0, edge.target.label.length() - 5);
						if (done.contains(src_mention + dest_mention) || done.contains(dest_mention + src_mention))
							continue;
						// add edges
						source_cand = mention_candidate_map.get(src_mention);
						dest_cand = mention_candidate_map.get(dest_mention);
						if (source_cand != null && dest_cand != null) {
							AddCandidateCandiateEdges(source_cand, dest_cand);
						}
						done.add(src_mention + dest_mention);

					} else {
						// mention candidate edge, remove
						removeCandidateMentionEdge(cand_node, edge);
					}
				}
				nodes_to_remove.add(cand_node.label);
			}
		}
		for (String cand_node_label : nodes_to_remove) {
			nodes.remove(cand_node_label);
		}
	}

	private void removeAnchorSemanticTargetEdge(Node anchor, Node target) {

		anchor.removeEdge(target.label);

	}

	private void AddCandidateCandiateEdges(Set<Candidate> source_cands, Set<Candidate> dest_cands) {
		double weight = 0;
		for (Candidate src_cand : source_cands) {
			for (Candidate dest_cand : dest_cands) {
				weight = getCandidateCandidateWeight(src_cand.getSemanticTargetIdForGraph(),
						dest_cand.getSemanticTargetIdForGraph());
				if (weight > 0)
					addEdge(src_cand.getSemanticTargetIdForGraph(), dest_cand.getSemanticTargetIdForGraph(), weight,
							Edge.TYPE.CANDIDATE_CANDIDATE);
			}
		}
	}

	private void AddAnchorCandidatesEdges(Node anchor, Set<Candidate> candidates) {
		if (candidates == null)
			return;
		for (Candidate cand : candidates) {
			addEdge(anchor.label, cand.getSemanticTargetIdForGraph(), 1.0, Edge.TYPE.ANCHOR_EDGE);
		}
	}

	private double getCandidateCandidateWeight(String label1, String label2) {
		if (candidateCandiate_weight_map.containsKey(label1 + label2)) {
			return candidateCandiate_weight_map.get(label1 + label2);
		} else if (candidateCandiate_weight_map.containsKey(label2 + label1)) {
			return candidateCandiate_weight_map.get(label2 + label1);
		}
		return 0.0;
	}

	private boolean removeCandidateMentionEdge(Node cand_node, Edge edge) {
		return edge.target.removeEdge(cand_node.label);
	}

	@Override
	public void addPotentialEdge(String node1, String node2, double weight, graph.Edge.TYPE type) {
		if (weight <= 0)
			return;
		PotentialEdge edge = new PotentialEdge(node1, node2, Edge.hyper_param[type.getVal()] * weight , type);
		potential_edges.add(edge);
	}

	@Override
	public void addCandidateCandidateEdgeWeight(String label1, String label2, double sim) {
		if (candidateCandiate_weight_map == null)
			candidateCandiate_weight_map = new HashMap<String, Double>();
		candidateCandiate_weight_map.put(label1 + label2, sim);
	}

	@Override
	public long getNEdges() {
		return Edge.get_counter();
	}

	@Override
	public void adjastEdgeWeightsUsingHyperparams() {
		for (Node node : nodes.values()) {
			node.rescaleWeights();
		}

	}

	@Override
	public int getNNodes() {

		return nodes.size();
	}

	@Override
	public void addAnchorNodes() {
		int count = 0;
		Node anchor;
		String id;
		Map<String, Node> anchors = new HashMap<String, Node>();
		for (Node node : nodes.values()) {
			if (node.Edge_lst.size() == 1) {
				// leaf variable attach an anchor to it
				id = String.format("a_%d", ++count);
				anchor = new Node(id, TYPE.ANCHOR);
				anchor.addEdge(new WeightedEdge(node, 1.0, graph.Edge.TYPE.ANCHOR_EDGE, false));
				node.addEdge(new WeightedEdge(anchor, 1.0, graph.Edge.TYPE.ANCHOR_EDGE, false));
				anchors.put(id, anchor);
			}
		}
		nodes.putAll(anchors);
		anchor_nodes_count = count;
	}

	@Override
	public void normalizeEdgeWeightsByConstant() {
		double max_sum =Double.MIN_VALUE;
		for (Node node : nodes.values()) {
			max_sum = Math.max(max_sum, node.getOutGoingEdgeSum());
		}
		for (Node node : nodes.values()) {
			node.normalizeByConstant(max_sum);
		}
	}

	@Override
	public void renormalize() {

		for (Node node : nodes.values()) {
			node.normalizeEdgeWeightsByTotalSum();
		}

	}

}
