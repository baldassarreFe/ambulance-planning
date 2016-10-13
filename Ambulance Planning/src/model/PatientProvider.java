package model;

public abstract class PatientProvider {

	public PatientProvider() {
	}

	public abstract Patient getNewPatient();

	public abstract boolean hasNewPatient();

}
