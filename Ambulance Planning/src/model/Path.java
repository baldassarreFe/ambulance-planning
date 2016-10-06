package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {
	private final int distance;
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

	public int getDistance() {
		return distance;
	}

	public int getHops() {
		return path.size();
	}

	Path(Node from, Node to, int distance) {
		path = new ArrayList<>();
		path.add(from);
		path.add(to);
		this.distance = distance;
	}

	Path(List<Node> path, int distance) {
		this.path = path;
		this.distance = distance;
	}

}
