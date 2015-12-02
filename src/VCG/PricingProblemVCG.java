package VCG;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import problem.*;
import user_interface.Interface;
import elements.*;

public class PricingProblemVCG {

	Resource resourceType;
	public IloCplex cplex;
	private IloObjective reduced_cost;
	private IloLinearNumExpr num_expr;
	private List<IloConstraint> constraints;
	public MyMipCallBack mip_call_back;
	public double lastObjValue;
	public double lastObjValueRelaxed;
	// define variables
	public IloNumVar varphiAgent[][] = new IloNumVar[Interface.network.AgentSet
			.size()][Interface.network.TaskSet.size()];
	public IloIntVar varphi[] = new IloIntVar[Interface.network.TaskSet.size()];

	private class MyMipCallBack extends IloCplex.MIPInfoCallback {
		private boolean aborted;
		private double time_start;
		private double time_limit;

		public void setResourceType(Resource r) {
			resourceType = r;
		}

		public void main() {
			try {
				if (!aborted && hasIncumbent()) {
					double time_used = getCplexTime() - time_start;
					if ((getIncumbentObjValue() < ParametersVCG.ColGen.subproblemObjVal)
							|| (time_used > time_limit)
							|| (getBestObjValue() > ParametersVCG.ColGen.zero_reduced_cost)) {
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
				time_limit = ParametersVCG.ColGen.subproblemTiLim;
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		}
	}

	public PricingProblemVCG() {
		this.constraints = new ArrayList<IloConstraint>();
		createModel();
		setPriority();
		ParametersVCG.configureCplex(this);
		this.mip_call_back = new MyMipCallBack();
		try {
			cplex.use(mip_call_back);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void setPriority() {
		// try {
		// // for (int j : Interface.network.AgentSet) {
		// // // cplex.setPriority(/**/, 1);
		// // }
		// } catch (IloException e) {
		// System.err.println("Concert exception caught: " + e);
		// }

	}

	public void createModel() {
		try {
			// define model
			cplex = new IloCplex();
			for (Task t : Interface.network.TaskSet) {
				varphi[t.getID()] = cplex.intVar(0, 1);

				for (Agent a : Interface.network.AgentSet)
					varphiAgent[a.getID()][t.getID()] = cplex.numVar(0,
							Double.MAX_VALUE);

			}

			// set objective
			reduced_cost = cplex.addMaximize();

			// set constraint :
			for (Agent a : Interface.network.AgentSet) {
				num_expr = cplex.linearNumExpr();
				for (Task t : Interface.network.TaskSet) {
					if (a.isNeighbor(t.getLocation()) || t.getLocation() == a) {
						num_expr.add((IloLinearNumExpr) varphiAgent[a.getID()][t
								.getID()]);
					}
				}
				constraints.add(cplex.addLe(num_expr,
						a.getResourceSetAgent(resourceType.getID())));
			}
			// // set constraint :
			for (Task t : Interface.network.TaskSet) {
				num_expr = cplex.linearNumExpr();
				for (Agent a : Interface.network.AgentSet) {
					if (t.getLocation() == a || a.isNeighbor(t.getLocation())) {
						num_expr.add((IloLinearNumExpr) varphiAgent[a.getID()][t
								.getID()]);
					}
				}
				constraints.add(cplex.addEq(
						num_expr,
						cplex.prod(varphi[t.getID()],
								t.getRequiredResources(resourceType.getID()))));
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void updateReducedCost(Resource resource) {
		try {
			IloLinearNumExpr num_expr = cplex.linearNumExpr();

			for (IloConstraint r : ColumnGenVCG.getInstance().masterproblem.constraints)
				if (r.getName().equals("C24")) {
					 //num_expr.add();
				} else if (r.getName().equals("C25")) {
					// num_expr.add(arg0);
				} else if (r.getName().equals("C26")) {
					// num_expr.add(arg0);
				} else if (r.getName().equals("C27")) {
					// num_expr.add(arg0);
					/*
					 * } else if (r.getName().equals("C28")) { //
					 * num_expr.add(arg0); } ...
					 */
				}
			reduced_cost.clearExpr();
			reduced_cost.setExpr(num_expr);
		} catch (IloException e) {

		}
	}

	public void solve(Resource r) {
		try {
			mip_call_back.reset();
			if (cplex.solve()) {
				this.lastObjValue = cplex.getObjValue();
				this.lastObjValueRelaxed = cplex.getBestObjValue();
				int nPool = cplex.getSolnPoolNsolns();
				for (int i = 0; i < nPool; i++) {
					if (cplex.getObjValue(i) < ParametersVCG.ColGen.zero_reduced_cost) {
						addConfiguration(i);
					}
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	private void addConfiguration(int nSol) {
		// Configuration c = new Configuration();

		// ColumnGenVCG.configList.add(c);
	}

}
