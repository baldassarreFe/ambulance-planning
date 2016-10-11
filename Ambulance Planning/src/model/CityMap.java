package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CityMap {
	public static void main(String[] args) {
		Random r = new Random();
		List<Node> nodes = new ArrayList<>();
		Map<NodePair, Double> adjMatrix = new HashMap<>();
		for (int i = 0; i < 8; i++) {
			nodes.add(new Node(10 * r.nextDouble(), 10 * r.nextDouble(), r.nextInt(365)));
		}
		for (Node from : nodes) {
			for (Node to : nodes) {
				double distance;
				if (from.equals(to)) {
					distance = 0;
				} else if (r.nextBoolean()) {
					distance = -1;
				} else {
					distance = noisedDistance(r, from, to);
				}
				adjMatrix.put(new NodePair(from, to), distance);
			}
		}

		CityMap map = new CityMap(adjMatrix, nodes);

		for (Node from : nodes) {
			for (Node to : nodes) {
				System.out.println(map.shortestPath(from, to));
			}
		}
	}

	private static double noisedDistance(Random r, Node from, Node to) {
		double deltaX = from.getX() - to.getX();
		double deltaY = from.getY() - to.getY();
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY) * (1 + 0.2 * r.nextDouble());
	}

	private final List<Path> shortestPaths;
	private final List<Node> nodes;

	public CityMap(Map<NodePair, Double> adjMatrix, List<Node> nodes) {
		this.nodes = nodes;
		this.shortestPaths = new ArrayList<>();
		computePaths(adjMatrix);
	}

	public int ambulanceCount() {
		return nodes.stream().mapToInt(n -> n.getAmbulances().size()).sum();
	}

	public int hospitalCount() {
		return nodes.stream().mapToInt(n -> n.getHospitals().size()).sum();
	}

	public int patientCount() {
		return nodes.stream().mapToInt(n -> n.getPatients().size()).sum();
	}

	public int nodesCount() {
		return nodes.size();
	}

	public void spawn(Patient patient, int nodeId) {
		nodes.stream().filter(n -> n.getId() == nodeId).findFirst().ifPresent(n -> n.spawn(patient));
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Node> getAmbulancesLocation() {
		return nodes.stream().filter(Node::containsAmbulance).collect(Collectors.toList());
	}

	public List<Node> getHospitalsLocation() {
		return nodes.stream().filter(Node::containsHospital).collect(Collectors.toList());
	}

	public List<Node> getPatientsLocation() {
		return nodes.stream().filter(Node::containsPatient).collect(Collectors.toList());
	}

	public Path shortestPath(int from, int to) {
		return shortestPaths.stream().filter(p -> p.getFrom().getId() == from && p.getTo().getId() == to).findFirst()
				.get();
	}

	public Path shortestPath(Node from, Node to) {
		return shortestPaths.stream()
				.filter(p -> p.getFrom().getId() == from.getId() && p.getTo().getId() == to.getId()).findFirst().get();
	}

	public List<Path> shortestPathsFrom(Node from) {
		return shortestPaths.stream().filter(p -> p.getFrom().getId() == from.getId()).collect(Collectors.toList());
	}

	public List<Path> shortestPathsTo(Node to) {
		return shortestPaths.stream().filter(p -> p.getTo().getId() == to.getId()).collect(Collectors.toList());
	}

	public List<Node> reachableFrom(Node from) {
		return shortestPaths.stream().filter(p -> p.getFrom().getId() == from.getId() && p.getHops() == 1)
				.map(Path::getTo).collect(Collectors.toList());
	}

	public List<Node> reaching(Node to) {
		return shortestPaths.stream().filter(p -> p.getTo().getId() == to.getId() && p.getHops() == 1)
				.map(Path::getFrom).collect(Collectors.toList());
	}

	public void performActions(List<Action> actions) {
		for (Action action : actions) {
			action.performAction(this);
		}
	}

	/*
	 * This function would update the path list (contains the shortest path and the cost info) between every possible pair of points
	 * @params adjMatrix - adjacency matrix
	 */
	private void computePaths(Map<NodePair, Double> adjMatrix) {
		int numNodes = (int) Math.sqrt(adjMatrix.size());
		double[][] adjacencyMatrix = new double[numNodes][numNodes];
		double[][] distance = new double[numNodes][numNodes];
		List<List<Integer>> paths = new ArrayList<List<Integer>>();

		for (Entry<NodePair, Double> e : adjMatrix.entrySet()) {
			NodePair np = (NodePair) e.getKey();
			double d = (double) e.getValue();
			adjacencyMatrix[np.from][np.to] = d;
		}

		// this loop would generate all the paths and updates the distance matrix
		for (int i = 0; i < numNodes; i++) {
			Set<Integer> Q = new HashSet<Integer>();
			double[] dist = new double[numNodes];
			int[] prev = new int[numNodes];

			for (int j = 0; j < numNodes; j++) {
				Q.add(j);
				dist[j] = Double.POSITIVE_INFINITY;
				prev[j] = -1;
			}

			dist[i] = 0;

			while (!Q.isEmpty()) {
				int u = minDist(dist, Q);
				Q.remove(u);

				for (int v = 0; v < numNodes; v++) {
					if (adjacencyMatrix[u][v] > 0) {
						double alt = dist[u] + adjacencyMatrix[u][v];
						if (alt < dist[v]) {
							dist[v] = alt;
							prev[v] = u;
						}
					} // if adjacency end
				} // for v end
			} // while end (Q is empty)

			// generate all paths
			for (int trgt = 0; trgt < numNodes; trgt++) {
				List<Integer> S = new ArrayList<Integer>();
				int u = trgt;
				while (u >= 0 && prev[u] >= 0) {
					S.add(0, u);
					u = prev[u];
				}
				S.add(0, u);
				paths.add(S);
			} // backtracking for end
			distance[i] = dist;
		} // end of source for

		for (int from = 0; from < numNodes; from++) {
			for (int to = 0; to < numNodes; to++) {
				Path p = new Path(
						paths.get(from * numNodes + to).stream().map(id -> nodes.get(id)).collect(Collectors.toList()),
						distance[from][to]);
				shortestPaths.add(p);
			}
		}
	}

	/*
	 * A utility function for the computePath function
	 */
	public static int minDist(double[] dist, Set<Integer> Q) {
		int ind = 0;
		int lenDist = dist.length;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < lenDist; i++) {
			if (Q.contains(i)) {
				if (dist[i] < min) {
					min = dist[i];
					ind = i;
				}
			}
		}
		return ind;
	}

	public static class NodePair {
		public final int from;
		public final int to;

		public NodePair(Node from, Node to) {
			this.from = from.getId();
			this.to = to.getId();
		}

		@Override
		public String toString() {
			return "[ N_" + from + " : N_" + to + " ]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + from;
			result = prime * result + to;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodePair other = (NodePair) obj;
			if (from != other.from)
				return false;
			if (to != other.to)
				return false;
			return true;
		}
	}
}
