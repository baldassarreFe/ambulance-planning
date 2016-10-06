package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import model.Action;
import model.CityMap;
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

		List<Action> plan = null;
		do {
			// if we don't have a plan make one
			if (plan == null)
				plan = p.solve(map);
			
			// print full plan
			for (Action action : plan) {
				System.out.println(action);
			}

			// remove from the plan the actions that can be done concurrently in
			// the next step
			List<Action> nextStepActions = Planner.pickConcurrentActions(plan);
			
			// print actions planned for next step
			for (Action action : nextStepActions) {
				System.out.println(action);
			}
			map.performActions(nextStepActions);
			
			// prompt user to spawn a patient
			System.out.println("Want to add a patient?");
			String answer = in.readLine().trim();
			if (!answer.isEmpty()) {
				// parse user input
				Patient patient = null;
				int nodeId = 0;
				map.spawn(patient, nodeId);
				// invalidate plan
				plan = null;
			}
		} while (plan == null || !plan.isEmpty());

	}
}
