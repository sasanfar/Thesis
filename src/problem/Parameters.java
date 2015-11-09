package problem;

import problem.ColumnGenBarter.MasterProblem;
import problem.ColumnGenBarter.SubProblem;
import ilog.concert.*;
import ilog.cplex.*;

public class Parameters {
	public static double capacity = 200;

	public static class ColGen {
		public static boolean abort = false;
		public static double zero_reduced_cost = -0.0001;
		public static double zero_reduced_cost_AbortColGen = -0.005;
		public static double subproblemTiLim = 5;
		public static double subproblemObjVal = -1000;
	}

	public static void configureCplex(MasterProblem masterProblem) {
		try {
			// branch and bound
			masterProblem.cplex.setParam(
					IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			masterProblem.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			masterProblem.cplex.setParam(IloCplex.Param.MIP.Display, 2);
			masterProblem.cplex.setParam(IloCplex.Param.Tune.Display, 1);
			masterProblem.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public static void configureCplex(SubProblem subProblem) {
		try {
			// branch and bound
			subProblem.cplex
					.setParam(IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			subProblem.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			subProblem.cplex.setParam(IloCplex.Param.MIP.Display, 2);
			subProblem.cplex.setParam(IloCplex.Param.Tune.Display, 1);
			subProblem.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public static void configureCplex(problem.ColumnGenVCG.SubProblem subProblem) {
		try {
			// branch and bound
			subProblem.cplex
					.setParam(IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			subProblem.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			subProblem.cplex.setParam(IloCplex.Param.MIP.Display, 2);
			subProblem.cplex.setParam(IloCplex.Param.Tune.Display, 1);
			subProblem.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public static void configureCplex(
			problem.ColumnGenVCG.MasterProblem masterProblem) {
		try {
			// branch and bound
			masterProblem.cplex.setParam(
					IloCplex.Param.MIP.Strategy.NodeSelect, 1);
			masterProblem.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
			// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
			// true);
			// display options
			masterProblem.cplex.setParam(IloCplex.Param.MIP.Display, 2);
			masterProblem.cplex.setParam(IloCplex.Param.Tune.Display, 1);
			masterProblem.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}
}