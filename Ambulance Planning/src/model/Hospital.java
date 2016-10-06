package model;

public class Hospital {
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
	
	Hospital(int maxSeverity) {
		this(++ID, maxSeverity);
	}
	
	Hospital(int id, int maxSeverity) {
		this.id = id;
		this.maxSeverity = maxSeverity;
	}
	
	void accept(Patient patient) {
		if (patient.getSeverity()>maxSeverity) {
			throw new IllegalStateException("Hospital can not accept patient");
		}
	}
}
