package model;

public class ActionMove extends Action {
	
	private Ambulance ambulance;
	private int from;
	private int to;

	public ActionMove(Ambulance a, int from, int to) {
		this.ambulance = a;
		this.from = from;
		this.to = to;
	}

	@Override
	protected void checkPreconditions(CityMap cityMap) {
		assert ambulance.getNode() == from;
		assert cityMap.areAdjacent(from, to);
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.setNode(to);
	}
}
