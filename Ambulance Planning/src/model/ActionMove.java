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
		if (ambulance.getNode() != from || !cityMap.areAdjacent(from, to))
			throw new IllegalStateException();
	}

	@Override
	protected void applyEffects(CityMap cityMap) {
		cityMap.getContentAt(from).remove(ambulance);
		ambulance.setNode(to);
		cityMap.getContentAt(to).add(ambulance);
	}

	@Override
	public String toString() {
		return String.format("move(A%d %d -> %d)", ambulance.getId(), from, to);
	}
}
