package newBarter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import user_interface.Interface;
import elements.*;
import ilog.concert.IloException;

public class CG_Barter {

	public static List<Agent> agentSet = Interface.network.AgentSet;
	public static List<Task> taskSet = Interface.network.TaskSet;
	public static List<Resource> resourceSet = Interface.network.ResourceSet;
	public Agent designer = agentSet.get(0);

	/**
	 * [agent][resource]
	 */
	public static int[][] reportedType = new int[agentSet.size()][resourceSet
			.size()];

	public static class theta {
		int ID;
		int[] quantity = new int[resourceSet.size()];
		static int nextID = 0;

		public theta(Agent a) {
			ID = nextID++;
			quantity = a.getResourceSetAgent();
		}

		public theta() {
			ID = nextID++;
		}

		public int getID() {
			return ID;
		}

		public void setQuantity(int[] q) {
			quantity = q;
		}

		public int getQuantity(Resource r) {
			return quantity[r.getID()];
		}

	}

	static List<theta> thetaSet = new ArrayList<theta>();

	/**
	 * [agent][resource]
	 */

	public double[][] probability = new double[agentSet.size()][resourceSet
			.size()];

	public static List<Output> outputSet = new ArrayList<Output>();

	public static void runBarter() throws IloException {
		MP_Barter master;
		PP_Barter pp;
		int iterationCounter = 0;
		int maxIteration = 1;
		buildThetaSet();
		build_O_init_dummy();
		do {

			master = new MP_Barter();
			master.solve();

			// pp = new PP_Barter();
			// pp.solve();

			iterationCounter++;
		} while (iterationCounter < maxIteration);

		master.solveMIP();
	}

	private static void buildThetaSet() {
		for (Agent agent : agentSet) {
			theta t = new theta(agent);
			thetaSet.add(t);
		}
		for (int i = 0; i < resourceSet.size(); i++) {
			theta t = new theta();
			int[] q = new int[resourceSet.size()];
			for (int j = 0; j < resourceSet.size(); j++) {
				q[j] = (int) (Math.random() * Interface._maxEachTypePerAgent);
			}
			t.setQuantity(q);
			thetaSet.add(t);
		}
	}

	private static void build_O_init_dummy() {
		// TODO Auto-generated method stub
		Output output = new Output();
		int[][][] allocation = new int[agentSet.size()][agentSet.size()][resourceSet
				.size()];

		output.setAllocation(allocation);

		outputSet.add(output);

//		output = new Output();
//
//		boolean found = false;
//		for (Agent a : agentSet) {
//			for (Agent a2 : agentSet) {
//				if (a.isNeighbor(a2)) {
//					for (int r : a.getResourceSetAgent()) {
//						if (a.getResourceSetAgent(r) >= 2) {
//							allocation[a.getID()][a2.getID()][r] = -2;
//							allocation[a2.getID()][a.getID()][r] = 2;
//							found = true;
//							break;
//						}
//					}
//					if (found)
//						break;
//				}
//			}
//			if (found)
//				break;
//		}
//
//		output.setAllocation(allocation);
//		outputSet.add(output);
	}
}
