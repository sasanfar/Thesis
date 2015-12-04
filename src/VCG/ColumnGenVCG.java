package VCG;

import java.util.*;
import problem.Logger;
import user_interface.Interface;
import elements.*;

public class ColumnGenVCG {
	
	public MasterProblemVCG masterproblem = new MasterProblemVCG();
	public PricingProblemVCG subproblem = new PricingProblemVCG();
	public Logger logger = new Logger();

	public List<Configuration> configList = new ArrayList<Configuration>();

	static ColumnGenVCG instance;
	
	private ColumnGenVCG(){
	}

	public static ColumnGenVCG getInstance() {
		if (instance == null) {
			instance = new ColumnGenVCG();
		}
			return instance;
	}

	public void runColumnGenerationVCG() {
		int iteration_counter = 0;
		for (Resource r : Interface.network.ResourceSet) {
			do {
				iteration_counter++;
				instance.masterproblem.solveRelaxation();
				instance.subproblem.updateReducedCost(masterproblem.constraints);
				instance.subproblem.solve();
				displayIteration(iteration_counter);
			} while (subproblem.lastObjValue < ParametersVCG.ColGen.zero_reduced_cost_AbortColGen);
		}
		instance.masterproblem.solveMIP();
	}

	private void displayIteration(int iter) {
		if ((iter) % 20 == 0 || iter == 1) {
			System.out.println();
			System.out.print("Iteration");
			System.out.print("     Time");
			System.out.print("   nConfg");
			System.out.print("       MP lb");
			System.out.print("       SB lb");
			System.out.print("      SB int");
			System.out.println();
		}
		System.out.format("%9.0f", (double) iter);
		System.out.format("%9.1f", logger.timeStamp() / 60);
		System.out.format("%9.0f", (double) configList.size());
		System.out.format("%12.4f", instance.masterproblem.lastObjValue); 	// master
																			// lower
		// bound
		System.out.format("%12.4f", instance.subproblem.lastObjValueRelaxed);	// sb
																				// lower
																				// bound
		System.out.format("%12.4f", instance.subproblem.lastObjValue);// sb
																		// lower
																		// bound
		System.out.println();
	}
}
