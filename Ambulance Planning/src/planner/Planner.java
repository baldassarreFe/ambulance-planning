package planner;

import java.util.List;

import model.Action;
import model.CityMap;
import model.CityMap2;

public abstract class Planner {
	public abstract List<Action> solve(CityMap2 map);

	public static List<Action> pickConcurrentActions(List<Action> plan) {
		// TODO find the first action for every ambulance, remove them and return them
		return null;
	}
}
