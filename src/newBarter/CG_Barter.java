package newBarter;

import java.util.ArrayList;
import java.util.List;

import user_interface.Interface;
import elements.*;

public class CG_Barter {

	public static List<Agent> agentSet = Interface.network.AgentSet;
	public static List<Task> taskSet = Interface.network.TaskSet;
	public static List<Resource> resourceSet = Interface.network.ResourceSet;
	
	public static List<Output> outoutSet = new ArrayList<Output>();
	
	public static void runBarter(){
		MP_Barter master;
		PP_Barter pp;
		int iterationCounter = 0;
		
		buildFirstOutcome();
		do {
			
			master = new MP_Barter();
			master.solve();
			
			pp = new PP_Barter();
			pp.solve();
			
			iterationCounter++;
		}while (iterationCounter <100);
		
		master.solveMIP();
	}

	private static void buildFirstOutcome() {
		// TODO Auto-generated method stub
		
	}
}
