package planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Action;
import model.ActionDrop;
import model.ActionMove;
import model.ActionPick;
import model.Ambulance;
import model.CityMap;
import model.Patient;

public class HungarianPlanner extends Planner {

	@Override
	public Map<Ambulance, List<Action>> solve(CityMap map) {
		int[] availAmbIds = map.getAmbulances().stream().filter(Ambulance::isFree).mapToInt(Ambulance::getId).toArray();
		int availAmb = availAmbIds.length;

		int[] patIds = map.getPatients().stream()
				.filter(p -> map.getAmbulances().stream()
						.noneMatch(a -> a.getPatient() != null && a.getPatient().getId() == p.getId()))
				.mapToInt(Patient::getId).toArray();
		int numPat = patIds.length;

		int[] centroids = new int[0];
		if (availAmb > numPat) {
			int numCentroid = availAmb - numPat;
			centroids = MaxCoverage.findMaxCoverageLocations(numCentroid, map);
		}

		// must be a square matrix:
		// - amb x (pat+cen) if pat<=amb
		// - (amb+padding) x pat if pat>amb
		int columnCount = numPat + centroids.length;
		int rowCount = availAmb;
		int[][] shortestDistances = new int[rowCount][columnCount];
		for (int amb = 0; amb < availAmb; amb++) {
			int ambNode = map.getAmbulances().get(availAmbIds[amb]).getNode();
			for (int pat = 0; pat < numPat; pat++) {
				// total distance amb->pat->hos / severity factor
				int patNode = map.getPatients().get(patIds[pat]).getNode();
				int hosNode = map.closestHospital(patNode);

				double dist = map.shortestDistance(ambNode, patNode) + map.shortestDistance(patNode, hosNode);
				shortestDistances[amb][pat] = (int) Math
						.round(dist * 3 / map.getPatients().get(patIds[pat]).getSeverity());
			}
			for (int cen = 0; cen < centroids.length; cen++) {
				shortestDistances[amb][numPat + cen] = (int) Math
						.round(3 * map.shortestDistance(ambNode, centroids[cen]));
			}
		}
		// for (int padding = availAmb; padding < rowCount; padding++) {
		// for (int colum = 0; colum < columnCount; colum++) {
		// shortestDistances[padding][colum] = 0;
		// }
		// }

		Map<Ambulance, List<Action>> bigplan = new HashMap<Ambulance, List<Action>>();

		int[] destinations = AssignmentProblemSolver.solve(shortestDistances);
		for (int ambIdx = 0; ambIdx < availAmb; ambIdx++) {
			int ambId = availAmbIds[ambIdx];
			Ambulance ambulance = map.getAmbulances().get(ambId);
			List<Action> plan = new ArrayList<>();
			int ambNode = map.getAmbulances().get(ambId).getNode();
			int column = destinations[ambIdx];
			if (column < numPat) {
				int patNode = map.getPatients().get(patIds[column]).getNode();
				int hosNode = map.closestHospital(patNode);
				ArrayList<Integer> pathToPat = map.shortestPath(ambNode, patNode);
				for (int i = 0; i < pathToPat.size() - 1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(ambId), pathToPat.get(i), pathToPat.get(i + 1)));
				}
				plan.add(
						new ActionPick(map.getAmbulances().get(ambId), patNode, map.getPatients().get(patIds[column])));

				ArrayList<Integer> pathToHos = map.shortestPath(patNode, hosNode);
				for (int i = 0; i < pathToHos.size() - 1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(ambId), pathToHos.get(i), pathToHos.get(i + 1)));
				}
				plan.add(
						new ActionDrop(map.getAmbulances().get(ambId), hosNode, map.getPatients().get(patIds[column])));
			} else {
				int cenNode = centroids[column - numPat];
				ArrayList<Integer> pathToCen = map.shortestPath(ambNode, cenNode);
				for (int i = 0; i < pathToCen.size() - 1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(ambId), pathToCen.get(i), pathToCen.get(i + 1)));
				}
			}
			bigplan.put(ambulance, plan);
		}

		// Ambs with a patient
		int[] busyAmbIds = map.getAmbulances().stream().filter(a -> !a.isFree()).mapToInt(Ambulance::getId).toArray();

		for (int ambIdx = 0; ambIdx < busyAmbIds.length; ambIdx++) {
			int ambId = busyAmbIds[ambIdx];
			Ambulance ambulance = map.getAmbulances().get(ambId);
			List<Action> plan = new ArrayList<>();
			int ambNode = map.getAmbulances().get(ambId).getNode();
			int patId = map.getAmbulances().get(ambId).getPatient().getId();
			int hosNode = map.closestHospital(ambNode);
			ArrayList<Integer> pathToHos = map.shortestPath(ambNode, hosNode);
			for (int i = 0; i < pathToHos.size() - 1; i++) {
				plan.add(new ActionMove(map.getAmbulances().get(ambId), pathToHos.get(i), pathToHos.get(i + 1)));
			}
			plan.add(new ActionDrop(map.getAmbulances().get(ambId), hosNode, ambulance.getPatient()));
			bigplan.put(ambulance, plan);
		}

		return bigplan;
	}

	@Override
	public boolean replanAfterDropAction() {
		return true;
	}
}
