package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
	private static int ID = 0;

	private final int id;
	private final List<NodeContent> contents;
	private final int request;
	private final double x;
	private final double y;
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}	

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
	Node(double x, double y, int request) {
		this(x, y, request, ID++);
	}

	Node(double x, double y, int request, int id) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.request = request;
		this.contents = new ArrayList<>();
	}

	void spawn(Patient patient) {
		contents.add((NodeContent) patient);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}
