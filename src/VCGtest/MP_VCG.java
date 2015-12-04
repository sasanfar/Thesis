package VCGtest;

import ilog.concert.*;
import ilog.cplex.*;

import java.util.ArrayList;
import java.util.List;

import elements.Agent;
import elements.Resource;
import elements.Task;
import user_interface.Interface;
import VCG.ColumnGenVCG;

public class MP_VCG {

	List<IloRange> constraints = new ArrayList<IloRange>();
	List<Double> dualConstraints = new ArrayList<Double>();

	// Parameters
	/**
	 * [Task][Resource]
	 */
	private int[][] Req = new int[Interface.network.TaskSet.size()][Interface.network.ResourceSet
			.size()];
	// Variables
	/**
	 * Task
	 */
	private IloNumVar[] y;
	/**
	 * [Resource][Agent]
	 */
	private IloNumVar[][] E_N;
	/**
	 * [Configuration]
	 */
	public IloNumVar[] z;
	/**
	 * [Agent][Configuration]
	 */
	public IloNumVar[][] z_N;
	/**
	 * [Configuration][Agent][Task]
	 */
	public Double[][][] varphiAgent = new Double[CG_VCG.configurationList
			.size()][CG_VCG.agentSet.size()][CG_VCG.taskSet.size()];
	/**
	 * [Configuration][Task]
	 */
	public Double[][] varphi = new Double[CG_VCG.configurationList.size()][CG_VCG.taskSet
			.size()];

	IloCplex master;

	public void solve(boolean lastTime) {
		try {
			master = new IloCplex();
			// variables
			y = master.numVarArray(CG_VCG.taskSet.size(), 0, 1);
			for (Resource r : CG_VCG.resourceSet)
				E_N[r.getID()] = master.numVarArray(CG_VCG.agentSet.size(), 0,
						Double.MAX_VALUE);
			z = master.numVarArray(CG_VCG.configurationList.size(), 0, 1);
			for (Agent a : CG_VCG.agentSet)
				z_N[a.getID()] = master.numVarArray(
						CG_VCG.configurationList.size(), 0, 1);
			// Objective
			IloLinearNumExpr objective = master.linearNumExpr();
			for (Task task : CG_VCG.taskSet)
				objective.addTerm(task.getUtility(), y[task.getID()]);
			IloLinearNumExpr tmp = master.linearNumExpr();
			for (Agent agent : CG_VCG.agentSet) {
				for (Resource resource : CG_VCG.resourceSet) {
					objective.addTerm(-1, E_N[resource.getID()][agent.getID()]);
					for (Configuration c : CG_VCG.configurationList)
						if (c.getResource().equals(resource))
							for (Agent agent2 : CG_VCG.agentSet)
								if (!agent.equals(agent2))
									for (Task task : agent2.getTaskSetAgent())
										tmp.addTerm(
												task.getUtility()
														* varphi[c.getID()][task
																.getID()],
												z[c.getID()]);
					objective.add(tmp);
				}
			}
			master.addMaximize(objective);

			IloLinearNumExpr numExpr = master.linearNumExpr();

			for (Task t : CG_VCG.taskSet) {
				for (Resource r : CG_VCG.resourceSet) {
					// 24
					numExpr = master.linearNumExpr();
					for (Configuration c : CG_VCG.configurationList)
						if (c.getResource().equals(r))
							for (Agent a : CG_VCG.agentSet)
								if (a.isNeighbor(t.getLocation())
										|| a.equals(t.getLocation())) {
									numExpr.addTerm(varphiAgent[c.getID()][a
											.getID()][t.getID()], z[c.getID()]);
								}
					numExpr.addTerm(-Req[t.getID()][r.getID()], y[t.getID()]);
					constraints.add(master.addGe(numExpr, 0, "C24"));

					// 26
					for (Agent agent : CG_VCG.agentSet) {
						numExpr = master.linearNumExpr();
						for (Configuration c : CG_VCG.configurationList)
							if (c.getResource().equals(r))
								for (Agent a : CG_VCG.agentSet)
									if (!a.equals(agent)
											&& (a.isNeighbor(t.getLocation()) || a
													.equals(t.getLocation()))) {
										numExpr.addTerm(
												varphiAgent[c.getID()][a
														.getID()][t.getID()],
												z_N[a.getID()][c.getID()]);
									}

						numExpr.addTerm(-Req[t.getID()][r.getID()],
								y[t.getID()]);
						constraints.add(master.addGe(numExpr, 0, "C26"));
					}
				}
			}

			for (Agent a : CG_VCG.agentSet) {
				for (Resource r : CG_VCG.resourceSet) {
					// 25
					numExpr = master.linearNumExpr();
					for (Configuration c : CG_VCG.configurationList)
						if (c.getResource().equals(r))
							for (Task t : CG_VCG.taskSet)
								numExpr.addTerm(varphiAgent[c.getID()][a
										.getID()][t.getID()], z[c.getID()]);
					constraints.add(master.addLe(numExpr,
							a.getResourceSetAgent(r.getID()), "C25"));

					// 27
					for (Agent a2 : CG_VCG.agentSet)
						if (!a2.equals(a)) {
							numExpr = master.linearNumExpr();
							for (Configuration c : CG_VCG.configurationList)
								if (c.getResource().equals(r))
									for (Task t : CG_VCG.taskSet)
										numExpr.addTerm(
												varphiAgent[c.getID()][a2
														.getID()][t.getID()],
												z_N[a.getID()][c.getID()]);
							constraints.add(master.addLe(numExpr,
									a2.getResourceSetAgent(r.getID()), "C27"));
						}
				}
			}

			// 28
			for (Agent a : CG_VCG.agentSet) {
				for (Resource r : CG_VCG.resourceSet) {
					numExpr = master.linearNumExpr();
					for (Configuration c : CG_VCG.configurationList) {
						if (c.getResource().equals(r)) {
							for (Agent a2 : CG_VCG.agentSet) {
								if (!a2.equals(a)) {
									for (Task t : a2.getTaskSetAgent()) {
										numExpr.addTerm(t.getUtility()
												* varphi[c.getID()][t.getID()],
												z_N[a.getID()][c.getID()]);
									}
								}
							}
						}
					}
					numExpr.addTerm(-1, E_N[r.getID()][a.getID()]);
					constraints.add(master.addLe(0, numExpr, "C28"));
				}
			}

			for (Resource resource : CG_VCG.resourceSet) {
				// 29
				numExpr = master.linearNumExpr();
				for (Configuration configuration : CG_VCG.configurationList) {
					if (configuration.getResource().equals(resource))
						numExpr.addTerm(1, z[configuration.getID()]);
				}
				constraints.add(master.addLe(numExpr, 1, "C29"));
				// 30
				for (Agent agent : CG_VCG.agentSet) {
					numExpr = master.linearNumExpr();
					for (Configuration configuration : CG_VCG.configurationList) {
						if (configuration.getResource().equals(resource))
							numExpr.addTerm(1,
									z_N[agent.getID()][configuration.getID()]);
					}
					constraints.add(master.addLe(numExpr, 1, "C30"));
				}
			}

			if (master.solve()) {

			}
			if (!lastTime)
				master.end();

		} catch (IloException e) {
			System.err.println("Error in Master Problem");
			e.printStackTrace();
		}
	}

	public void solveMIP() {

	}

}
