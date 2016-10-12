package model;

public class ActionPick extends Action {
	
	private Ambulance ambulance;
	private int at;
	private Patient p;

	public ActionPick(Ambulance a, int at, Patient p) {
		this.ambulance = a;
		this.at = at;
		this.p = p;
	}

	@Override
	protected void checkPreconditions(CityMap cityMap) {
		assert p.isWaiting();
		assert ambulance.isFree();
		assert p.getNode() == at;
		assert ambulance.getNode() == at;
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.load(p);
		p.load();
	}
	
	@Override
	public String toString() {
		return String.format("pick(A%d P%d @ N%d)", ambulance.getId(), p.getId(), at);
	}
}
