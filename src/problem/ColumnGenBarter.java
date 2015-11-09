package problem;

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

	public static Map<Integer, Agent> all_agents = new HashMap<Integer, Agent>();
	public static Map<Integer, Resource> all_resources = new HashMap<Integer, Resource>();
	public static Map<Integer, Task> all_tasks = new HashMap<Integer, Task>();

	public MasterProblem masterproblem;
	public SubProblem subproblem;
	public Logger logger = new Logger();
	public List<Assign> assigns = new ArrayList<Assign>();

	private class Assign {
		public int ID;
		public double cost;
		public List<Agent> agents;

		public Assign() {
			agents = new ArrayList<Agent>();

		}

	}

	public class MasterProblem {
		public IloCplex cplex;
		private IloObjective socialWelfare;
		private Map<Agent, IloRange> row_agents = new HashMap<Agent, IloRange>();
		private Map<Agent, Double> pi = new HashMap<Agent, Double>();
		private List<IloConversion> mipConversion = new ArrayList<IloConversion>();
		public double lastObjValue;

		public Logger logger = new Logger();

		public MasterProblem() {
			createModel();
			createDefaultPaths();
			Parameters.configureCplex(this);
		}

		public void createDefaultPaths() {
			// for (Customer c : all_customers.values()) {
			// List<Integer> new_path = new ArrayList<Integer>();
			// new_path.add(depot_start.id);
			// new_path.add(c.id);
			// new_path.add(depot_end.id);
			// addNewColumn(new Path(new_path));
			// }
		}

		public void createModel() {
			try {
				cplex = new IloCplex();
				socialWelfare = cplex.addMaximize();
				// for (Customer customer : all_customers.values())
				// row_agents.put(customer,
				// cplex.addRange(1, 1, "cust " + customer.id));
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void addNewColumn() {
			// try {
			// IloColumn new_column = cplex.column(total_cost, path.cost);
			// for (Customer c : all_customers.values())
			// new_column = new_column.and(cplex.column(row_agents.get(c),
			// path.a(c)));
			// path.y = cplex.numVar(new_column, 0, 1, "y." + path.id);
			// } catch (IloException e) {
			// System.err.println("Concert exception caught: " + e);
			// }
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
			
		}

		public void displaySolution() {
		
		}

	}

	public class SubProblem {
		public IloCplex cplex;
		private IloIntVar[][] x;
		private IloNumVar[] s;
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
			setPriority();
			Parameters.configureCplex(this);
			this.mip_call_back = new MyMipCallBack();
			try {
				cplex.use(mip_call_back);
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void setPriority() {
			// try {
			// for (int j : all_customers.keySet())
			// cplex.setPriority(x[depot_start.id][j], 1);
			// } catch (IloException e) {
			// System.err.println("Concert exception caught: " + e);
			// }
		}

		public void createModel() {
			// try {
			// // define model
			// cplex = new IloCplex();
			// // define variables
			// x = new IloIntVar[all_nodes.size()][];
			// for (int i : all_nodes.keySet()) {
			// x[i] = cplex.boolVarArray(all_nodes.size());
			// for (int j : all_nodes.keySet()) {
			// x[i][j].setName("x." + i + "." + j);
			// }
			// }
			// s = cplex.numVarArray(all_nodes.size(), 0, Double.MAX_VALUE);
			// // define parameters
			// double M = 0;
			// for (int i = 0; i < all_customers.size(); i++)
			// for (int j = 0; j < all_customers.size(); j++) {
			// double val = all_customers.get(i).b()
			// + all_customers.get(i).time_to_node(
			// all_customers.get(j))
			// + all_customers.get(i).time_at_node()
			// - all_customers.get(j).a();
			// if (M < val)
			// M = val;
			// }
			// double q = Parameters.capacity;
			// // set objective
			// reduced_cost = cplex.addMinimize();
			// // set constraint : capacity
			// num_expr = cplex.linearNumExpr();
			// for (int i : all_customers.keySet())
			// for (int j : all_nodes.keySet())
			// num_expr.addTerm(all_customers.get(i).d(), x[i][j]);
			// constraints.add(cplex.addLe(num_expr, q, "c1"));
			// // set constraint : start from depot
			// num_expr = cplex.linearNumExpr();
			// for (int j : all_nodes.keySet())
			// num_expr.addTerm(1.0, x[depot_start.id][j]);
			// constraints.add(cplex.addEq(num_expr, 1, "c2"));
			// // set constraint : flow conservation
			// for (int h : all_customers.keySet()) {
			// num_expr = cplex.linearNumExpr();
			// for (int i : all_nodes.keySet())
			// num_expr.addTerm(1.0, x[i][h]);
			// for (int j : all_nodes.keySet())
			// num_expr.addTerm(-1.0, x[h][j]);
			// constraints.add(cplex.addEq(num_expr, 0, "c3"));
			// }
			// // set constraint : end at depot
			// num_expr = cplex.linearNumExpr();
			// for (int i : all_nodes.keySet())
			// num_expr.addTerm(1.0, x[i][depot_end.id]);
			// constraints.add(cplex.addEq(num_expr, 1, "c4"));
			// // set constraint : sub tour elimination
			// for (int i : all_nodes.keySet())
			// for (int j : all_nodes.keySet()) {
			// double t_ij = all_nodes.get(i).time_at_node()
			// + all_nodes.get(i).time_to_node(
			// all_nodes.get(j));
			// num_expr = cplex.linearNumExpr();
			// num_expr.addTerm(1.0, s[i]);
			// num_expr.addTerm(-1.0, s[j]);
			// num_expr.addTerm(M, x[i][j]);
			// constraints.add(cplex.addLe(num_expr, M - t_ij, "c5"));
			// }
			// // set constraint : time windows
			// for (int i : all_customers.keySet()) {
			// constraints.add(cplex.addGe(s[i], all_customers.get(i).a(),
			// "c6"));
			// constraints.add(cplex.addLe(s[i], all_customers.get(i).b(),
			// "c6"));
			// }
			// // prohibited moves
			// num_expr = cplex.linearNumExpr();
			// for (int i : all_nodes.keySet()) {
			// num_expr.addTerm(1.0, x[depot_end.id][i]);
			// num_expr.addTerm(1.0, x[i][depot_start.id]);
			// num_expr.addTerm(1.0, x[i][i]);
			// }
			// constraints.add(cplex.addEq(num_expr, 0, "c7"));
			// } catch (IloException e) {
			// System.err.println("Concert exception caught: " + e);
			// }
		}

		public void updateReducedCost() {
			// try {
			// IloLinearNumExpr num_expr = cplex.linearNumExpr();
			// for (int i : all_agents.keySet()) {
			// for (int j : all_agents.keySet()) {
			// double t_ij = all_agents.get(i).time_at_node()
			// + all_agents.get(i).time_to_node(
			// all_nodes.get(j));
			// if (all_customers.keySet().contains(i))
			// num_expr.addTerm(
			// t_ij
			// - ColumnGenBarter.this.masterproblem.pi
			// .get(all_customers.get(i)),
			// x[i][j]);
			// else
			// num_expr.addTerm(t_ij, x[i][j]);
			// }
			// }
			// reduced_cost.clearExpr();
			// reduced_cost.setExpr(num_expr);
			// } catch (IloException e) {
			// System.err.println("Concert exception caught: " + e);
			// }
		}

		public void solve() {
			try {
				mip_call_back.reset();
				if (cplex.solve()) {
					this.lastObjValue = cplex.getObjValue();
					this.lastObjValueRelaxed = cplex.getBestObjValue();
					int nPool = cplex.getSolnPoolNsolns();
					for (int i = 0; i < nPool; i++) {
						if (cplex.getObjValue(i) < Parameters.ColGen.zero_reduced_cost) {
							// savePath(i);
							saveVal(i);
							/* add code */
						}
					}
				}
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}

		public void saveVal(int nSol) {

		}
		// public void savePath(int nSol) {
		// try {
		// // print
		// /*
		// * for (int i : all_nodes.keySet()) for (int j :
		// * all_nodes.keySet()) if (cplex.getValue(x[i][j],nSol) >
		// * 0.99999) System.out.println(i+","+j);
		// */
		// // save
		// List<Integer> stops_new_path = new ArrayList<Integer>();
		// for (int i : all_customers.keySet()) {
		// if (cplex.getValue(x[depot_start.id][i], nSol) > 0.99999) {
		// stops_new_path.add(depot_start.id);
		// stops_new_path.add(i);
		// while (i != depot_end.id) {
		// for (int j : all_nodes.keySet()) {
		// if (cplex.getValue(x[i][j], nSol) > 0.99999) {
		// stops_new_path.add(j);
		// i = j;
		// break;
		// }
		// }
		// }
		// break;
		// }
		// }
		// ColumnGen.this.masterproblem.addNewColumn(new Path(
		// stops_new_path));
		// } catch (IloException e) {
		// System.err.println("Concert exception caught: " + e);
		// }
		// }
	}

	public ColumnGenBarter() {
		ReadData();
		masterproblem = new MasterProblem();
		subproblem = new SubProblem();
	}

	private void ReadData() {
		// try {
		// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// String db_connect_string =
		// "jdbc:sqlserver://ultraman1.eng.buffalo.edu;instance=SQLEXPRESSHC;databaseName=Seminar";
		// Connection connection = DriverManager.getConnection(
		// db_connect_string, "open_user", "bell");
		// Statement statement = connection.createStatement();
		// String strsql =
		// "SELECT Customer, Xcoord, Ycoord, Demand, ReadyTime, DueDate, ServiceTime FROM Customers WHERE Instance='"
		// + instance + "';";
		// statement.execute(strsql);
		// ResultSet results = statement.getResultSet();
		// while ((results != null) && (results.next())) {
		// int id_external = results.getInt("Customer");
		// double xcoord = results.getDouble("Xcoord");
		// double ycoord = results.getDouble("Ycoord");
		// double demand = results.getDouble("Demand");
		// double readyt = results.getDouble("ReadyTime");
		// double duedate = results.getDouble("DueDate");
		// double servicet = results.getDouble("ServiceTime");
		// new Customer(id_external, xcoord, ycoord, demand, readyt,
		// duedate, servicet);
		// }
		// strsql =
		// "SELECT Depot, Xcoord, Ycoord, Descr FROM Depots WHERE Instance='"
		// + instance + "';";
		// statement.execute(strsql);
		// results = statement.getResultSet();
		// while ((results != null) && (results.next())) {
		// int id_external = results.getInt("Depot");
		// double xcoord = results.getDouble("Xcoord");
		// double ycoord = results.getDouble("Ycoord");
		// String descr = results.getString("Descr");
		// if (descr.equals("start")) {
		// depot_start = new Depot(id_external, xcoord, ycoord);
		// } else if (descr.equals("end")) {
		// depot_end = new Depot(id_external, xcoord, ycoord);
		// }
		// }
		// connection.close();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
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
			System.out.print("   nPaths");
			System.out.print("       MP lb");
			System.out.print("       SB lb");
			System.out.print("      SB int");
			System.out.println();
		}
		System.out.format("%9.0f", (double) iter);
		System.out.format("%9.1f", logger.timeStamp() / 60);
		// System.out.format("%9.0f", (double) paths.size());
		System.out.format("%12.4f", masterproblem.lastObjValue); // master lower
																	// bound
		System.out.format("%12.4f", subproblem.lastObjValueRelaxed);// sub lower
																	// bound
		System.out.format("%12.4f", subproblem.lastObjValue);// sub lower bound
		System.out.println();
	}
}
