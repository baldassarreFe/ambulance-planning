package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
	private final double distance;
	private final List<Node> path;

	public Node getFrom() {
		return path.get(0);
	}

	public Node getTo() {
		return path.get(path.size() - 1);
	}

	public List<Node> getPath() {
		return (List<Node>) Collections.unmodifiableCollection(path);
	}

	public double getDistance() {
		return distance;
	}

	public int getHops() {
		return path.size();
	}

	Path(Node from, Node to, double distance) {
		path = new ArrayList<>();
		path.add(from);
		path.add(to);
		this.distance = distance;
	}

	Path(List<Node> path, double distance) {
		this.path = path;
		this.distance = distance;
	}

	@Override
	public String toString() {
		return path.stream().map(n -> "N_" + n.getId()).collect(Collectors.joining(", ", "[", "]")) + "(" + distance
				+ ")";
	}
}
