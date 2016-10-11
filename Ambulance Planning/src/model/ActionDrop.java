package model;

public class ActionDrop extends Action {
	
	private Ambulance ambulance;
	private int at;
	private Patient p;

	public ActionDrop(Ambulance a, int at, Patient p) {
		this.ambulance = a;
		this.at = at;
		this.p = p;
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
