package Barter;

import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;

import java.util.ArrayList;
import java.util.List;

import user_interface.Interface;
import elements.Agent;

class Output {
	public int ID;
	public double cost;
	public IloIntVar[][] g = new IloIntVar[Interface.network.AgentSet.size()][Interface.network.ResourceSet.size()];
	public double[] utilityAgent = new double[Interface.network.AgentSet.size()];

	public int[][] allocation = new int[Interface.network.AgentSet.size()][Interface.network.ResourceSet
			.size()];

	public Output(int id) {
		ID=id;
	}

}