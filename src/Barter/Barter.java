package Barter;

import Parameters;
import ilog.concert.IloColumn;
import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.*;

import ColumnGen.Path;
import ColumnGen.SubProblem.MyMipCallBack;
import newBarter.CG_Barter;
import user_interface.Interface;
import elements.*;

public class Barter {
	Master master;
	Pricing pp;
	List<Agent> agentSet = Interface.network.AgentSet;
	List<Resource> resourceSet = Interface.network.ResourceSet;
	List<Task> taskSet = Interface.network.TaskSet;
	List<Theta> thetaSet = new ArrayList<Theta>();

	List<Outcome> outcomeSet = new ArrayList<Outcome>();

	static int nextOutcomeID = 0;

	public class Outcome {
		public int ID;
		public IloNumVar[] g;
		/**
		 * theta agent
		 */
		public double[][] u;
		/**
		 * agent agent resource
		 */
		int[][][] allocation;

		public Outcome() {
			ID = nextOutcomeID++;
			u = new double[thetaSet.size()][agentSet.size()];
			allocation = new int[CG_Barter.agentSet.size()][CG_Barter.agentSet
					.size()][CG_Barter.resourceSet.size()];
		}

		public int[] givenResourcesToAgent(Agent a) {
			int i = a.getID();
			int[] newResources = new int[CG_Barter.resourceSet.size()];
			for (int j = 0; j < CG_Barter.agentSet.size(); j++) {
				for (int r = 0; r < CG_Barter.resourceSet.size(); r++) {
					newResources[r] += allocation[i][j][r];
				}
			}
			return newResources;
		}

		public double U(Agent a, Theta t) {
			return u[t.ID][a.getID()];
		}

		public void calculateU(Theta t, Agent a) {
			double u = 0;
			int[] newAllocation = givenResourcesToAgent(a);

			Task[] tasksOfAgent = new Task[a.getTaskSetAgent().size()];

			for (int i = 0; i < a.getTaskSetAgent().size(); i++) {
				tasksOfAgent[i] = a.getTaskSetAgent().get(i);
			}

			Arrays.sort(tasksOfAgent);

			for (int i = tasksOfAgent.length - 1; i >= 0; i--) {
				Task task = tasksOfAgent[i];
				boolean possibleExecution = true;
				for (Resource r : CG_Barter.resourceSet) {
					if (newAllocation[r.getID()] + t.getQuantity(r) < task
							.getRequiredResources(r.getID())) {
						possibleExecution = false;
						break;
					}
				}
				if (possibleExecution) {
					u += task.getUtility();
					for (int r = 0; r < CG_Barter.resourceSet.size(); r++) {
						newAllocation[r] -= task.getRequiredResources(r);
					}
				}
			}
			this.u[t.ID][a.getID()] = u;
		}

		public void displayInfo() {
			// TODO Auto-generated method stub

		}
	}

	public class Theta {
		public int ID;
		int[] quantity = new int[resourceSet.size()];

		public int getQuantity(Resource r) {
			return quantity[r.getID()];
		}
	}

	public class Master {
		public IloCplex cplex;
		private IloObjective sw;
		private Map<Theta, IloRange> Proba = new HashMap<Theta, IloRange>();
		private Map<Theta, Double> probaDual = new HashMap<Barter.Theta, Double>();
		private Map<Map<Theta, Agent>, IloRange> IR = new HashMap<Map<Theta, Agent>, IloRange>();
		private Map<Map<Theta, Agent>, Double> irDual = new HashMap<Map<Theta, Agent>, Double>();
		private Map<Map<Theta, Map<Theta, Agent>>, IloRange> IC = new HashMap<Map<Theta, Map<Theta, Agent>>, IloRange>();
		private Map<Map<Theta, Map<Theta, Agent>>, Double> icDual = new HashMap<Map<Theta, Map<Theta, Agent>>, Double>();
		private List<IloConversion> mipConversion = new ArrayList<IloConversion>();
		public double lastObjective;

		public Master() {
			createModel();
			addO_init();
			configureCplex();
		}

		private void configureCplex() {
			try {
				// branch and bound
				this.cplex.setParam(IloCplex.Param.MIP.Strategy.NodeSelect, 1);
				this.cplex.setParam(IloCplex.Param.MIP.Strategy.Branch, 1);
				// masterproblem.cplex.setParam(IloCplex.Param.Preprocessing.Presolve,
				// true);
				// display options
				this.cplex.setParam(IloCplex.Param.MIP.Display, 2);
				this.cplex.setParam(IloCplex.Param.Tune.Display, 1);
				this.cplex.setParam(IloCplex.Param.Simplex.Display, 0);
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		private void addO_init() {
			addNewColumn(outcomeSet.get(0));
		}

		private void createModel() {
			try {
				cplex = new IloCplex();
				sw = cplex.addMaximize();
				// IR
				for (Agent agent : agentSet) {
					for (Theta theta : thetaSet) {
						Map<Theta, Agent> TA = new HashMap<Theta, Agent>();
						TA.put(theta, agent);
						IR.put(TA, cplex.addRange(
								outcomeSet.get(0).U(agent, theta),
								Double.MAX_VALUE));
					}
				}
				// IC
				for (Theta theta : thetaSet) {
					for (Theta theta2 : thetaSet) {
						if (theta.ID != theta2.ID) {
							for (Agent agent : agentSet) {
								Map<Theta, Agent> TA = new HashMap<Theta, Agent>();
								TA.put(theta2, agent);
								Map<Theta, Map<Theta, Agent>> TTA = new HashMap<Theta, Map<Theta, Agent>>();
								TTA.put(theta, TA);
								IC.put(TTA, cplex.addRange(0, Double.MAX_VALUE));
							}
						}
					}
				}
				// PROBA
				for (Theta theta : thetaSet) {
					Proba.put(theta, cplex.addRange(0, 0));
				}
			} catch (IloException e) {
				System.err.println("Concert Exception caught: " + e);
			}

		}

		public void addNewColumn(Outcome o) {
			try {
				IloColumn newColumn = null;
				for (Agent agent : agentSet) {
					for (Theta theta : thetaSet) {
						newColumn = cplex.column(sw, o.U(agent, theta));
					}
				}
				// Proba
				for (Theta theta : thetaSet) {
					newColumn = newColumn
							.and(cplex.column(Proba.get(theta), 1));
				}
				// IR
				for (Agent agent : agentSet) {
					for (Theta theta : thetaSet) {
						Map<Theta, Agent> TA = new HashMap<Theta, Agent>();
						TA.put(theta, agent);
						// newColumn =
						// newColumn.and(cplex.column(IR.get(TA)));????????????????????????????????????????????????
					}
				}
				// IC
				for (Theta theta : thetaSet) {
					for (Theta theta2 : thetaSet) {
						for (Agent agent : agentSet) {
							// ???????????????????????????????????????????????????????????????????????????????????????????????
						}
					}
				}
				// g
				o.g = cplex.numVarArray(thetaSet.size(), 0, 1);
			} catch (IloException e) {
				System.err.println("Concert Exception caught: " + e);
			}
		}

		public void solveRelaxation() {
			try {
				if (cplex.solve()) {
					saveDualValues();
					lastObjective = cplex.getObjValue();
				}
			} catch (IloException e) {
				System.err.println("Concert Exception caught: " + e);
			}

		}

		private void saveDualValues() {
			try {
				// Proba
				for (Theta theta : thetaSet) {
					probaDual.put(theta, cplex.getDual(Proba.get(theta)));
				}
				// IR
				for (Agent agent : agentSet) {
					for (Theta theta : thetaSet) {
						Map<Theta, Agent> TA = new HashMap<Theta, Agent>();
						TA.put(theta, agent);
						irDual.put(TA, cplex.getDual(IR.get(TA)));
					}
				}
				// IC
				for (Theta theta : thetaSet) {
					for (Theta theta2 : thetaSet) {
						for (Agent agent : agentSet) {
							Map<Theta, Agent> TA = new HashMap<Theta, Agent>();
							TA.put(theta2, agent);
							Map<Theta, Map<Theta, Agent>> TTA = new HashMap<Barter.Theta, Map<Theta, Agent>>();
							TTA.put(theta, TA);
							icDual.put(TTA, cplex.getDual(IC.get(TTA)));
						}
					}
				}
			} catch (IloException e) {
				System.err.println("Concert Exception caught: " + e);
			}
		}

		public void solveMIP() {
			try {
				convertToMIP();
				if (cplex.solve()) {
					displaySolution();
				} else {
					System.out.println("Integer solution not found");
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}

		}

		private void displaySolution() {
			try {
				for (Outcome outcome : outcomeSet) {
					for (Theta theta : thetaSet) {
						if (cplex.getValue(outcome.g[theta.ID]) > 0.99999) {
							outcome.displayInfo();
						}
					}
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		private void convertToMIP() {
			try {
				for (Outcome outcome : outcomeSet) {
					mipConversion.add(cplex.conversion(outcome.g,
							IloNumVarType.Bool));
					cplex.add(mipConversion.get(mipConversion.size() - 1));
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}

		}

	}

	public class Pricing {
		public IloCplex cplex;
		public double lastObjValue;
		private IloObjective reducedCost;
		public MyMipCallBack mip_call_back;
		
		private class MyMipCallBack extends IloCplex.MIPInfoCallback {
			private boolean aborted;
			private double time_start;
			private double time_limit;

			public void main() {
				try {
					if (!aborted && hasIncumbent()) {
						double time_used = getCplexTime() - time_start;
						if ((getIncumbentObjValue() < -1000)
								|| (time_used > time_limit)
								|| (getBestObjValue() > -0.0001)) {
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
					time_limit = 5;
				} catch (IloException e) {
					System.err.println("Concert exception caught: " + e);
				}
			}
		}
		public void updateReducedCost() {
			// TODO Auto-generated method stub

		}

		public void solve() {
			// TODO Auto-generated method stub

		}

	}

	public Barter() {
		master = new Master();
		pp = new Pricing();
	}

	public void runCG() {
		buildThetaSet();
		build_O_init();
		int iterCounter = 0;
		do {
			iterCounter++;
			master.solveRelaxation();
			pp.updateReducedCost();
			pp.solve();
		} while (pp.lastObjValue < 0);
		master.solveMIP();
	}

	private void build_O_init() {
		Outcome o_init = new Outcome();
		for (Theta t : thetaSet)
			for (Agent a : agentSet)
				o_init.calculateU(t, a);
		outcomeSet.add(o_init);
	}

	private void buildThetaSet() {
		// TODO Auto-generated method stub

	}

}
