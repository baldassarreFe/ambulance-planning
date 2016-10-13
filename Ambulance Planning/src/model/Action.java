package model;

public abstract class Action {
	abstract protected void applyEffects(CityMap cityMap);

	abstract protected void checkPreconditions(CityMap cityMap);

	void performAction(CityMap cityMap) {
		checkPreconditions(cityMap);
		applyEffects(cityMap);
	}
}
