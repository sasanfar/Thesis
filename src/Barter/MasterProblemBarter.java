package Barter;

import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import problem.Logger;
import user_interface.Interface;
import elements.Agent;
import elements.Resource;

public class MasterProblemBarter {
	public IloCplex master;
	private IloObjective F_designer;
	public Map<Agent, Double> pi = new HashMap<Agent, Double>();
	private List<IloConversion> mipConversion = new ArrayList<IloConversion>();

	// private double[][] theta = new
	// double[Interface.network.AgentSet.size()][Interface.network.ResourceSet
	// .size()];
	public double lastObjValue;

	private double[][] probability = new double[Interface.network.ResourceSet
			.size()][Interface.network.AgentSet.size()];

	public List<Output> outputSet = new ArrayList<Output>();

	public Logger logger = new Logger();

	public List<IloRange> constraints = new ArrayList<IloRange>();
	public List<Double> dualConstraints = new ArrayList<Double>();

	public MasterProblemBarter() {
		createModel();
		createDefaultOutcomes();
		ParametersBarter.configureCplex(this);
	}

	public void createDefaultOutcomes() {
		Output init = new Output(0);
		for (Agent a : Interface.network.AgentSet)
			for (Resource r : Interface.network.ResourceSet)
				init.allocation[a.getID()][r.getID()] = a.getResourceSetAgent(r
						.getID());
		outputSet.add(init);
	}

	public void createModel() {
		try {
			master = new IloCplex();

			IloLinearNumExpr numExpr1 = master.linearNumExpr();
			IloLinearNumExpr numExpr2 = master.linearNumExpr();

			// Objective

			F_designer = master.addMaximize();
			for (Agent a : Interface.network.AgentSet) {
				if (!a.equals(ColumnGenBarter.designer))
					for (Resource r : Interface.network.ResourceSet) {
						for (Output o_k : outputSet) {
							numExpr1.addTerm(
									probability[a.getID()][r.getID()]
											* o_k.utilityAgent[ColumnGenBarter.designer
													.getID()],
									o_k.g[a.getID()][r.getID()]);
						}
					}
			}

			F_designer.setExpr(numExpr1);

			// IC constraints

			for (Agent a : Interface.network.AgentSet) {
				numExpr1 = master.linearNumExpr();
				numExpr2 = master.linearNumExpr();
				for (Resource r1 : Interface.network.ResourceSet) {
					for (Resource r2 : Interface.network.ResourceSet) {
						if (r1 != r2) {
							for (Output o_k : outputSet) {
								numExpr1.addTerm(o_k.utilityAgent[a.getID()],
										o_k.g[a.getID()][r1.getID()]);
								numExpr2.addTerm(o_k.utilityAgent[a.getID()],
										o_k.g[a.getID()][r2.getID()]);
							}
							constraints.add((IloRange) master.addGe(numExpr1,
									numExpr2,
									("IC " + a.getID()) + " " + r1.getID()
											+ " " + r2.getID()));
						}
					}
				}
			}

			// Interim IR constraints

			for (Agent a : Interface.network.AgentSet) {
				numExpr1 = master.linearNumExpr();
				for (Resource r : Interface.network.ResourceSet) {
					for (Output o_k : outputSet)
						numExpr1.addTerm(o_k.utilityAgent[a.getID()],
								o_k.g[a.getID()][r.getID()]);
					constraints.add(master.addGe(numExpr1,
							outputSet.get(0).utilityAgent[a.getID()], ("IR "
									+ a.getID() + r.getID())));
				}
			}

			// Ex-post IR constraints

			for (Agent a : Interface.network.AgentSet) {
				for (Resource r : Interface.network.ResourceSet) {
					for (Output o_k : outputSet) {
						constraints
								.add(master.addGe(
										master.prod(
												(outputSet.get(0).utilityAgent[ColumnGenBarter.designer
														.getID()] - o_k.utilityAgent[ColumnGenBarter.designer
														.getID()]), o_k.g[a
														.getID()][r.getID()]),
										0, ("Ex IR " + a.getID() + " " + r
												.getID())));
					}
				}
			}

			// probability distribution constraints:
			for (Agent a : Interface.network.AgentSet) {
				numExpr1 = master.linearNumExpr();
				for (Resource r : Interface.network.ResourceSet) {
					for (Output o_k : outputSet) {
						numExpr1.addTerm(1, o_k.g[a.getID()][r.getID()]);
					}
					constraints.add(master.addEq(numExpr1, 1,
							"proba " + a.getID() + " " + r.getID()));
				}
			}

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void solveRelaxation() {
		try {
			if (master.solve()) {
				saveDualValues();
				lastObjValue = master.getObjValue();
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void saveDualValues() {
		try {
			for (IloRange rng : constraints) {
				dualConstraints.add(master.getDual(rng));

			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void solveMIP() {
		try {
			convertToMIP();
			if (master.solve()) {
				displaySolution();
				logger.writeLog(master.getObjValue(), master.getBestObjValue());
			} else {
				System.out.println("Integer solution not found");
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void convertToMIP() {

	}

	public void displaySolution() {

	}

}
