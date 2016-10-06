package model;

public class ActionPick extends Action {
	
	private Ambulance ambulance;
	private Node at;
	private Hospital h;

	public ActionPick(Ambulance a, Node at, Hospital h) {
		this.ambulance = a;
		this.at = at;
		this.h = h;
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
