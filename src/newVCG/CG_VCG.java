package newVCG;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resources;

import user_interface.Interface;
import elements.*;

public class CG_VCG {
	public static List<Agent> agentSet = Interface.network.AgentSet;
	public static List<Task> taskSet = Interface.network.TaskSet;
	public static List<Resource> resourceSet = Interface.network.ResourceSet;

	public static List<Configuration> configurationList = new ArrayList<Configuration>();

	public static void runVCG() {
		MP_VCG master;
		PP_VCG pp;
		int iterator = 0;
		boolean lastTime = false;
		generateFirstConfigurations();
		do {
			master = new MP_VCG();
			master.solve();

			for (Resource r : resourceSet) {
				pp = new PP_VCG(r);
				pp.solve();
			}

		} while (iterator < 100);

		master.solveMIP();
	}

	private static void generateFirstConfigurations() {
		for (Resource resource : resourceSet) {
			Configuration configuration = new Configuration(resource);
			for (Agent agent : agentSet)
				configuration.allocation[agent.getID()] = agent
						.getResourceSetAgent(resource.getID());
			
			// add varphi and varphiAgent
			
			configurationList.add(configuration);
		}
	}
}
