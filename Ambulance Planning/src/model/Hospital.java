package model;

public class Hospital extends NodeContent {
private static int ID = 0;
	
	private final int id;
	private final int maxSeverity;
	
	public int getId() {
		return id;
	}
	
	public int getMaxSeverity() {
		return maxSeverity;
	}
	
	public String toString() {
		return String.format("H_%d (%d)", id, maxSeverity);
	}
	
	// package-private methods
	
	Hospital(int node, int maxSeverity) {
		this(node, ++ID, maxSeverity);
	}
	
	Hospital(int node, int id, int maxSeverity) {
		super(node);
		this.id = id;
		this.maxSeverity = maxSeverity;
	}
	
	void accept(Patient patient) {
		if (patient.getSeverity()>maxSeverity) {
			throw new IllegalStateException("Hospital can not accept patient");
		}
	}
}
