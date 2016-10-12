package model;


import java.util.Random;
import java.util.stream.DoubleStream;

public class RandomPatientProvider extends PatientProvider {

	private Random random = new Random(504);
	private double prob;

	private double[] demands;
	private double demandSum;

	public RandomPatientProvider(double prob, CityMap map) {
		this.prob = prob;

		demands = map.getDemands().stream().mapToDouble(x -> x).toArray();
		demandSum = DoubleStream.of(demands).sum();
	}

	@Override
	public boolean hasNewPatient() {
		return random.nextDouble() < prob;
	}

	@Override
	public Patient getNewPatient() {
		double demand = random.nextDouble() * demandSum;
		int node = 0;
		while (node < demands.length - 1 && demand > 0) {
			demand -= demands[node];
			node++;
		}
		return new Patient(node, 1 + random.nextInt(3));
	}
}
