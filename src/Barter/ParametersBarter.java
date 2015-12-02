package Barter;

import ilog.concert.*;
import ilog.cplex.*;

public class ParametersBarter {
	public static double capacity = 200;

	public static class ColGen {
		public static boolean abort = false;
		public static double zero_reduced_cost = -0.0001;
		public static double zero_reduced_cost_AbortColGen = -0.005;
		public static double subproblemTiLim = 5;
		public static double subproblemObjVal = -1000;
	}

	public static void configureCplex(MasterProblemBarter masterProblemBarter) {
		try {
			// branch and bound
			masterProblemBarter.master.setParam(
					IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			masterProblemBarter.master.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			masterProblemBarter.master.setParam(IloCplex.Param.MIP.Display, 2);
			masterProblemBarter.master.setParam(IloCplex.Param.Tune.Display, 1);
			masterProblemBarter.master.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public static void configureCplex(PricingProblemBarter pricingProblemBarter) {
		try {
			// branch and bound
			pricingProblemBarter.cplex
					.setParam(IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			pricingProblemBarter.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			pricingProblemBarter.cplex.setParam(IloCplex.Param.MIP.Display, 2);
			pricingProblemBarter.cplex.setParam(IloCplex.Param.Tune.Display, 1);
			pricingProblemBarter.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

}