package model;

public abstract class Action {
	void performAction(CityMap cityMap) {
		checkPreconditions(cityMap);
		applyEffects(cityMap);
	}
	
	abstract protected void checkPreconditions(CityMap cityMap);
	abstract protected void applyEffects(CityMap cityMap);
}
