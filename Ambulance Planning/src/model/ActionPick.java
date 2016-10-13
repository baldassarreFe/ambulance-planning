package model;

public class ActionPick extends Action {

	private Ambulance ambulance;
	private int at;
	private Patient p;

	public ActionPick(Ambulance a, int at, Patient p) {
		ambulance = a;
		this.at = at;
		this.p = p;
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		ambulance.load(p);
		cityMap.getContentAt(at).remove(p);
		p.load();
	}

	@Override
	protected void checkPreconditions(CityMap cityMap) {
		if (!(p.isWaiting() && ambulance.isFree() && p.getNode() == at && ambulance.getNode() == at))
			throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return String.format("pick(A%d P%d @ N%d)", ambulance.getId(), p.getId(), at);
	}
}
