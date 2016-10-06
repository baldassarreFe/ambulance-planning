package model;

public class ActionMove extends Action {
	
	private Ambulance ambulance;
	private Node from;
	private Node to;

	public ActionMove(Ambulance a, Node from, Node to) {
		this.ambulance = a;
		this.from = from;
		this.to = to;
	}

	@Override
	protected void checkPreconditions(CityMap cityMap) {
		// TODO
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		// TODO
	}
}
