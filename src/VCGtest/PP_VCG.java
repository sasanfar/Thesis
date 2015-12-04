package VCGtest;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import elements.*;

public class PP_VCG {
	Resource resource;

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

	public void solve() {
		try {
			// Problem
			pricing = new IloCplex();

			// Variables
			varphi = pricing.numVarArray(CG_VCG.taskSet.size(), 0, 1);

			for (Agent a : CG_VCG.agentSet)
				varphiAgent[a.getID()] = pricing.numVarArray(
						CG_VCG.taskSet.size(), 0, Double.MAX_VALUE);

			// Objective
			IloLinearNumExpr ppObjective = pricing.linearNumExpr();
			
			
			
			pricing.addMaximize(ppObjective);

			// Constraints

			IloLinearNumExpr numExpr;
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
				for (Task task : CG_VCG.taskSet){
					configuration.allocation[agent.getID()] = (int) (agent
							.getResourceSetAgent(resource.getID())
							- configuration.varphiAgent[agent.getID()][task
									.getID()]);
					
					
				}
			}
			CG_VCG.configurationList.add(configuration);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
}
