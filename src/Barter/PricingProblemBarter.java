package Barter;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import VCG.ColumnGenVCG;

public class PricingProblemBarter {
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
					if ((getIncumbentObjValue() < ParametersBarter.ColGen.subproblemObjVal)
							|| (time_used > time_limit)
							|| (getBestObjValue() > ParametersBarter.ColGen.zero_reduced_cost)) {
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
				time_limit = ParametersBarter.ColGen.subproblemTiLim;
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}
	}

	public PricingProblemBarter() {
		this.constraints = new ArrayList<IloConstraint>();
		createModel();
		setPriority();
		ParametersBarter.configureCplex(this);
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
		try {
			// define model
			cplex = new IloCplex();			
			

			// set objective
			reduced_cost = cplex.addMaximize();
			
			// define variables
			
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void updateReducedCost() {
		 try {
		IloLinearNumExpr numExpr = cplex.linearNumExpr();

		for (IloConstraint r : ColumnGenBarter.getInstance().masterproblem.constraints){
			if (r.getName().equalsIgnoreCase("IC")) {
				numExpr.addTerm(coef, var);
			} else if (r.getName().equalsIgnoreCase("IR")) {
				// num_expr.add(arg0);
			} else if (r.getName().equalsIgnoreCase("proba")) {
				// num_expr.add(arg0);
				/*
				 * } else if (r.getName().equals("C28")) { //
				 * num_expr.add(arg0); } ...
				 */
			}
		}
		reduced_cost.clearExpr();
		reduced_cost.setExpr(num_expr);
		 } catch (IloException e) {
		 System.err.println("Concert exception caught: " + e);
		 }
	}

	public void solve() {
		try {
			mip_call_back.reset();
			if (cplex.solve()) {
				this.lastObjValue = cplex.getObjValue();
				this.lastObjValueRelaxed = cplex.getBestObjValue();
				int nPool = cplex.getSolnPoolNsolns();
				for (int i = 0; i < nPool; i++) {
					if (cplex.getObjValue(i) < ParametersBarter.ColGen.zero_reduced_cost) {
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