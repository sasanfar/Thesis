package VCG;

import ilog.concert.*;
import ilog.cplex.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import problem.*;
import user_interface.Interface;
import elements.*;

public class MasterProblemVCG {
	public IloCplex cplex;
	private IloLinearNumExpr socialWelfare;
	private Map<Agent, IloRange> row_agents = new HashMap<Agent, IloRange>();
	private Map<Agent, Double> pi = new HashMap<Agent, Double>(); // agents'
																	// duals
	private List<IloConversion> mipConversion = new ArrayList<IloConversion>();

	public IloNumVar[] z = new IloNumVar[ColumnGenVCG.getInstance().configList
			.size()];
	public Double[][][] varphiAgent = new Double[ColumnGenVCG.getInstance().configList
			.size()][Interface.network.AgentSet.size()][Interface.network.TaskSet
			.size()];

	public Double[][] varphi = new Double[ColumnGenVCG.getInstance().configList
			.size()][Interface.network.TaskSet.size()];
	public IloNumVar[][] z_N = new IloNumVar[ColumnGenVCG.getInstance().configList
			.size()][Interface.network.AgentSet.size()];
	public double lastObjValue;

	IloLinearNumExpr num_expr;
	// Parameters
	private int[][] Req = new int[Interface.network.TaskSet.size()][Interface.network.ResourceSet
			.size()];

	// Variables
	private IloIntVar[] y = new IloIntVar[Interface.network.TaskSet.size()];
	private IloNumVar[][] E_N = new IloNumVar[Interface.network.AgentSet.size()][Interface.network.ResourceSet
			.size()];
	List<IloRange> constraints = new ArrayList<IloRange>();
	public Logger logger = new Logger();

	public MasterProblemVCG() {
		creatDefaultConfigurations();
		createModel();
		ParametersVCG.configureCplex(this);

	}

	public void creatDefaultConfigurations() {
		for (Resource r : Interface.network.ResourceSet) {
			Configuration c = new Configuration(r);
			ColumnGenVCG.getInstance().configList.add(c);
		}
	}

	public void createModel() {
		try {

			cplex = new IloCplex();
			// Variables
			for (int c = 0; c < ColumnGenVCG.getInstance().configList.size(); c++) {
				Configuration config = ColumnGenVCG.getInstance().configList
						.get(c);
				z = cplex.numVarArray(
						ColumnGenVCG.getInstance().configList.size(), 0, 1);

				for (Task t : Interface.network.TaskSet) {
					for (Agent a : Interface.network.AgentSet) {

					}
				}

				// z = master.boolVarArray(configuration.length);
			}
			// ----------------------------------------------------------------------
			// Objective
			// ----------------------------------------------------------------------
			socialWelfare = cplex.linearNumExpr();

			for (Resource k : Interface.network.ResourceSet)
				for (Configuration c : ColumnGenVCG.getInstance().configList)
					if (c.getResourceTypeOfConfiguration() == k) {
						for (Agent a : Interface.network.AgentSet) {
							socialWelfare
									.addTerm(-1, E_N[a.getID()][k.getID()]);
							for (Agent a2 : Interface.network.AgentSet) {
								for (Task t : a2.getTaskSetAgent())
									if (a2 != a)
										socialWelfare.addTerm(t.getUtility()
												* varphi[c.ID][t.getID()],
												z[c.ID]);

							}
						}
					}

			for (Task t : Interface.network.TaskSet)
				socialWelfare.addTerm(t.getUtility(), y[t.getID()]);

			cplex.addMaximize(socialWelfare);

			// ----------------------------------------------------------------------
			// Variables and Parameters
			// ----------------------------------------------------------------------

			for (Agent a : Interface.network.AgentSet) {
				for (Resource k : Interface.network.ResourceSet)
					E_N[a.getID()][k.getID()] = cplex.numVar(0,
							Double.MAX_VALUE);
			}

			for (Task t : Interface.network.TaskSet)
				for (Resource k : Interface.network.ResourceSet)
					Req[t.getID()][k.getID()] = t.getRequiredResources(k
							.getID());

			// ----------------------------------------------------------------------
			// Constraints
			// ----------------------------------------------------------------------

			// // 24
			for (Task t : Interface.network.TaskSet)
				for (Resource k : Interface.network.ResourceSet) {
					num_expr = cplex.linearNumExpr();
					for (Configuration c : ColumnGenVCG.getInstance().configList)
						for (Agent a : Interface.network.AgentSet) {
							if (t.getLocation() == a
									|| t.getLocation().isNeighbor(a))
								if (c.getResourceTypeOfConfiguration() == k)
									num_expr.addTerm(varphiAgent[c.ID][a
											.getID()][t.getID()], z[c.ID]);
							constraints.add((IloRange) cplex.addGe(
									num_expr,
									cplex.prod(y[t.getID()],
											Req[t.getID()][k.getID()]), "C24"));
							// ////26
							for (Agent a2 : Interface.network.AgentSet) {
								num_expr = cplex.linearNumExpr();
								if ((t.getLocation() == a2 || t.getLocation()
										.isNeighbor(a2)) && a != a2)
									if (c.getResourceTypeOfConfiguration() == k) {
										num_expr.addTerm(varphiAgent[c.ID][a2
												.getID()][t.getID()],
												z_N[c.ID][a.getID()]);
									}
							}
							constraints.add((IloRange) cplex.addGe(
									num_expr,
									cplex.prod(y[t.getID()],
											Req[t.getID()][k.getID()]), "C26"));

						}
				}
			// ////25
			for (Resource k : Interface.network.ResourceSet) {
				for (Agent a : Interface.network.AgentSet) {
					num_expr = cplex.linearNumExpr();
					for (Configuration c : ColumnGenVCG.instance.configList) {
						if (c.getResourceTypeOfConfiguration() == k) {
							for (Task t : Interface.network.TaskSet) {
								num_expr.addTerm(
										varphiAgent[c.ID][a.getID()][t.getID()],
										z[c.ID]);
							}
							constraints.add(cplex.addLe(num_expr,
									c.allocation[a.getID()], "C25"));
						}
					}

					// ////27
					for (Agent a2 : Interface.network.AgentSet) {
						num_expr = cplex.linearNumExpr();
						if (c.getResourceTypeOfConfiguration() == k && a2 != a) {
							for (Task t : Interface.network.TaskSet) {

								num_expr.addTerm(
										c.varphiAgent[a2.getID()][t.getID()],
										c.z);
							}
							constraints.add(cplex.addLe(num_expr,
									c.allocation[a2.getID()], "C27"));

						}
					}
				}
			}
			// ////28
			for (Resource k : Interface.network.ResourceSet) {
				for (Agent a : Interface.network.AgentSet) {
					num_expr = cplex.linearNumExpr();
					for (Agent a2 : Interface.network.AgentSet) {
						for (Task t : a2.getTaskSetAgent()) {
							if (c.getResourceTypeOfConfiguration() == k
									&& a2 != a) {
								num_expr.addTerm(
										t.getUtility() * c.varphi[t.getID()],
										c.z_N[a.getID()]);

							}
						}

					}
					constraints.add((IloRange) cplex.addLe(num_expr,
							E_N[a.getID()][k.getID()], "C28"));
				}
			}
			// ////29
			for (Resource k : Interface.network.ResourceSet) {
				num_expr = cplex.linearNumExpr();
				if (c.getResourceTypeOfConfiguration() == k)
					num_expr.addTerm(1, c.z);
			}
			constraints.add(cplex.addLe(num_expr, 1, "C29"));

			// ////30
			for (Resource k : Interface.network.ResourceSet) {
				for (Agent a : Interface.network.AgentSet) {
					num_expr = cplex.linearNumExpr();
					if (c.getResourceTypeOfConfiguration() == k) {
						num_expr.add((IloLinearNumExpr) c.z_N[a.getID()]);
					}
				}
				constraints.add(cplex.addLe(num_expr, 1, "C30"));

			}
			// ////31
			for (Resource k : Interface.network.ResourceSet) {
				for (Agent a : Interface.network.AgentSet) {
					constraints.add(cplex.addGe(E_N[a.getID()][k.getID()], 0,
							"C31"));

				}
			}

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void addNewColumn(Configuration conf) {
		try {
			IloColumn new_column = cplex.column((IloRange) socialWelfare,
					conf.ID);
			for (Agent a : Interface.network.AgentSet)
				new_column = new_column.and(cplex.column(row_agents.get(a),
						conf.ID));

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void solveRelaxation() {
		try {
			if (cplex.solve()) {
				saveDualValues();
				lastObjValue = cplex.getObjValue();
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void saveDualValues() {
		try {
			for (Agent a : Interface.network.AgentSet)
				pi.put(a, cplex.getDual(row_agents.get(a)));
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void solveMIP() {
		try {
			convertToMIP();
			if (cplex.solve()) {
				displaySolution();
				logger.writeLog(cplex.getObjValue(), cplex.getBestObjValue());
			} else {
				System.out.println("Integer solution not found");
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void convertToMIP() {
		try {
			for (Configuration c : ColumnGenVCG.getInstance().configList) {
				mipConversion.add(cplex.conversion(c.z, IloNumVarType.Bool));
				cplex.add(mipConversion.get(mipConversion.size() - 1));
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void displaySolution() {
		try {
			for (Configuration c : ColumnGenVCG.getInstance().configList) {
				if (cplex.getValue(c.z) > 0.99999) {
					c.displayInfo();
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

}
