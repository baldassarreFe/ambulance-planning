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
	
	public int getId() {
		return id;
	}
	
	public boolean isClean() {
		return clean;
	}
	
	public boolean isFree() {
		return this.patient==null;
	}

	public Patient getPatient() {
		return patient;
	}
	
	public String toString() {
		return String.format("A_%d [%s] %s", id, isFree()?"":patient.toString(), isClean()?"":"***");
	}

	// package-private methods
	
	Ambulance(int node) {
		this(node, ID++, null, true);
	}
	
	Ambulance(int node, int id, Patient patient, boolean clean) {
		super(node);
		this.id = id;
		this.patient = patient;
		this.clean = clean;
	}

	void load(Patient patient) {
		if (!isFree()) {
			throw new IllegalStateException("Patient already on board");
		}
		if (!isClean()) {
			throw new IllegalStateException("Ambulance not clean");
		}
		this.patient = patient;
		this.clean = false;
	}
	
	Patient unload() {
		if (isFree()) {
			throw new IllegalStateException("No patient onboard");
		}
		Patient p = this.patient;
		this.patient = null;
		return p;
	}
	
	void clean() {
		if (!isFree()) {
			throw new IllegalStateException("Can not clean with patient onboard");
		}
		this.clean = true;
	}
}
