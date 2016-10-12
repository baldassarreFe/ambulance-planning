package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import model.Action;
import model.Ambulance;
import model.CityMap;
import model.CityMap.Print;
import model.CityParser;
import model.Patient;
import planner.Planner;

public class Main {
	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// args parsing
		String cityFileName = args[0];
		String plannerName = args[1];

		// initial set up
		Planner p = (Planner) Class.forName(plannerName).newInstance();
		CityMap map = CityParser.parse(cityFileName);

		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println(map.represent(Print.HOSPITAL_LOCATIONS));
		System.out.println(map.represent(Print.PATIENT_LOCATIONS));
		System.out.println(map.represent(Print.DEMANDS));

		Map<Ambulance, List<Action>> plan = null;
		do {
			// if we don't have a plan make one
			if (plan == null) {
				plan = p.solve(map);
			}

			// print full plan
			for (Ambulance amb : map.getAmbulances()) {
				System.out.println("Actions for " + amb);
				for (Action a : plan.get(amb)) {
					System.out.println(a);
				}
				System.out.println();
			}

			for (Ambulance amb : map.getAmbulances()) {
				if (!plan.get(amb).isEmpty()) {
					Action a = plan.get(amb).remove(0);
					System.out.println("Executing: " + a);
					map.performAction(a);
				}
			}

			// prompt user to spawn a patient
			System.out.println("Want to add a patient?");
			String answer = in.readLine().trim();
			if (!answer.isEmpty()) {
				// parse user input
				Patient patient = new Patient(2, 3);
				map.spawn(patient);
				// invalidate plan
				plan = null;
			}
		} while (plan == null || !plan.values().stream().allMatch(List::isEmpty));

	}
}
