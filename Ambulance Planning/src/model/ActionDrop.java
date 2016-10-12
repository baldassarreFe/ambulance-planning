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
		assert ambulance.getNode() == at;  // ambulance at location
		assert cityMap.getContentAt(at).stream().anyMatch(c -> c instanceof Hospital);  // hospital at location
		assert ambulance.getPatient() == p;  // patient is the same
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.unload();
		p.unload();
		p.setNode(at);
	}
}
