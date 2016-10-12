package model;


import java.io.BufferedReader;
import java.io.IOException;

public class ManualPatientProvider extends PatientProvider {

	private BufferedReader in;
	private String lastAnswer = null;

	public ManualPatientProvider(BufferedReader in) {
		this.in = in;
	}

	@Override
	public boolean hasNewPatient() {
		System.out.println("Want to add a patient?");
		try {
			lastAnswer = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (lastAnswer == null) {
			return false;
		}
		lastAnswer = lastAnswer.trim();
		return !lastAnswer.isEmpty();
	}

	@Override
	public Patient getNewPatient() {
		// parse user input
		String[] lines = lastAnswer.split("\\s+");
		lastAnswer = null;
		return new Patient(Integer.parseInt(lines[0]), Integer.parseInt(lines[1]));
	}
}
