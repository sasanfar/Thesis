package Barter;

import java.security.AllPermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import elements.*;
import problem.*;
import user_interface.Interface;
import ilog.concert.*;
import ilog.cplex.*;

public class ColumnGenBarter {

	public MasterProblemBarter masterproblem;
	public PricingProblemBarter pricing;
	public Logger logger = new Logger();
	public List<Output> outputSet = new ArrayList<Output>();

	private static ColumnGenBarter instance;

	public static Agent designer;

	private ColumnGenBarter() {
		designer = Interface.network.AgentSet
				.get((int) (Interface.network.AgentSet.size() * Math.random()));
		masterproblem = new MasterProblemBarter();
		pricing = new PricingProblemBarter();
	}

	public static ColumnGenBarter getInstance() {
		if (instance == null)
			instance = new ColumnGenBarter();
		return instance;
	}

	public void runColumnGeneration() {
		int iteration_counter = 0;
		do {
			iteration_counter++;
			masterproblem.solveRelaxation();
			pricing.updateReducedCost();
			pricing.solve();
			displayIteration(iteration_counter);
		} while (pricing.lastObjValue < ParametersBarter.ColGen.zero_reduced_cost_AbortColGen);
		masterproblem.solveMIP();
	}

	private void displayIteration(int iter) {
		if ((iter) % 20 == 0 || iter == 1) {
			System.out.println();
			System.out.print("Iteration");
			System.out.print("     Time");
			System.out.print(" #outcome");
			System.out.print("       MP lb");
			System.out.print("       SB lb");
			System.out.print("      SB int");
			System.out.println();
		}
		System.out.format("%9.0f", (double) iter);
		System.out.format("%9.1f", logger.timeStamp() / 60);
		System.out.format("%9.0f", (double) outputSet.size());
		System.out.format("%12.4f", masterproblem.lastObjValue); // master lower
																	// bound
		System.out.format("%12.4f", pricing.lastObjValueRelaxed);// sub lower
																	// bound
		System.out.format("%12.4f", pricing.lastObjValue);// sub lower bound
		System.out.println();
	}
}
