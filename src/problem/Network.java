package problem;

import elements.*;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Network {

	int _numAgents = 100;
	int _numTasks = 50;
	int _numResourceTypes = 20;
	int _maxEachTypePerAgent = 10;
	int _maxEachTypePerTask = 5;
	
	public List<Task> TaskSet = new ArrayList<Task>();
	public List<Resource> ResourceSet = new ArrayList<Resource>();
	public List<Agent> AgentSet = new ArrayList<Agent>();
	public List<Link> LinkSet = new ArrayList<Link>();

	// private static Network instance = new Network();
	//
	// public static Network getInstance() {
	// if (instance == null)
	// instance = new Network();
	// return instance;
	// }

	/*
	 * @Override public String toString() { String s = ""; for (Link link :
	 * Links) {
	 * 
	 * s += link.get_firstNode() + "->" + link.getSecondNode()+ " \n"; } return
	 * s; }
	 */

	public boolean initializeNetwork() {

		// The size of the problem
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		// System.out.println("Insert the number of agents");
		// try {
		// _numAgents = br.read();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// System.out.println("Insert the total number of tasks");
		// try {
		// _numTasks = br.read();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// System.out.println("Insert the total number of resource types");
		// try {
		// _numResourceTypes = br.read();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		if (_numAgents <= 0 || _numResourceTypes <= 0 || _numTasks <= 0) {
			System.out.println("Error in inputs!");
			return false; // return "FALSE"
		}

		// Building the network randomly
		// adding resources
		for (int id = 0; id < _numResourceTypes; id++) {
			Resource r = new Resource(id);
			ResourceSet.add(r);
		}

		// adding agents
		for (int id = 0; id < _numAgents; id++) {
			Agent a = new Agent(id, _numResourceTypes);
			AgentSet.add(a);
		}

		// adding tasks and locating them
		for (int id = 0; id < _numTasks; id++) {
			Task t = new Task(id, _numResourceTypes);
			int a = (int) (Math.random() * _numAgents);
			t.setLocation(AgentSet.get(a));
			AgentSet.get(a).setTaskSetAgent(t);
			t.setUtility((int) (Math.random() * 1000));
			TaskSet.add(t);
		}

		// adding links and setting neighbors
		/*
		 * int _numberLinks = (int) (Math.random() * _maxAgents * (_maxAgents -
		 * 1) / 2); for (int id = 0; id <= _numberLinks; id++) { int a1 = (int)
		 * (Math.random() * _maxAgents); int a2 = (int) (Math.random() *
		 * _maxAgents); if (a1 != a2) { Link l = new Link(id, AgentSet.get(a1),
		 * AgentSet.get(a2)); LinkSet.add(l);
		 * AgentSet.get(a1).setNeighbor(AgentSet.get(a2));
		 * AgentSet.get(a2).setNeighbor(AgentSet.get(a1)); } }
		 */
		int linkID = 0;
		for (Agent a1 : AgentSet) {
			for (Agent a2 : AgentSet) {
				if (a2 != a1 && !a1.isNeighbor(a2) && Math.random() >= 0.5) {
					Link l = new Link(linkID, a1, a2);
					linkID++;
					a2.setNeighbor(a1);
					a1.setNeighbor(a2);
					LinkSet.add(l);
				}
			}
		}
		if (LinkSet.size() < _numAgents - 1)
			return false; // return "FALSE" if smaller than a tree

		// assigning resources to agents
		for (Resource r : ResourceSet) {
			for (Agent a : AgentSet) {
				if (Math.random() >= 0.5) {
					a.setResourceSetAgent(
							(int) (Math.random() * _maxEachTypePerAgent), r);
					r.setLocation(a);
				}
			}
		}

		// assigning resources to tasks
		for (Resource r : ResourceSet) {
			for (Task t : TaskSet) {
				if (Math.random() >= 0.5) {
					t.setRequiredResources(
							(int) (Math.random() * _maxEachTypePerTask), r);
				}
			}
		}

		print();

		return true;
	}

	private void print() {
		// PRINTING ADJACENCY MATRIX
		System.out.println("ADJACENCY MATRIX:");
		System.out.println("=================");
		System.out.print("    ");
		for (int i = 0; i < _numAgents; i++)
			System.out.printf("A%-4d", i);
		System.out.println();
		for (int i = 0; i < _numAgents; i++) {
			System.out.printf("A%-4d", i);
			for (int j = 0; j < _numAgents; j++) {
				if (AgentSet.get(i).isNeighbor(AgentSet.get(j)))
					System.out.printf("%-5d", 1);
				else
					System.out.printf("%-5d", 0);
			}
			System.out.println();
		}

		// PRINTING RESOURCE ALLOCATION
		

		System.out.println("\n\nINITIAL RESOURCE ALLOCATION:");
		System.out.println("============================");
		System.out.print("    ");
		for (Resource resource : ResourceSet) {
			System.out.printf("R%-4d", resource.getID());
		}
		System.out.println();
		for (Agent agent : AgentSet) {
			System.out.printf("A%-4d", agent.getID());
			for (Resource resource : ResourceSet) {
				System.out.printf("%-5d", agent
						.getResourceSetAgent(resource.getID()));
			}
			System.out.println();
		}

		// PRINT TASK LOCATIONS
		System.out.println("\n\nTASK LOCATIONS:");
		System.out.println("===================");
		
		for (Agent agent : AgentSet) {
			System.out.print("A"+agent.getID()+": ");
			for (Task task: agent.getTaskSetAgent()){
				System.out.print("t"+ task.getID()+" ");
			}
			System.out.println();
		}
		
		// PRINT TASK REQUIREMENTS
		System.out.println("\n\nTASK REQUIREMENTS:");
		System.out.println("======================");
		for (Task task : TaskSet) {
			System.out.print("t"+ task.getID()+": ");
			for (Resource resource : ResourceSet) {
				System.out.print(task.getRequiredResources(resource.getID())+"*R"+resource.getID()+" ");
			}
			System.out.println();
		}
	}

	boolean saveNetwork() {
		return true;
	}

	boolean loadNetwork() {

		return true;
	}

}
