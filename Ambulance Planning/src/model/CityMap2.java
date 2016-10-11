package model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CityMap2 {
	public static void main(String[] args) {
		Random r = new Random();
		int numNodes = 4;
		double[][] adjMatrix = new double[numNodes][numNodes];
		double[][] coordinates = new double[numNodes][NUM_COORD];
		double[] demands = new double[numNodes];
		ArrayList<?>[] contents = new ArrayList<?>[numNodes];

		for (int node = 0; node < numNodes; node++) {
			coordinates[node][X] = 10 * r.nextDouble();
			coordinates[node][Y] = 10 * r.nextDouble();
			demands[node] = r.nextInt(365);
			contents[node] = new ArrayList<>();
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

		CityMap2 map = new CityMap2(adjMatrix, coordinates, (ArrayList<NodeContent>[]) contents, demands);
		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.DEMANDS));
	}

	public static final int X = 0;
	public static final int Y = 1;
	public static final int NUM_COORD = 2;

	private final double[][] adjMatrix;
	private final double[] demands;
	private final ArrayList<NodeContent>[] contents;
	private final double[][] shortestDistances;
	private final ArrayList<?>[][] shortestsPaths; // ? will be integers
	private final int nodeCount;
	private final int ambulanceCount;
	private final int hospitalCount;

	public CityMap2(double[][] adjMatrix, double[][] coordinates, ArrayList<NodeContent>[] contents, double[] demands) {
		nodeCount = adjMatrix.length;
		ambulanceCount = (int) Arrays.stream(contents).flatMap(list -> list.stream())
				.filter(nc -> nc instanceof Ambulance).count();
		hospitalCount = (int) Arrays.stream(contents).flatMap(list -> list.stream())
				.filter(nc -> nc instanceof Hospital).count();

		this.demands = demands;
		this.adjMatrix = adjMatrix;
		this.contents = contents;
		shortestDistances = new double[nodeCount][];
		shortestsPaths = new ArrayList<?>[nodeCount][nodeCount];

		computePaths();
	}

	public int ambulanceCount() {
		return ambulanceCount;
	}

	public int hospitalCount() {
		return hospitalCount;
	}

	public int patientCount() {
		return (int) Arrays.stream(contents).flatMap(list -> list.stream()).filter(nc -> nc instanceof Ambulance)
				.count();
	}

	public int nodesCount() {
		return nodeCount;
	}

	public double getDemands(int node) {
		return demands[node];
	}

	public void spawn(Patient patient, int nodeId) {
		contents[nodeId].add(patient);
	}

	public List<Integer> getAmbulancesLocations() {
		return getLocationsOf(Ambulance.class);
	}

	public List<Integer> getHospitalLocations() {
		return getLocationsOf(Ambulance.class);
	}

	public List<Integer> getPatientLocations() {
		return getLocationsOf(Ambulance.class);
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
		return (List<T>) Arrays.stream(contents).flatMap(Collection::stream)
				.filter(klass::isInstance)
				.collect(Collectors.toList());
	}

	private List<Integer> getLocationsOf(Class<? extends NodeContent> klass) {
		List<Integer> result = new ArrayList<>();
		IntStream.range(0, nodeCount).forEach(node -> {
			for (int count = 0; count < contents[node].stream().filter(nc -> klass.isInstance(nc)).count(); count++) {
				result.add(node);
			}
		});
		return result;
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

	public void performActions(List<Action> actions) {
		for (Action action : actions) {
			// action.performAction(this);
		}
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
				int intermediateNode = endNode;
				while (intermediateNode >= 0 && previousNode[intermediateNode] >= 0) {
					path.add(0, intermediateNode);
					intermediateNode = previousNode[intermediateNode];
				}
				path.add(0, intermediateNode);
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

			sb.append(IntStream.range(0, nodeCount).mapToObj(Integer::toString)
					.collect(Collectors.joining("\t", "\t", "\n")));
			sb.append(IntStream.range(0, nodeCount)
					.mapToObj(from -> IntStream.range(0, nodeCount).mapToDouble(to -> matrix[from][to])
							.mapToObj(d -> d < 0 ? "-" : String.format("%+.2f", d))
							.collect(Collectors.joining("\t", from + "\t", "")))
					.collect(Collectors.joining("\n")));
			break;
		case SHORTEST_PATHS:
			IntStream.range(0, nodeCount)
					.forEachOrdered(from -> IntStream.range(0, nodeCount).forEachOrdered(to -> sb.append(String.format(
							"N%d -> N%d (%.3f): %s\n", from, to, shortestDistances[from][to],
							shortestsPaths[from][to].stream().map(n -> "N" + n).collect(Collectors.joining(","))))));
			break;
		case AMBULANCES_LOCATIONS:
			sb.append(getAmbulancesLocations().toString());
			sb.append("\n");
			break;
		case HOSPITAL_LOCATIONS:
			sb.append(getHospitalLocations().toString());
			sb.append("\n");
			break;
		case PATIENT_LOCATIONS:
			sb.append(getPatientLocations().toString());
			sb.append("\n");
			break;
		case DEMANDS:
			sb.append(IntStream.range(0, nodeCount).mapToObj(Integer::toString)
					.collect(Collectors.joining("\t", "", "\n")));
			sb.append(IntStream.range(0, nodeCount).mapToDouble(n -> demands[n]).mapToObj(Double::toString)
					.collect(Collectors.joining("\t", "", "")));
			break;
		default:
			break;
		}
		return sb.toString();
	}

	public static enum Print {
		ALL, ADJ_MATRIX, SHORTEST_DISTANCES_MATRIX, SHORTEST_PATHS, AMBULANCES_LOCATIONS, PATIENT_LOCATIONS, HOSPITAL_LOCATIONS, DEMANDS
	}
}
