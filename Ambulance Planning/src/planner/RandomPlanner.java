package planner;

import java.util.ArrayList;
import java.util.List;

import model.*;

public class RandomPlanner extends Planner {

	@Override
	public List<Action> solve(CityMap2 map) {
		int from = map.getAmbulancesLocations().get(0);
		int to = (int) map.adjacentNodes(from).toArray()[0];
		Ambulance amb = map.getAmbulances().get(0);
		
		List<Action> plan = new ArrayList<>();
//		plan.add(new ActionMove(amb, from, to));
		
		return plan;
	}

}
