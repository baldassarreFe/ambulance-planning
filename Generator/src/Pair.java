/**
 * Class that represents a pair of values.
 * In this case, Cartesian coordinates
 * @author Team 14
 *
 * @param <X>
 * @param <Y>
 */
public class Pair<X, Y> {
	X x;
	Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return x + " " + y;
		
	}
}
