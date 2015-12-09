package newVCG;

import java.util.Map;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import elements.*;

public class PP_VCG {
	/**
	 * Resource Type of the PP
	 */
	Resource resource;
	/**
	 * The model for the PP
	 */
	IloCplex pricing;
	/**
	 * [task]
	 */
	IloNumVar[] varphi;
	/**
	 * [agent][task]
	 */
	IloNumVar[][] varphiAgent;

	public PP_VCG(Resource r) {
		resource = r;
	}

	/**
	 * Solve the PP for the given resource
	 */
	public void solve() {
		try {
			// Problem
			pricing = new IloCplex();

			// Variables
			varphi = pricing.numVarArray(CG_VCG.taskSet.size(), 0, 1);

			for (Agent a : CG_VCG.agentSet)
				varphiAgent[a.getID()] = pricing.numVarArray(
						CG_VCG.taskSet.size(), 0, Double.MAX_VALUE);

			// double values
			double u24[], u25[], u26[][], u27[][], u28[], u29 = 0, u30[];

			u24 = new double[CG_VCG.taskSet.size()];
			u25 = new double[CG_VCG.agentSet.size()];
			u26 = new double[CG_VCG.taskSet.size()][CG_VCG.agentSet.size()];
			u27 = new double[CG_VCG.agentSet.size()][CG_VCG.agentSet.size()];
			u28 = new double[CG_VCG.agentSet.size()];
			u30 = new double[CG_VCG.agentSet.size()];
			for (Map<String, Double> r : MP_VCG.dualConstraints) {
				// switch(r.){
				//
				// }
			}

			// Objective
			IloLinearNumExpr ppObjective = pricing.linearNumExpr();
			IloLinearNumExpr numExpr;
			
			double sum = 0;
			for (Agent a : CG_VCG.agentSet) {
				sum+=-u30[a.getID()];
			}
			ppObjective.setConstant(-u29+sum);

			for (Task t : CG_VCG.taskSet) {
				numExpr = pricing.linearNumExpr();
				for (Agent a : CG_VCG.agentSet) {
					numExpr.addTerm(1, varphiAgent[a.getID()][t.getID()]);
				}
				ppObjective.add((IloLinearNumExpr) pricing.prod(
						-u24[t.getID()], numExpr));
			}

			for (Agent a : CG_VCG.agentSet) {
				numExpr = pricing.linearNumExpr();
				for (Task t : CG_VCG.taskSet) {
					numExpr.addTerm(-u25[a.getID()],
							varphiAgent[a.getID()][t.getID()]);
				}
				ppObjective.add(numExpr);
			}
			numExpr = pricing.linearNumExpr();
			for (Agent a : CG_VCG.agentSet) {
				for (Task t : CG_VCG.taskSet) {
					for (Agent a2 : CG_VCG.agentSet) {
						numExpr.addTerm(-u26[t.getID()][a.getID()], varphiAgent[a2.getID()][t.getID()]);
					}
				}
			}
			ppObjective.add(numExpr);

			numExpr = pricing.linearNumExpr();
			for (Agent a : CG_VCG.agentSet) {
				for (Agent a2 : CG_VCG.agentSet) {
					for (Task t : CG_VCG.taskSet) {
						numExpr.addTerm(-u27[a.getID()][a2.getID()], varphiAgent[a.getID()][t.getID()]);
					}
				}
			}
			ppObjective.add(numExpr);
			
			
			pricing.addMaximize(ppObjective);

			// Constraints

			for (Agent a : CG_VCG.agentSet) {
				numExpr = pricing.linearNumExpr();
				for (Task t : CG_VCG.taskSet) {
					if (t.getLocation().equals(a)
							|| a.isNeighbor(t.getLocation())) {
						numExpr.addTerm(1, varphiAgent[a.getID()][t.getID()]);
					}
				}
				pricing.addLe(numExpr, a.getResourceSetAgent(resource.getID()));
			}

			for (Task t : CG_VCG.taskSet) {
				numExpr = pricing.linearNumExpr();
				for (Agent a : CG_VCG.agentSet) {
					if (a.equals(t.getLocation())
							|| a.isNeighbor(t.getLocation())) {
						numExpr.addTerm(1, varphiAgent[a.getID()][t.getID()]);
					}
				}
				numExpr.addTerm(-t.getRequiredResources(resource.getID()),
						varphi[t.getID()]);

				pricing.addEq(numExpr, 0);
			}

			// solve
			if (pricing.solve()) {
				// update
				update();
			}

			// end
			pricing.end();

		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sets the built configuration by the PP
	 */
	public void update() {
		try {
			Configuration configuration = new Configuration(resource);
			for (Task t : CG_VCG.taskSet) {
				configuration.varphi[t.getID()] = pricing.getValue(varphi[t
						.getID()]);
				for (Agent a : CG_VCG.agentSet) {
					configuration.varphiAgent[a.getID()][t.getID()] = pricing
							.getValue(varphiAgent[a.getID()][t.getID()]);
				}
			}
			for (Agent agent : CG_VCG.agentSet) {
				for (Task task : CG_VCG.taskSet) {
					configuration.allocation[agent.getID()] = (int) (agent
							.getResourceSetAgent(resource.getID()) - configuration.varphiAgent[agent
							.getID()][task.getID()]);

				}
			}
			CG_VCG.configurationList.add(configuration);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
}
