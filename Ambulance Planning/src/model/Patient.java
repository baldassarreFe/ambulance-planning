package model;

public class Patient implements NodeContent {
private static int ID = 0;
	
	private final int id;
	private final int severity;
	
	public int getId() {
		return id;
	}
	
	public int getSeverity() {
		return severity;
	}
	
	public String toString() {
		return String.format("P_%d (%d)", id, severity);
	}
	
	// package-private methods
	
	Patient(int severity) {
		this(++ID, severity);
	}
	
	Patient(int id, int severity) {
		this.id = id;
		this.severity = severity;
	}
}
