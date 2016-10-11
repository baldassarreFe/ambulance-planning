package planner;


import model.*;
import utils.Pair;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Planner that uses PSO as a tool to find better solution.
 */
public class PSOPlanner extends Planner {

	private PSO pso;
	private PSO.PSOEvaluator evaluator;

	private CityMap map;

	private List<Node> ambulances;
	private List<Node> patients;
	private List<Node> hospitals;
	private int ambCnt;
	private int patCnt;
	private int hosCnt;

	private int particleDims;
	private double[][] particleBounds;

	private int[][] optHospitals;
	private double[][] optHospitalsDist;
	private int[] singleOptHospitals;
	private double[] singleOptHospitalsDist;

	@Override
	public List<Action> solve(CityMap map) {
		this.map = map;

		// todo: (!!!) this is not the correct way to get lists of all ambulances/patients/hospitals
		ambulances = map.getAmbulancesLocation();
		patients = map.getPatientsLocation();
		hospitals = map.getHospitalsLocation();
		ambCnt = ambulances.size();
		patCnt = patients.size();
		hosCnt = hospitals.size();

		// Handle bounds for particles
		particleDims = ambCnt * 2 + patCnt;
		buildBounds();

		precalcOptimalHospitals();

		// Initialize PSO
		evaluator = new VRPEvaluator();
		pso = new PSO(evaluator, particleDims, particleBounds);

		// Find solution
		double[] particle = pso.run(Long.MAX_VALUE);
		Plan plan = decodePlan(particle);

		return plan.toMainRepresentation();
	}

	/**
	 * Required for PSO to initialize first particles correctly.
	 * <p>
	 * Updates <code>particleBounds</code> array.
	 */
	private void buildBounds() {
		particleBounds = new double[particleDims][2];

		/*
		 * Get bounding box for the map.
		 */
		double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
		for (Node node : map.getNodes()) {
			double x = node.getX();
			double y = node.getY();
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}

		/*
		 * Set correct values in particleBounds.
		 */
		for (int i = 0; i < ambCnt * 2; i += 2) {
			particleBounds[i][0] = minX;
			particleBounds[i][1] = maxX;
			particleBounds[i + 1][0] = minY;
			particleBounds[i + 1][1] = maxY;
		}
		for (int i = ambCnt * 2; i < ambCnt * 2 + patCnt; i++) {
			particleBounds[i][0] = 0;
			particleBounds[i][1] = 1;
		}
	}

	/**
	 * Builds <code>optimalHospitals</code> array.
	 * Asymptotic: O(patCnt^2 * hosCnt).
	 * <p>
	 * <code>optHospitals[i][j] - the best hospital to go to with ith patient before going to jth</code>
	 * <code>singleOptHospitals[i] - the best hospital to go to with ith patient</code>
	 */
	private void precalcOptimalHospitals() {
		optHospitals = new int[patCnt][patCnt];
		optHospitalsDist = new double[patCnt][patCnt];
		singleOptHospitals = new int[patCnt];
		singleOptHospitalsDist = new double[patCnt];

		for (int i = 0; i < patCnt; i++) {
			for (int j = 0; j < patCnt; j++) {
				optHospitals[i][j] = -1;
				for (int k = 0; k < hosCnt; k++) {
					if (isValidHospital(patients.get(i), hospitals.get(k))) {
						double curDist = shortestPath(patients.get(i), hospitals.get(k)).getDistance()
								+ shortestPath(hospitals.get(k), patients.get(j)).getDistance();
						if (optHospitals[i][j] == -1 || optHospitalsDist[i][j] > curDist) {
							optHospitals[i][j] = k;
							optHospitalsDist[i][j] = curDist;
						}
					}
				}
			}
			singleOptHospitals[i] = -1;
			for (int k = 0; k < hosCnt; k++) {
				if (isValidHospital(patients.get(i), hospitals.get(k))) {
					double curDist = shortestPath(patients.get(i), hospitals.get(k)).getDistance();
					if (singleOptHospitals[i] == -1 || singleOptHospitalsDist[i] > curDist) {
						singleOptHospitals[i] = k;
						singleOptHospitalsDist[i] = curDist;
					}
				}
			}
		}
	}

	/**
	 * Check if the given hospital can accept the given patient.
	 */
	private boolean isValidHospital(Node patient, Node hospital) {
		/*
		 * Currently every hospital can take care of any patient.
		 */
		return true;
	}


	/**
	 * Transform PSO particle into a valid routes.
	 * <p>
	 * It's the heart of algorithm. For example, the method can be used to generate a good valid routes
	 * from a random particle, without actual use of PSO.
	 */
	private Plan decodePlan(double[] particle) {
		/*
		 * Split particle into specific subarrays.
		 */
		double[] ambDims = Arrays.copyOfRange(particle, 0, ambCnt * 2); // todo: maybe try to remove this part and use current ambulances' coordinates
		double[] patDims = Arrays.copyOfRange(particle, ambCnt * 2, ambCnt * 2 + patCnt);

		/*
		 * Sort patients according to their priorities (here priorities are numbers from patDims, they are not
		 * the same as real patient priorities).
		 */
		int[] patientsSorted = Utils.getSortedIndices(patDims);

		/*
		 * Get matrix of ambulance priorities for each patient, according to ambulances "positions" from the particle.
		 */
		int[][] ambPriorities = getAmbPriorities(ambDims);

		/*
		 * Add patients to the routes.
		 */
		Plan plan = new Plan(ambCnt);
		for (int patient : patientsSorted) {
			// Insert the patient into the routes.
			// Test only first few ambulances (currently: at least 2, but not more than 33% or 10)
			int ambsToTest = Math.max(Math.min(ambCnt, 2), Math.min(ambCnt / 3, 10));
			double bestInsertionCost = Double.POSITIVE_INFINITY;
			int insertionAmbulance = -1;
			int insertionIndex = -1;
			for (int i = 0; i < ambsToTest; i++) {
				int ambulance = ambPriorities[patient][i];
				Pair<Integer, Double> curInsertion = tryInsert(plan.routes[ambulance], ambulance, patient);
				if (curInsertion.y < bestInsertionCost) {
					bestInsertionCost = curInsertion.y;
					insertionAmbulance = ambulance;
					insertionIndex = curInsertion.x;
				}
			}

			/*
			 * Insert patient into chosen position in the routes.
			 */
			plan.planCost += bestInsertionCost;
			plan.routes[insertionAmbulance].add(insertionIndex, patient);

		}

		return plan;
	}

	/**
	 * Find the best spot in the route to insert new patient.
	 * <p>
	 * Method does not change the given list.
	 *
	 * @param ambPlan list of patients to visit
	 * @param amb     index of an ambulance
	 * @param pat     new patient
	 * @return pair of insertion index and planCost delta
	 */
	private Pair<Integer, Double> tryInsert(List<Integer> ambPlan, int amb, int pat) {
		Node ambNode = ambulances.get(amb);
		Node patNode = patients.get(pat);
		/*
		 * Special case of empty routes.
		 */
		if (ambPlan.size() == 0) {
			return new Pair<>(0, shortestPath(ambNode, patNode).getDistance());
		}
		/*
		 * Find bounds where patient can be placed considering his priority.
		 * Can use two binary searches, but that is unnecessary.
		 */
		int patPriority = patients.get(pat).getRequest();
		int l = 0;
		int r = 0;
		for (; r < ambPlan.size(); r++) {
			int p = patients.get(ambPlan.get(r)).getRequest();
			if (patPriority > p) {
				break;
			} else if (patPriority < p) {
				l = r + 1;
			}
		}

		/*
		 * Find best spot to insert patient.
		 */
		Pair<Integer, Double> pair = null;
		for (int i = l; i <= r; i++) {
			double diff;
			if (i == 0) {
				int next = ambPlan.get(i);
				Node nextNode = patients.get(next);
				diff = -shortestPath(ambNode, nextNode).getDistance()
						+ shortestPath(ambNode, patNode).getDistance()
						+ optHospitalsDist[pat][next];
			} else if (i == ambPlan.size()) {
				int prev = ambPlan.get(i - 1);
				diff = -singleOptHospitalsDist[prev]
						+ optHospitalsDist[prev][pat]
						+ singleOptHospitalsDist[pat];
			} else {
				int prev = ambPlan.get(i - 1);
				int next = ambPlan.get(i);
				diff = -optHospitalsDist[prev][next]
						+ optHospitalsDist[prev][pat]
						+ optHospitalsDist[pat][next];
			}
			if (pair == null || pair.y > diff) {
				pair = new Pair<>(i, diff);
			}
		}

		assert pair != null;

		return pair;
	}

	/**
	 * Get matrix of ambulance priorities for each patient, according to ambulances "positions" from the particle.
	 */
	private int[][] getAmbPriorities(double[] ambDims) {
		/*
		 * Fill 'distances' array.
		 * dist[i][j] - squared distance from ith patient to jth ambulance's point.
		 */
		double[][] dist = new double[patCnt][ambCnt];
		for (int i = 0; i < patCnt; i++) {
			Node patient = patients.get(i);
			double px = patient.getX();
			double py = patient.getY();
			for (int j = 0; j < ambCnt; j++) {
				double ax = ambDims[j * 2];
				double ay = ambDims[j * 2 + 1];
				dist[i][j] = Utils.distSqr(px, py, ax, ay);
			}
		}

		/*
		 * Fill matrix of priorities.
		 * priorities[i] - array of ambulances' indices for ith patient.
		 */
		int[][] priorities = new int[patCnt][ambCnt];
		for (int i = 0; i < patCnt; i++) {
			priorities[i] = Utils.getSortedIndices(dist[i]);
		}

		return priorities;
	}

	/**
	 * Evaluate the routes.
	 *
	 * @param plan list of actions
	 * @return value to minimize
	 */
	private double evaluatePlan(Plan plan) {
		return plan.planCost;
	}

	/**
	 * Apply additional heuristics to enhance the existing routes.
	 *
	 * @param plan current routes
	 * @return improved routes
	 */
	private List<Action> applyOptimizations(List<Action> plan) {
		return plan;  // todo: when main part works, implement this (2-opt, for example) (it's an important part)
	}


	/**
	 * Wrapper for shortestPath method in CityMap.
	 */
	private Path shortestPath(Node a, Node b) {
		return map.shortestPath(a.getId(), b.getId());
	}


	/**
	 * Implementation of PSO Evaluator for VRP problem.
	 */
	public class VRPEvaluator extends PSO.PSOEvaluator {

		@Override
		public double evaluate(double[] particle) {
			Plan plan = decodePlan(particle);
			return evaluatePlan(plan);
		}
	}

	/**
	 * Representation of a plan that is used in algorithm.
	 */
	private class Plan {

		private List<Integer>[] routes;
		private double planCost;

		public Plan(int ambCnt) {
			routes = new List[ambCnt];
			for (int i = 0; i < ambCnt; i++) {
				routes[i] = new ArrayList<>();
			}

			planCost = 0;
		}

		/**
		 * Transform plan from inner representation into real one.
		 *
		 * @return plan representation used by model
		 */
		private List<Action> toMainRepresentation() {

			System.out.println(toString()); // todo: remove debug

			List<Action> actions = new ArrayList<>();

			// todo

//			for (int i = 0; i < routes.length; i++) {
//				int id = ambulances.get(i).getId();
//
//			}

			return actions;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("Plan cost: ").append(planCost).append("\n");
			for (int i = 0; i < routes.length; i++) {
				int id = ambulances.get(i).getId();
				str.append("A").append(id).append(" ->");
				for (int j = 0; j < routes[i].size() - 1; j++) {
					int cur = routes[i].get(j);
					int nxt = routes[i].get(j + 1);
					int hos = optHospitals[cur][nxt];
					str.append(" P").append(patients.get(cur).getId())
							.append(" H").append(hospitals.get(hos).getId());
				}
				int cur = routes[i].get(routes[i].size() - 1);
				int hos = singleOptHospitals[cur];
				str.append(" P").append(patients.get(cur).getId())
						.append(" H").append(hospitals.get(hos).getId());
				str.append("\n");
			}

			return str.toString();
		}
	}

}
