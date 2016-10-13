package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Action;
import model.ActionDrop;
import model.Ambulance;
import model.CityMap;
import model.CityMap.Print;
import model.CityParser;
import model.Patient;
import planner.HungarianPlanner;
import planner.Planner;

public class Main {
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// args parsing
		String cityFileName = args[0];

		// debugging utilities
		new File("logs").mkdir();
		String now = new SimpleDateFormat(".yyyy-MM-dd_HH.mm.ss").format(new Date());
		String easyToReadDescription = "logs/" + cityFileName.split(".pddl")[0] + now + ".descr";
		String solution = "logs/" + cityFileName.split(".pddl")[0] + now + ".plan";

		// initial set up
		Planner planner = new HungarianPlanner();
		CityMap map = CityParser.parse(cityFileName);
		Files.copy(new File(cityFileName).toPath(), new File(easyToReadDescription).toPath());

		PrintWriter solutionWriter = new PrintWriter(solution);
		PrintWriter eventsWriter = new PrintWriter(easyToReadDescription);

		System.out.println(map.represent(Print.ADJ_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		System.out.println(map.represent(Print.SHORTEST_PATHS));
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));
		System.out.println(map.represent(Print.HOSPITAL_LOCATIONS));
		System.out.println(map.represent(Print.PATIENT_LOCATIONS));
		System.out.println(map.represent(Print.DEMANDS));

		eventsWriter.println(map.represent(Print.ADJ_MATRIX));
		eventsWriter.println(map.represent(Print.SHORTEST_DISTANCES_MATRIX));
		eventsWriter.println(map.represent(Print.SHORTEST_PATHS));
		eventsWriter.println(map.represent(Print.HOSPITAL_LOCATIONS));
		eventsWriter.println(map.represent(Print.DEMANDS));
		eventsWriter.println("\n--------------------------------------------------\n");

		Map<Ambulance, List<Action>> plan = null;
		int step = 0;
		boolean replanningNeeded = true;
		do {
			System.out.println("\n--------------------------------------------------");
			System.out.println("Step: " + step);
			solutionWriter.println("Step:" + step);
			eventsWriter.println("Step:" + step);
			step++;
			

			// if we don't have a plan make one
			if (replanningNeeded) {
				System.out.println("Replanning...");
				eventsWriter.println("  Replanning...");
				plan = planner.solve(map);
				replanningNeeded = false;
			}

			System.out.print(map.represent(Print.AMBULANCES_LOCATIONS));
			System.out.print(map.represent(Print.PATIENT_LOCATIONS));

			// print full plan
			for (Ambulance amb : plan.keySet()) {
				System.out.println("Actions for " + amb);
				if (!plan.get(amb).isEmpty()) {
					for (Action a : plan.get(amb)) {
						System.out.println("   " + a);
					}
				} else {
					System.out.println("   nop");
				}
			}

			for (Ambulance amb : plan.keySet()) {
				if (!plan.get(amb).isEmpty()) {
					Action a = plan.get(amb).remove(0);
					System.out.println("Executing: " + a);
					solutionWriter.println("  " + a);
					eventsWriter.println("  " + a);
					map.performAction(a);
					if (a instanceof ActionDrop)
						replanningNeeded = true;
				}
			}

			// prompt user to spawn a patient

			System.out.printf(
					"Want to add a patient?\n(enter to continue, r for random, syntax: [node(0-%d) severity(1-3)]* | r, ex: 5 3 7 1)\n > ",
					map.nodesCount());
			String answer = in.readLine();
			if (answer != null && !answer.trim().isEmpty()) {
				try {
					// parse user input
					if (answer.trim().equals("r")) {
						Random r = new Random();
						Patient patient = new Patient(r.nextInt(map.nodesCount()), r.nextInt(3) + 1);
						map.spawn(patient);
						// invalidate plan
						replanningNeeded = true;
						System.out.println("Added " + patient);
						eventsWriter.println("  Added " + patient);
					} else {
						String[] tokens = answer.trim().split(" ");
						for (int i = 0; i < tokens.length; i+=2) {
							int node = Integer.parseInt(tokens[i]);
							int severity = Integer.parseInt(tokens[i+1]);
							if (node < map.nodesCount() && severity >= 1 && severity <= 3) {
								Patient patient = new Patient(node, severity);
								map.spawn(patient);
								// invalidate plan
								replanningNeeded = true;
								System.out.println("Added " + patient);
								eventsWriter.println("  Added " + patient);
							} else {
								System.out.println("Nope");
							}
						}
					}
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					System.out.println("Format error");
				}
			}
		} while (replanningNeeded || !plan.values().stream().allMatch(List::isEmpty));

		System.out.println("\nDone!");
		System.out.println(map.represent(Print.AMBULANCES_LOCATIONS));

		eventsWriter.println("\nDone!");
		eventsWriter.println(map.represent(Print.AMBULANCES_LOCATIONS));

		solutionWriter.close();
		eventsWriter.close();
	}
}
