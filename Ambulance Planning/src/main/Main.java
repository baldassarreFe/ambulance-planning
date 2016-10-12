package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import model.*;
import model.CityMap.Print;
import planner.HungarianPlanner;
import planner.PSOPlanner;
import planner.Planner;

public class Main {
	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// args parsing
		String cityFileName = args[0];

		// initial set up
		Planner p = new HungarianPlanner();
//		Planner p = new PSOPlanner();
		CityMap map = CityParser.parse(cityFileName);

		PatientProvider pProvider = new RandomPatientProvider(0.5, 10, map);
//		PatientProvider pProvider = new ManualPatientProvider(in);

		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println(map.represent(Print.HOSPITAL_LOCATIONS));
		System.out.println(map.represent(Print.PATIENT_LOCATIONS));
		System.out.println(map.represent(Print.DEMANDS));

		Map<Ambulance, List<Action>> plan = null;
		boolean replanningNeeded = true;
		do {
			// if we don't have a plan make one
			if (replanningNeeded) {
				System.out.println("Replanning...");
				plan = p.solve(map);
				replanningNeeded = false;
			}

			// print full plan
			for (Ambulance amb : plan.keySet()) {
				if (!plan.get(amb).isEmpty()) {
					System.out.println("Actions for " + amb);
					for (Action a : plan.get(amb)) {
						System.out.println("   " + a);
					}
					System.out.println();
				}
			}

			for (Ambulance amb : plan.keySet()) {
				if (!plan.get(amb).isEmpty()) {
					Action a = plan.get(amb).remove(0);
					System.out.println("Executing: " + a);
					map.performAction(a);
					if (a instanceof ActionDrop && p.replanAfterDropAction())
						replanningNeeded = true;
				}
			}

			// prompt user to spawn a patient
			if (pProvider.hasNewPatient()) {
				Patient patient = pProvider.getNewPatient();
				System.out.println("New patient: " + patient);
				map.spawn(patient);
				replanningNeeded = true;
			}
		} while (replanningNeeded || !plan.values().stream().allMatch(List::isEmpty));

	}
}
