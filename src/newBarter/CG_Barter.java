package newBarter;

import java.util.ArrayList;
import java.util.List;

import user_interface.Interface;
import elements.*;
import ilog.concert.IloException;

public class CG_Barter {

	public static List<Agent> agentSet = Interface.network.AgentSet;
	public static List<Task> taskSet = Interface.network.TaskSet;
	public static List<Resource> resourceSet = Interface.network.ResourceSet;
	public Agent designer = agentSet.get(0);
	public class Theta{
		
	}
	/**
	 * [agent][resource]
	 */
	public static int[][] reportedType = new int[agentSet.size()][resourceSet.size()];

	/**
	 * [agent][resource]
	 */
	public double[][] probability = new double[agentSet.size()][resourceSet.size()];

	public static List<Output> outputSet = new ArrayList<Output>();

	public static void runBarter() throws IloException {
		MP_Barter master;
		PP_Barter pp;
		int iterationCounter = 0;

		reportInformation();
		buildFirstOutcome();
		do {

			master = new MP_Barter();
			master.solve();

			pp = new PP_Barter();
			pp.solve();

			iterationCounter++;
		} while (iterationCounter < 100);

		master.solveMIP();
	}

	private static void reportInformation() {
		for (Agent agent : agentSet) {
			for (Resource resource : resourceSet) {
				reportedType[agent.getID()][resource.getID()] = (int) (Math.random() * Interface._maxEachTypePerAgent);
			}
		}
	}

	private static void buildFirstOutcome() {
		// TODO Auto-generated method stub

	}
}
