/**
 *
 */
package model;

/**
 * @author federico
 *
 */
public class Ambulance extends NodeContent {
	private static int ID = 0;

	private final int id;
	private Patient patient;
	private boolean clean;

	Ambulance(int node) {
		this(node, ID++, null, true);
	}

	Ambulance(int node, int id, Patient patient, boolean clean) {
		super(node);
		this.id = id;
		this.patient = patient;
		this.clean = clean;

		ID = Math.max(ID, id + 1); // avoid id collisions, ID is always greater
									// than max id
	}

	void clean() {
		if (!isFree())
			throw new IllegalStateException("Can not clean with patient onboard");
		clean = true;
	}

	@Override
	public int getId() {
		return id;
	}

	public Patient getPatient() {
		return patient;
	}

	public boolean isClean() {
		return clean;
	}

	public boolean isFree() {
		return patient == null;
	}

	void load(Patient patient) {
		if (!isFree())
			throw new IllegalStateException("Patient already on board");
		// if (!isClean()) {
		// throw new IllegalStateException("Ambulance not clean");
		// }
		this.patient = patient;
		clean = false;
	}

	@Override
	public String toString() {
		return String.format("A_%d [%s] @ N%d %s", id, isFree() ? "" : patient.toString(), getNode(),
				isClean() ? "" : "*");
	}

	Patient unload() {
		if (isFree())
			throw new IllegalStateException("No patient onboard");
		Patient p = patient;
		patient = null;
		return p;
	}
}
