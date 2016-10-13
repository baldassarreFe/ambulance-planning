package model;

public class Hospital extends NodeContent {
	private static int ID = 0;

	private final int id;
	private final int maxSeverity;

	Hospital(int node, int maxSeverity) {
		this(node, ++ID, maxSeverity);
	}

	Hospital(int node, int id, int maxSeverity) {
		super(node);
		this.id = id;
		this.maxSeverity = maxSeverity;

		ID = Math.max(ID, id + 1); // avoid id collisions, ID is always greater
									// than max id
	}

	void accept(Patient patient) {
		if (patient.getSeverity() > maxSeverity)
			throw new IllegalStateException("Hospital can not accept patient");
	}

	@Override
	public int getId() {
		return id;
	}

	public int getMaxSeverity() {
		return maxSeverity;
	}

	@Override
	public String toString() {
		return String.format("H_%d (%d)", id, maxSeverity);
	}
}
