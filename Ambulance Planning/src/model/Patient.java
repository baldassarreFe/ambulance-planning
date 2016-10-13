package model;

public class Patient extends NodeContent {
	private static int COUNT = 0;

	private final int id;
	private final int severity;

	private boolean waiting = true;
	private boolean inHospital = false;

	public int getId() {
		return id;
	}

	public int getSeverity() {
		return severity;
	}

	public String toString() {
		return String.format("P%d (%d) @ N%d", id, severity, getNode());
	}

	// package-private methods

	public Patient(int node, int severity) {
		this(node, -1, severity);
	}

	Patient(int node, int id, int severity) {
		super(node);
		this.id = id == -1 ? COUNT : id;
		COUNT++;
		this.severity = severity;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void load() {
		waiting = false;
	}

	public boolean isInHospital() {
		return inHospital;
	}

	public void unload() {
		inHospital = true;
	}
}
