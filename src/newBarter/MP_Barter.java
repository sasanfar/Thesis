package newBarter;

import java.util.*;

import newBarter.CG_Barter.theta;
import elements.Agent;
import elements.Resource;
import elements.Task;
import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.UnknownObjectException;

public class MP_Barter {

	IloCplex master;
	List<IloRange> constraintsProba = new ArrayList<IloRange>();
	List<IloRange> constraintsIR = new ArrayList<IloRange>();
	List<IloRange> constraintsIC = new ArrayList<IloRange>();
	List<Double> dualProba = new ArrayList<Double>();
	List<Double> dualIR = new ArrayList<Double>();
	List<Double> dualIC = new ArrayList<Double>();

	private List<IloConversion> mipConversion = new ArrayList<IloConversion>();

	public List<Double> getDualProba() {
		return dualProba;
	}

	public List<Double> getDualIR() {
		return dualIR;
	}

	public List<Double> getDualIC() {
		return dualIC;
	}

	/**
	 * [theta] [output] [agent]
	 */
	double[][][] u;

	public void solve() throws IloException {

		// variables
		u = new double[CG_Barter.thetaSet.size()][CG_Barter.outputSet.size()][CG_Barter.agentSet
				.size()];

		master = new IloCplex();
		long startingTime = System.currentTimeMillis();
		IloLinearNumExpr numExpr;
		Output O_init = CG_Barter.outputSet.get(0);

		for (int i = 0; i < CG_Barter.outputSet.size(); i++) {
			CG_Barter.outputSet.get(i).g = master.numVarArray(
					CG_Barter.thetaSet.size(), 0, 1);
		}

		// objective
		IloLinearNumExpr obj = master.linearNumExpr();
		for (theta theta : CG_Barter.thetaSet) {
			for (Output output : CG_Barter.outputSet) {
				for (Agent a : CG_Barter.agentSet) {
					u[theta.getID()][output.getID()][a.getID()] = utility(a,
							theta, output);
					obj.addTerm(u[theta.getID()][output.getID()][a.getID()],
							output.g[theta.getID()]);
				}
			}
		}
		master.addMaximize(obj);

		// PROBA
		for (CG_Barter.theta r : CG_Barter.thetaSet) {
			numExpr = master.linearNumExpr();
			for (Output o : CG_Barter.outputSet) {
				numExpr.addTerm(1, o.g[r.getID()]);
			}
			constraintsProba.add(master.addEq(1, numExpr));
		}

		// IR
		for (Agent a : CG_Barter.agentSet) {
			for (CG_Barter.theta r : CG_Barter.thetaSet) {
				numExpr = master.linearNumExpr();
				for (Output o : CG_Barter.outputSet) {
					numExpr.addTerm(u[r.getID()][o.getID()][a.getID()],
							o.g[r.getID()]);
				}
				constraintsIR.add(master.addGe(utility(a, r, O_init), numExpr));
			}
		}

		// IC - Bayes-Nash
		for (Agent a : CG_Barter.agentSet) {
			for (CG_Barter.theta r1 : CG_Barter.thetaSet) {
				for (CG_Barter.theta r2 : CG_Barter.thetaSet) {
					numExpr = master.linearNumExpr();
					for (Output o : CG_Barter.outputSet) {
						numExpr.addTerm(u[r2.getID()][o.getID()][a.getID()],
								o.g[r2.getID()]);
					}
					for (Output o : CG_Barter.outputSet) {
						numExpr.addTerm(-u[r2.getID()][o.getID()][a.getID()],
								o.g[r1.getID()]);
					}
					constraintsIC.add(master.addGe(0, numExpr));
				}
			}
		}

		// solve
		if (master.solve()) {
			long EndTime = System.currentTimeMillis();
			System.out.println("Master Solved in: " + (EndTime - startingTime)
					+ " miliseconds");
			for (IloRange iloRange : constraintsIR) {
				dualIR.add(master.getDual(iloRange));
			}
			for (IloRange iloRange : constraintsProba) {
				dualProba.add(master.getDual(iloRange));
			}
			for (IloRange iloRange : constraintsIC) {
				dualIC.add(master.getDual(iloRange));
			}
		}
	}


	public double utility(Agent a, CG_Barter.theta theta, Output o) { // Greedy
																		// Approach
		if (a.getTaskSetAgent().size() == 0)
			return 0;

		double u = 0;
		int[] newAllocation = o.givenResourcesToAgent(a);

		Task[] tasksOfAgent = new Task[a.getTaskSetAgent().size()];

		for (int i = 0; i < a.getTaskSetAgent().size(); i++) {
			tasksOfAgent[i] = a.getTaskSetAgent().get(i);
		}

		Arrays.sort(tasksOfAgent);

		for (int i = tasksOfAgent.length - 1; i >= 0; i--) {
			Task t = tasksOfAgent[i];
			boolean possibleExecution = true;
			for (Resource r : CG_Barter.resourceSet) {
				if (newAllocation[r.getID()] + theta.getQuantity(r) < t
						.getRequiredResources(r.getID())) {
					possibleExecution = false;
					break;
				}
			}
			if (possibleExecution) {
				o.executedTasks.add(t);
				u += t.getUtility();
				for (int r = 0; r < CG_Barter.resourceSet.size(); r++) {
					newAllocation[r] -= t.getRequiredResources(r);
				}
			}
		}
		return u;
	}

	public void solveMIP() {
		try {
			convertToMIP();
			if (master.solve()) {
				displaySolution();
			} else {
				System.out.println("Integer solution not found");
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}

	}

	private void convertToMIP() {
		try {
			for (Output outcome : CG_Barter.outputSet) {
				mipConversion.add(master.conversion(outcome.g,
						IloNumVarType.Bool));
				master.add(mipConversion.get(mipConversion.size() - 1));
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	private void displaySolution() {
		try {
			for (Output outcome : CG_Barter.outputSet) {
				for (CG_Barter.theta theta : CG_Barter.thetaSet) {
					if (master.getValue(outcome.g[theta.ID]) > 0.99999) {
						outcome.displayInfo();
					}
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

}
