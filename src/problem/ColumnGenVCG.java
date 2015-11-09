package problem;

import java.util.*;

import elements.*;
import ilog.concert.*;
import ilog.cplex.*;

public class ColumnGenVCG {

	public static Map<Integer, Agent> all_agents = new HashMap<Integer, Agent>();
	public static Map<Integer, Resource> all_resources = new HashMap<Integer, Resource>();
	public static Map<Integer, Task> all_tasks = new HashMap<Integer, Task>();

	public MasterProblem masterproblem;
	public SubProblem subproblem;
	public Logger logger = new Logger();

	public List<Configuration> configList = new ArrayList<ColumnGenVCG.Configuration>();

	public class Configuration {
		private Resource resourceType;
		private int ID;
		public int[] allocation = new int[all_agents.size()];
		public double welfare;
		public IloNumVar z;
		public IloIntVar[][] varphiAgent = new IloIntVar[all_agents.size()][all_tasks
				.size()];

		public IloIntVar[] varphi = new IloIntVar[all_tasks.size()];
		public IloIntVar[] z_N = new IloIntVar[all_agents.size()];

		public Configuration(Resource r) {
			resourceType = r;
			welfare = 0;
			ID = configList.size();
			for (Agent a : all_agents.values()) {
				allocation[a.getID()] = a.getResourceSetAgent(resourceType
						.getID());
			}
			configList.add(this);
		}

		public Resource getResourceTypeOfConfiguration() {
			return this.resourceType;
		}

		public int getID() {
			return this.ID;

		}

		public void displayInfo() {
			System.out
					.println("=====================================================================");
			System.out.println("Configuration id: " + ID);
			System.out.println("Configuration resource type: " + resourceType);
			System.out.println("------------------------------");
			for (Agent a : all_agents.values())
				System.out.println("Agent" + a.getID() + ": "
						+ Arrays.toString(a.getResourceSetAgent()));
			System.out
					.println("=====================================================================");
		}

	}

	public class MasterProblem {
		public IloCplex cplex;
		private IloLinearNumExpr socialWelfare;
		private Map<Agent, IloRange> row_agents = new HashMap<Agent, IloRange>();
		private Map<Agent, Double> pi = new HashMap<Agent, Double>(); // agents'
																		// duals
		private List<IloConversion> mipConversion = new ArrayList<IloConversion>();
		public double lastObjValue;
		public Configuration c;

		IloLinearNumExpr num_expr;
		// Parameters
		private int[][] Req = new int[all_tasks.size()][all_resources.size()];

		// Variables
		private IloIntVar[] y = new IloIntVar[all_tasks.size()];
		private IloNumVar[] u = new IloNumVar[all_tasks.size()];
		private IloNumVar[][] E_N = new IloNumVar[all_agents.size()][all_resources
				.size()];

		public Logger logger = new Logger();

		public MasterProblem() {
			createModel();
			creatDefaultConfigurations();
			Parameters.configureCplex(this);

		}

		public void creatDefaultConfigurations() {
			for (Resource r : all_resources.values()) {
				Configuration c = new Configuration(r);
				configList.add(c);
			}
		}

		public void createModel() {
			try {
				cplex = new IloCplex();

				// ----------------------------------------------------------------------
				// Objective
				// ----------------------------------------------------------------------
				socialWelfare = cplex.linearNumExpr();
				IloLinearNumExpr obj_task = cplex.linearNumExpr();
				for (Resource k : all_resources.values())
					for (Configuration c : configList) {
						if (c.getResourceTypeOfConfiguration() == k) {
							for (Agent a : all_agents.values()) {
								obj_task.add((IloLinearNumExpr) cplex.prod(-1,
										E_N[a.getID()][k.getID()]));
								for (Agent a2 : all_agents.values()) {
									for (Task t : a2.getTaskSetAgent())
										if (a2 != a)
											obj_task.add((IloLinearNumExpr) cplex
													.prod(u[t.getID()], cplex
															.prod(c.varphi[t
																	.getID()],
																	c.z)));
								}

							}
						}
					}

				for (Task t : all_tasks.values())
					obj_task.add((IloLinearNumExpr) cplex.prod(y[t.getID()],
							u[t.getID()]));

				socialWelfare = obj_task;

				cplex.addMaximize(socialWelfare);

				// ----------------------------------------------------------------------
				// Variables and Parameters
				// ----------------------------------------------------------------------
				for (Task t : all_tasks.values()) {
					y[t.getID()] = cplex.intVar(0, 1);
					for (Configuration c : configList) {
						c.varphi[t.getID()] = cplex.intVar(0, 1);
						for (Agent a : all_agents.values()) {
							c.varphiAgent[a.getID()][t.getID()] = cplex.intVar(
									0, Integer.MAX_VALUE);
						}
					}
				}
				for (Agent a : all_agents.values()) {
					for (Resource k : all_resources.values())
						E_N[a.getID()][k.getID()] = cplex.numVar(0,
								Double.MAX_VALUE);
				}

				for (Task t : all_tasks.values())
					for (Resource k : all_resources.values())
						Req[t.getID()][k.getID()] = t.getRequiredResources(k
								.getID());

				// ----------------------------------------------------------------------
				// Constraints
				// ----------------------------------------------------------------------
				List<IloRange> constraints = new ArrayList<IloRange>();
				// // 24
				for (Task t : all_tasks.values())
					for (Resource k : all_resources.values()) {
						num_expr = cplex.linearNumExpr();
						for (Agent a : all_agents.values()) {
							if (t.getLocation() == a
									|| t.getLocation().isNeighbor(a))
								for (Configuration c : configList)
									if (c.getResourceTypeOfConfiguration() == k)
										num_expr.add((IloLinearNumExpr) cplex.prod(
												c.varphiAgent[a.getID()][t
														.getID()], c.z));
							constraints.add((IloRange) cplex.addGe(
									num_expr,
									cplex.prod(y[t.getID()],
											Req[t.getID()][k.getID()]), "C24"));
							// ////26
							for (Agent a2 : all_agents.values()) {
								num_expr = cplex.linearNumExpr();
								if ((t.getLocation() == a2 || t.getLocation()
										.isNeighbor(a2)) && a != a2)
									for (Configuration c : configList)
										if (c.getResourceTypeOfConfiguration() == k) {
											num_expr.add((IloLinearNumExpr) cplex.prod(
													c.varphiAgent[a2.getID()][t
															.getID()], c.z_N[a
															.getID()]));
										}
							}
							constraints.add((IloRange) cplex.addGe(
									num_expr,
									cplex.prod(y[t.getID()],
											Req[t.getID()][k.getID()]), "C26"));

						}
					}
				// ////25
				for (Resource k : all_resources.values()) {
					for (Agent a : all_agents.values()) {
						num_expr = cplex.linearNumExpr();
						for (Configuration c : configList) {
							if (c.getResourceTypeOfConfiguration() == k) {
								for (Task t : all_tasks.values()) {
									num_expr.add((IloLinearNumExpr) cplex.prod(
											c.varphiAgent[a.getID()][t.getID()],
											c.z));
								}
								constraints.add(cplex.addLe(num_expr,
										c.allocation[a.getID()], "C25"));
							}
						}

						// ////27
						for (Agent a2 : all_agents.values()) {
							num_expr = cplex.linearNumExpr();
							for (Configuration c : configList) {
								if (c.getResourceTypeOfConfiguration() == k
										&& a2 != a) {
									for (Task t : all_tasks.values()) {

										num_expr.add((IloLinearNumExpr) cplex.prod(
												c.varphiAgent[a2.getID()][t
														.getID()], c.z));
									}
									constraints.add(cplex.addLe(num_expr,
											c.allocation[a2.getID()], "C27"));
								}
							}
						}
					}
				}
				// ////28
				for (Resource k : all_resources.values()) {
					for (Agent a : all_agents.values()) {
						num_expr = cplex.linearNumExpr();
						for (Configuration c : configList) {
							for (Agent a2 : all_agents.values()) {
								for (Task t : a2.getTaskSetAgent()) {
									if (c.getResourceTypeOfConfiguration() == k
											&& a2 != a) {
										num_expr.add((IloLinearNumExpr) cplex
												.prod(u[t.getID()], cplex.prod(
														c.varphi[t.getID()],
														c.z_N[a.getID()])));
									}
								}
							}

						}
						constraints.add((IloRange) cplex.addLe(num_expr,
								E_N[a.getID()][k.getID()], "C28"));
					}
				}
				// ////29
				for (Resource k : all_resources.values()) {
					num_expr = cplex.linearNumExpr();
					for (Configuration c : configList) {
						if (c.getResourceTypeOfConfiguration() == k)
							num_expr.add((IloLinearNumExpr) c.z);
					}
					constraints.add(cplex.addLe(num_expr, 1, "C29"));
				}
				// ////30
				for (Resource k : all_resources.values()) {
					for (Agent a : all_agents.values()) {
						num_expr = cplex.linearNumExpr();
						for (Configuration c : configList) {
							if (c.getResourceTypeOfConfiguration() == k) {
								num_expr.add((IloLinearNumExpr) c.z_N[a.getID()]);
							}
						}
						constraints.add(cplex.addLe(num_expr, 1, "C30"));
					}
				}
				// ////31
				for (Resource k : all_resources.values()) {
					for (Agent a : all_agents.values()) {
						constraints.add(cplex.addGe(E_N[a.getID()][k.getID()],
								0, "C31"));

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
				for (Agent a : all_agents.values())
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
				for (Agent a : all_agents.values())
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
					logger.writeLog(cplex.getObjValue(),
							cplex.getBestObjValue());
				} else {
					System.out.println("Integer solution not found");
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void convertToMIP() {
			try {
				for (Configuration c : configList) {
					mipConversion
							.add(cplex.conversion(c.z, IloNumVarType.Bool));
					cplex.add(mipConversion.get(mipConversion.size() - 1));
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void displaySolution() {
			try {
				for (Configuration c : configList) {
					if (cplex.getValue(c.z) > 0.99999) {
						c.displayInfo();
					}
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

	}

	public class SubProblem {
		Resource resourceType;
		public IloCplex cplex;
		private IloObjective reduced_cost;
		private IloLinearNumExpr num_expr;
		private List<IloConstraint> constraints;
		public MyMipCallBack mip_call_back;
		public double lastObjValue;
		public double lastObjValueRelaxed;

		private class MyMipCallBack extends IloCplex.MIPInfoCallback {
			private boolean aborted;
			private double time_start;
			private double time_limit;

			public void setResourceType(Resource r){
				resourceType=r;
			}
			
			public void main() {
				try {
					if (!aborted && hasIncumbent()) {
						double time_used = getCplexTime() - time_start;
						if ((getIncumbentObjValue() < Parameters.ColGen.subproblemObjVal)
								|| (time_used > time_limit)
								|| (getBestObjValue() > Parameters.ColGen.zero_reduced_cost)) {
							aborted = true;
							abort();
						}
					}
				} catch (IloException e) {
					System.err.println("Concert exception caught: " + e);
				}
			}

			public void reset() {
				try {
					aborted = false;
					time_start = cplex.getCplexTime();
					time_limit = Parameters.ColGen.subproblemTiLim;
				} catch (IloException e) {
					System.err.println("Concert exception caught: " + e);
				}
			}
		}

		public SubProblem() {
			this.constraints = new ArrayList<IloConstraint>();
			createModel();
			// setPriority();
			Parameters.configureCplex(this);
			this.mip_call_back = new MyMipCallBack();
			try {
				cplex.use(mip_call_back);
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		// public void setPriority() {
		// try {
		// for (int j : all_agents.keySet())
		// cplex.setPriority(/**/, 1);
		// } catch (IloException e) {
		// System.err.println("Concert exception caught: " + e);
		// }
		// }

		public void createModel() {
			try {
				// define model
				cplex = new IloCplex();
				// define variables
				IloNumVar varphiAgent[][] = new IloNumVar[all_agents.size()][all_tasks
						.size()];
				IloIntVar varphi[] = new IloIntVar[all_tasks.size()];
				for (Task t : all_tasks.values()) {
					varphi[t.getID()] = cplex.intVar(0, 1);

					for (Agent a : all_agents.values())
						varphiAgent[a.getID()][t.getID()] = cplex.numVar(0,
								Double.MAX_VALUE);

				}
				// define parameters

				// set objective
				reduced_cost = cplex.addMaximize();

				// set constraint :
				for (Agent a : all_agents.values()) {
					num_expr = cplex.linearNumExpr();
					for (Task t : all_tasks.values()) {
						if (a.isNeighbor(t.getLocation())
								|| t.getLocation() == a) {
							num_expr.add((IloLinearNumExpr) varphiAgent[a
									.getID()][t.getID()]);
						}
					}
					constraints.add(cplex.addLe(num_expr,
							a.getResourceSetAgent(resourceType.getID())));
				}
				// // set constraint :
				for (Task t : all_tasks.values()) {
					num_expr = cplex.linearNumExpr();
					for (Agent a : all_agents.values()) {
						if (t.getLocation() == a
								|| a.isNeighbor(t.getLocation())) {
							num_expr.add((IloLinearNumExpr) varphiAgent[a
									.getID()][t.getID()]);
						}
					}
					constraints.add(cplex.addEq(num_expr, cplex.prod(
							varphi[t.getID()],
							t.getRequiredResources(resourceType.getID()))));
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void updateReducedCost() {
			

		}

		public void solve() {

		}
	}

	public void runColumnGeneration() {
		int iteration_counter = 0;
		do {
			iteration_counter++;
			masterproblem.solveRelaxation();
			subproblem.updateReducedCost();
			subproblem.solve();
			displayIteration(iteration_counter);
		} while (subproblem.lastObjValue < Parameters.ColGen.zero_reduced_cost_AbortColGen);
		masterproblem.solveMIP();
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
		System.out.format("%12.4f", masterproblem.lastObjValue); // master lower
																	// bound
		System.out.format("%12.4f", subproblem.lastObjValueRelaxed);// sb lower
																	// bound
		System.out.format("%12.4f", subproblem.lastObjValue);// sb lower bound
		System.out.println();
	}
}
