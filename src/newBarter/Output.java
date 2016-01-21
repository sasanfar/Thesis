package newBarter;

import java.util.*;

import ilog.concert.*;
import elements.*;;

public class Output {
	int ID;
	static int nextID = 0;
	IloNumVar g[] = new IloNumVar[CG_Barter.resourceSet.size()];
	List<Task> executedTasks = new ArrayList<Task>();
	
	/**
	 * The quantity of resource, agent1 has given to agent2
	 * [agent1][agent2][resource]
	 */
	int[][][] allocation = new int[CG_Barter.agentSet.size()][CG_Barter.agentSet
			.size()][CG_Barter.resourceSet.size()];

	public Output() {
		ID = nextID++;
	}

	public int getID() {
		return ID;
	}

	public int[] givenResourcesToAgent(Agent a) {
		int i = a.getID();
		int[] newResources = new int[CG_Barter.resourceSet.size()];
		for (int j = 0; j < CG_Barter.agentSet.size(); j++) {
			for (int r = 0; r < CG_Barter.resourceSet.size(); r++) {
				newResources[r] += allocation[i][j][r];
			}
		}
		return newResources;
	}

	public void setAllocation(int[][][] allocation) {
		this.allocation = allocation;
	}

	@Override
	public String toString() {
		return "O: " + ID+ executedTasks.toString();
	}

	public void displayInfo() {
		// TODO Auto-generated method stub
		
	}
}
