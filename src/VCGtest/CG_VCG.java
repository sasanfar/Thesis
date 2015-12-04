package VCGtest;

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
		boolean lastTime=false;
		generateFirstConfigurations();
		do {
			master = new MP_VCG();
			master.solve(lastTime);

			for (Resource r : resourceSet) {
				pp = new PP_VCG(r);
				if (pp.solve())
					pp.update();
			}

		} while (iterator<100);

		master.solveMIP();
	}

	private static void generateFirstConfigurations() {
		// TODO Auto-generated method stub
		
	}
}
