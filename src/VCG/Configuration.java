package VCG;

import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;

import java.util.Arrays;

import user_interface.Interface;
import elements.Agent;
import elements.Resource;

public class Configuration {
	private Resource resourceType;
	int ID;
	public int[] allocation = new int[Interface.network.AgentSet.size()];
	public double welfare;
	public IloNumVar z;
	public Double[][] varphiAgent = new Double[Interface.network.AgentSet.size()][Interface.network.TaskSet
			.size()];

	public Double[] varphi = new Double[Interface.network.TaskSet.size()];
	public IloIntVar[] z_N = new IloIntVar[Interface.network.AgentSet.size()];

	public Configuration(Resource r) {
		resourceType = r;
		welfare = 0;
		ID = ColumnGenVCG.getInstance().configList.size();
		for (Agent a : Interface.network.AgentSet) {
			allocation[a.getID()] = a.getResourceSetAgent(resourceType.getID());
		}
		ColumnGenVCG.getInstance().configList.add(this);
	}

	public Resource getResourceTypeOfConfiguration() {
		return this.resourceType;
	}

	public int getID() {
		return this.ID;

	}

	public void displayInfo() {
		System.out
				.println("=====================================================================");
		System.out.println("Configuration id: " + ID);
		System.out.println("Configuration resource type: " + resourceType);
		System.out.println("------------------------------");
		for (Agent a : Interface.network.AgentSet)
			System.out.println("Agent" + a.getID() + ": "
					+ Arrays.toString(a.getResourceSetAgent()));
		System.out
				.println("=====================================================================");
	}

}
