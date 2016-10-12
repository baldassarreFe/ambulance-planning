package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CityMap {
	public static CityMap randomize() {
		Random r = new Random();
		int numNodes = 10;
		int numAmbs = 2;
		int numPat = 1;
		int numHos = 1;
		double[][] adjMatrix = new double[numNodes][numNodes];
		double[][] coordinates = new double[numNodes][NUM_COORD];
		double[] demands = new double[numNodes];
		ArrayList<List<NodeContent>> contents = new ArrayList<>();

		for (int node = 0; node < numNodes; node++) {
			coordinates[node][X] = 10 * r.nextDouble();
			coordinates[node][Y] = 10 * r.nextDouble();
			demands[node] = r.nextInt(365);
			contents.add(new ArrayList<>());
		}

		for (int i = 0; i < numAmbs; i++) {
			int node = r.nextInt(numNodes);
			contents.get(node).add(new Ambulance(node));
		}

		for (int i = 0; i < numPat; i++) {
			int node = r.nextInt(numNodes);
			contents.get(node).add(new Patient(node, r.nextInt(3)));
		}

		for (int i = 0; i < numHos; i++) {
			int node = r.nextInt(numNodes);
			contents.get(node).add(new Hospital(node, 5));
		}

		for (int from = 0; from < numNodes; from++) {
			for (int to = 0; to < numNodes; to++) {
				double distance;
				if (from == to) {
					distance = 0;
				} else if (r.nextBoolean()) {
					distance = -1;
				} else {
					double deltaX = coordinates[from][X] - coordinates[to][X];
					double deltaY = coordinates[from][Y] - coordinates[to][Y];
					distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY) * (1 + 0.2 * r.nextDouble());
				}
				adjMatrix[from][to] = distance;
			}
		}

		CityMap map = new CityMap(adjMatrix, coordinates, contents, demands);
		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println(map.represent(Print.HOSPITAL_LOCATIONS));
		System.out.println(map.represent(Print.PATIENT_LOCATIONS));
		System.out.println(map.represent(Print.DEMANDS));

		return map;
	}

	public static final int X = 0;
	public static final int Y = 1;
	public static final int NUM_COORD = 2;

	private final double[][] adjMatrix;
	private final double[] demands;
	private final List<List<NodeContent>> contents;
	private final double[][] shortestDistances;
	private final ArrayList<?>[][] shortestsPaths; // ? will be integers
	private final int nodeCount;
	private final int ambulanceCount;
	private final int hospitalCount;

	public CityMap(double[][] adjMatrix, double[][] coordinates, List<List<NodeContent>> contents, double[] demands) {
		nodeCount = adjMatrix.length;
		ambulanceCount = (int) contents.stream().flatMap(list -> list.stream()).filter(nc -> nc instanceof Ambulance)
				.count();
		hospitalCount = (int) contents.stream().flatMap(list -> list.stream()).filter(nc -> nc instanceof Hospital)
				.count();

		this.demands = demands;
		this.adjMatrix = adjMatrix;
		this.contents = contents;
		shortestDistances = new double[nodeCount][];
		shortestsPaths = new ArrayList<?>[nodeCount][nodeCount];

		computePaths();
	}

	public double[][] getShortestDistances() {
		return shortestDistances;
	}

	public int ambulanceCount() {
		return ambulanceCount;
	}

	public int hospitalCount() {
		return hospitalCount;
	}

	public int patientCount() {
		return (int) contents.stream().flatMap(list -> list.stream()).filter(nc -> nc instanceof Ambulance).count();
	}

	public int nodesCount() {
		return nodeCount;
	}

	public double getDemand(int node) {
		return demands[node];
	}

	public void spawn(Patient patient) {
		contents.get(patient.getNode()).add(patient);
	}

	public List<Double> getDemands() {
		return Arrays.stream(demands).boxed().collect(Collectors.toList());
	}

	public List<Ambulance> getAmbulances() {
		return getSpecificContent(Ambulance.class);
	}

	public List<Patient> getPatients() {
		return getSpecificContent(Patient.class);
	}

	public List<Hospital> getHospitals() {
		return getSpecificContent(Hospital.class);
	}

	public <T extends NodeContent> List<T> getSpecificContent(Class<T> klass) {
		return (List<T>) contents.stream().flatMap(Collection::stream).filter(klass::isInstance)
				.collect(Collectors.toList());
	}

	public double shortestDistance(int from, int to) {
		return shortestDistances[from][to];
	}

	public ArrayList<Integer> shortestPath(int from, int to) {
		return (ArrayList<Integer>) shortestsPaths[from][to];
	}

	public List<?> shortestPathsFrom(int from) {
		return IntStream.range(0, nodeCount).mapToObj(to -> shortestsPaths[from][to]).collect(Collectors.toList());
	}

	public List<?> shortestPathsTo(int to) {
		return IntStream.range(0, nodeCount).mapToObj(from -> shortestsPaths[from][to]).collect(Collectors.toList());
	}

	public void performAction(Action action) {
		action.performAction(this);
	}

	public Set<Integer> adjacentNodes(int from) {
		return IntStream.range(0, nodeCount).filter(to -> adjMatrix[from][to] > 0).boxed().collect(Collectors.toSet());
	}

	public Set<Integer> nodesThatReach(int to) {
		return IntStream.range(0, nodeCount).filter(from -> adjMatrix[from][to] > 0).boxed()
				.collect(Collectors.toSet());
	}

	private void computePaths() {
		// iterate the single source Dijkstra Algorithm for every node
		for (int startNode = 0; startNode < nodeCount; startNode++) {
			Set<Integer> nodesToProcess = IntStream.range(0, nodeCount).boxed().collect(Collectors.toSet());

			double[] shortestDistancesFrom = new double[nodeCount];
			Arrays.fill(shortestDistancesFrom, Double.POSITIVE_INFINITY);
			shortestDistancesFrom[startNode] = 0;

			int[] previousNode = new int[nodeCount];
			Arrays.fill(previousNode, -1);

			while (!nodesToProcess.isEmpty()) {
				// i.e. find the node that has minimum distance between the
				// nodes that are not yet explored
				double minDistance = nodesToProcess.stream().mapToDouble(node -> shortestDistancesFrom[node]).min()
						.getAsDouble();
				int nodeBeingProcessed = nodesToProcess.stream()
						.filter(node -> shortestDistancesFrom[node] == minDistance).findFirst().get();

				nodesToProcess.remove(nodeBeingProcessed);

				// from this node try to reach all the adjacents and check if
				// the overall distance from the starting node decreases
				for (int adjacentNode : adjacentNodes(nodeBeingProcessed)) {
					double alternativeDistance = shortestDistancesFrom[nodeBeingProcessed]
							+ adjMatrix[nodeBeingProcessed][adjacentNode];
					if (alternativeDistance < shortestDistancesFrom[adjacentNode]) {
						shortestDistancesFrom[adjacentNode] = alternativeDistance;
						previousNode[adjacentNode] = nodeBeingProcessed;
					}
				}
			}

			shortestDistances[startNode] = shortestDistancesFrom;

			// generate all paths backtracking on previousNode
			for (int endNode = 0; endNode < nodeCount; endNode++) {
				ArrayList<Integer> path = new ArrayList<Integer>();
				if (Double.isFinite(shortestDistances[startNode][endNode])) {
					int intermediateNode = endNode;
					while (intermediateNode >= 0 && previousNode[intermediateNode] >= 0) {
						path.add(0, intermediateNode);
						intermediateNode = previousNode[intermediateNode];
					}
					path.add(0, intermediateNode);
				}
				shortestsPaths[startNode][endNode] = path;
			}
		}
	}

	public String represent(Print what) {
		StringBuilder sb = new StringBuilder();

		switch (what) {
		case ADJ_MATRIX:
		case SHORTEST_DISTANCES_MATRIX:
			double[][] matrix = what == Print.ADJ_MATRIX ? adjMatrix : shortestDistances;
			sb.append(what == Print.ADJ_MATRIX ? "adjMatrix\n" : "shortestDistances\n");
			sb.append(IntStream.range(0, nodeCount).mapToObj(Integer::toString)
					.collect(Collectors.joining("\t", "\t", "\n")));
			sb.append(IntStream.range(0, nodeCount)
					.mapToObj(from -> IntStream.range(0, nodeCount).mapToDouble(to -> matrix[from][to])
							.mapToObj(d -> Double.isInfinite(d) || d < 0 ? "-" : String.format("%+.2f", d))
							.collect(Collectors.joining("\t", from + "\t", "")))
					.collect(Collectors.joining("\n")));
			break;
		case SHORTEST_PATHS:
			sb.append("Shortest paths\n");
			IntStream.range(0, nodeCount)
					.forEachOrdered(from -> IntStream.range(0, nodeCount).forEachOrdered(to -> sb.append(String.format(
							"N%d -> N%d (%.3f): %s\n", from, to, shortestDistances[from][to],
							shortestsPaths[from][to].stream().map(n -> "N" + n).collect(Collectors.joining(","))))));
			break;
		case AMBULANCES_LOCATIONS:
			sb.append(getAmbulances().stream().map(NodeContent::getNode).map(Object::toString)
					.collect(Collectors.joining(", ", "Ambulances [", "]")));
			break;
		case HOSPITAL_LOCATIONS:
			sb.append(getHospitals().stream().map(NodeContent::getNode).map(Object::toString)
					.collect(Collectors.joining(", ", "Hospitals [", "]")));
			break;
		case PATIENT_LOCATIONS:
			sb.append(getPatients().stream().map(NodeContent::getNode).map(Object::toString)
					.collect(Collectors.joining(", ", "Patients [", "]")));
			break;
		case DEMANDS:
			sb.append(IntStream.range(0, nodeCount).mapToObj(Integer::toString)
					.collect(Collectors.joining("\t", "Demands: ", "\n")));
			sb.append(IntStream.range(0, nodeCount).mapToDouble(n -> demands[n]).mapToObj(Double::toString)
					.collect(Collectors.joining("\t", "         ", "")));
			break;
		default:
			break;
		}
		return sb.toString();
	}

	public static enum Print {
		ALL, ADJ_MATRIX, SHORTEST_DISTANCES_MATRIX, SHORTEST_PATHS, AMBULANCES_LOCATIONS, PATIENT_LOCATIONS, HOSPITAL_LOCATIONS, DEMANDS
	}

	public int closestHospital(int from) {
		double min = Double.POSITIVE_INFINITY;
		int result = 0;
		for (int hosNode : getHospitals().stream().mapToInt(h->h.getNode()).toArray()){
			double dist = shortestDistance(from, hosNode);
			if (dist<min) {
				result = hosNode;
				min = dist;
			}
		}		
		return result;
	}
}
