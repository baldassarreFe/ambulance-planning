package planner;

import java.util.ArrayList;
import java.util.List;

import model.Action;
import model.ActionMove;
import model.Ambulance;
import model.CityMap;
import model.Node;

public class RandomPlanner implements Planner {

	@Override
	public List<Action> solve(CityMap map) {
		Node from = map.getAmbulancesLocation().get(0);
		Node to = map.reachableFrom(from).get(0);
		Ambulance amb = from.getAmbulances().get(0);
		
		List<Action> plan = new ArrayList<>();
		plan.add(new ActionMove(amb, from, to));
		
		return plan;
	}

}
