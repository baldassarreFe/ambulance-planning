package model;


public abstract class PatientProvider {

	public PatientProvider() {
	}

	public abstract boolean hasNewPatient();

	public abstract Patient getNewPatient();

}
