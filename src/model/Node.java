package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
	private static int ID = 0;

	private final int id;
	private final List<NodeContent> contents;
	private final int request;
	

	public int getId() {
		return id;
	}

	public int getRequest() {
		return request;
	}

	public boolean containsAmbulance() {
		return containsA(Ambulance.class);
	}

	public boolean containsHospital() {
		return containsA(Hospital.class);
	}

	public boolean containsPatient() {
		return containsA(Patient.class);
	}

	private boolean containsA(Class<?> contentType) {
		return contents.stream().anyMatch(x -> x.getClass() == contentType);
	}

	public List<Ambulance> getAmbulances() {
		return (List<Ambulance>) extractAll(Ambulance.class);
	}

	public List<Hospital> getHospitals() {
		return (List<Hospital>) extractAll(Hospital.class);
	}

	public List<Patient> getPatients() {
		return (List<Patient>) extractAll(Patient.class);
	}

	private List<?> extractAll(Class<?> contentType) {
		return contents.stream().filter(x -> x.getClass() == contentType).map(x -> contentType.cast(x)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return String.format("N_%d (%d) [%s]", id, request,
				contents.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}

	// package-private methods
	public Node(int request) {
		this(++ID, request);
	}

	Node(int id, int request) {
		this.id = id;
		this.request = request;
		this.contents = new ArrayList<>();
	}

	void spawn(Patient patient) {
		contents.add((NodeContent) patient);
	}
}
