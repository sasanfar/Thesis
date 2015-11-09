package elements;

import java.util.*;

public class Agent {
	int ID;
	int[] ResourceSetAgent;
	List<Task> TaskSetAgent;
	List<Agent> Neighbors;

	public Agent(int iD, int _maxNumberResourceTypes) {
		ID = iD;
		Neighbors = new ArrayList<Agent>();
		TaskSetAgent = new ArrayList<Task>();
		ResourceSetAgent = new int[_maxNumberResourceTypes];
	}

	public int getResourceSetAgent(int i) {
		return ResourceSetAgent[i];
	}

	public void setResourceSetAgent(int quantity, Resource resource) {
		ResourceSetAgent[resource.getID()] = quantity;
	}

	public List<Task> getTaskSetAgent() {
		return TaskSetAgent;
	}

	public void setTaskSetAgent(Task task) {
		TaskSetAgent.add(task);
	}

	public List<Agent> getNeighbors() {
		return Neighbors;
	}

	public void setNeighbor(Agent neighbor) {
		if (!Neighbors.contains(neighbor)) {
			Neighbors.add(neighbor);
			neighbor.setNeighbor(this);
		}
	}

	public int getID() {
		return ID;
	}

	public boolean isNeighbor(Agent agent) {
		for (Agent a : Neighbors) {
			if (agent == a)
				return true;
		}
		return false;
	}

	public int[] getResourceSetAgent() {
		// TODO Auto-generated method stub
		return this.ResourceSetAgent;
	}
}
