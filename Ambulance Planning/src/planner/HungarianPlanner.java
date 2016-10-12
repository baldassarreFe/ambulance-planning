package planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import model.Action;
import model.ActionDrop;
import model.ActionMove;
import model.ActionPick;
import model.Ambulance;
import model.CityMap;

public class HungarianPlanner extends Planner {

	@Override
	public List<Action> solve(CityMap map) {
		int availAmb = (int) map.getAmbulances().stream().filter(Ambulance::isFree).count();
		int numPat = map.patientCount();
		int[] centroids = new int[0];
		if (availAmb > numPat) {
			int numCentroid = availAmb - numPat;
			centroids = MaxCoverage.findMaxCoverageLocations(numCentroid, map);
		}

		// must be a square matrix:
		// - amb x (pat+cen) if pat<=amb
		// - (amb+padding) x pat if pat>amb
		int columnCount = numPat + centroids.length;
		int rowCount = availAmb>numPat?numPat:availAmb; 
		int[][] shortestDistances = new int[rowCount][columnCount];
		for (int amb = 0; amb < availAmb; amb++) {
			int ambNode = map.getAmbulances().get(amb).getNode();
			for (int pat = 0; pat < numPat; pat++) {
				// total distance amb->pat->hos / severity factor
				int hosp = map.closestHospital(pat);
				int patNode = map.getPatients().get(pat).getNode();
				int hosNode = map.closestHospital(patNode);
				shortestDistances[amb][pat] = (int) Math.round(map.shortestDistance(ambNode, patNode) + map.shortestDistance(patNode, hosNode));
				shortestDistances[amb][pat] =  (int) Math.round(shortestDistances[ambNode][patNode] *3.0/ map.getPatients().get(pat).getSeverity());
			}
			for (int cen = 0; cen < centroids.length; cen++) {
				shortestDistances[amb][numPat+cen] = (int) Math.round(map.shortestDistance(ambNode, centroids[cen]));
			}
		}
		for (int padding = availAmb; padding < rowCount; padding++) {
			for (int colum = 0; colum < columnCount; colum++) {
				shortestDistances[padding][colum] = 0;
			}
		}
		
		List<Action> plan = new ArrayList<>();
		
		int[] destinations = AssignmentProblemSolver.solve(shortestDistances);
		for (int amb = 0; amb < availAmb; amb++) {
			int ambNode = map.getAmbulances().get(amb).getNode();
			int target = destinations[amb];
			if (target<numPat) {
				int patNode = map.getPatients().get(target).getNode();
				int hosNode = map.closestHospital(patNode);
				ArrayList<Integer> pathToPat = map.shortestPath(ambNode, patNode);
				for (int i = 0; i < pathToPat.size()-1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(amb), pathToPat.get(i), pathToPat.get(i+1)));
				}
				plan.add(new ActionPick(map.getAmbulances().get(amb), patNode, map.getPatients().get(target)));
				
				ArrayList<Integer> pathToHos = map.shortestPath(patNode, hosNode);
				for (int i = 0; i < pathToHos.size()-1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(amb), pathToHos.get(i), pathToHos.get(i+1)));
				}
				plan.add(new ActionDrop(map.getAmbulances().get(amb), hosNode, map.getPatients().get(target)));
			} else {
				int cenNode = centroids[target];
				ArrayList<Integer> pathToCen = map.shortestPath(ambNode, cenNode);
				for (int i = 0; i < pathToCen.size()-1; i++) {
					plan.add(new ActionMove(map.getAmbulances().get(amb), pathToCen.get(i), pathToCen.get(i+1)));
				}
			}
		}
		
		return plan;
	}

}
