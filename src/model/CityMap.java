package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CityMap {
	private final List<Path> shortestPaths;
	private final List<Node> nodes;
	
	public CityMap(Map<NodePair, Integer> distanceMatrix, List<Node> nodes) {
		this.nodes = nodes;
		this.shortestPaths = new ArrayList<>();
		computePaths(distanceMatrix);
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
	
	public void spawn(Patient patient, int nodeId) {
		nodes.stream().filter(n -> n.getId() == nodeId).findFirst().ifPresent(n -> n.spawn(patient)); 
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
	
	public List<Path> shortestPathsFrom(Node from) {
		return shortestPaths.stream().filter(p -> p.getFrom().getId() == from.getId()).collect(Collectors.toList());
	}
	
	public List<Path> shortestPathsTo(Node to) {
		return shortestPaths.stream().filter(p -> p.getTo().getId() == to.getId()).collect(Collectors.toList());
	}
	
	public List<Node> reachableFrom(Node from) {
		return shortestPaths.stream().filter(p -> p.getFrom().getId() == from.getId() && p.getHops() == 1).map(Path::getTo).collect(Collectors.toList());
	}
	
	public List<Node> reaching(Node to) {
		return shortestPaths.stream().filter(p -> p.getTo().getId() == to.getId() && p.getHops() == 1).map(Path::getFrom).collect(Collectors.toList());
	}

	public void performActions(List<Action> actions) { 
		for (Action action : actions) {
			action.performAction(this);
		}
	}

	private void computePaths(Map<NodePair, Integer> distanceMatrix) {
		// TODO with SPF
	}

	public class NodePair {
		public final int from;
		public final int to;
		public NodePair(Node from, Node to) {
			this.from = from.getId();
			this.to = to.getId();
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
