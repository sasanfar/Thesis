package problem;

import elements.*;

import java.util.*;
import java.io.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Network  implements java.io.Serializable {

	public List<Task> TaskSet = new ArrayList<Task>();
	public List<Resource> ResourceSet = new ArrayList<Resource>();
	public List<Agent> AgentSet = new ArrayList<Agent>();
	public List<Link> LinkSet = new ArrayList<Link>();

	private static Network instance = new Network();

	private Network() {
	}

	public static Network getInstance() {
		if (instance == null)
			instance = new Network();
		return instance;
	}

	public static Network newNetwork(int numAgents, int numTasks,
			int numResourceTypes, int maxEachTypePerAgent,
			int maxEachTypePerTask) {
		// instance = new Network();
		instance.initializeNetwork(numAgents, numTasks, numResourceTypes,
				maxEachTypePerAgent, maxEachTypePerTask);
		return instance;
	}

	public boolean initializeNetwork(int numAgents, int numTasks,
			int numResourceTypes, int maxEachTypePerAgent,
			int maxEachTypePerTask) {

		TaskSet = new ArrayList<Task>();
		ResourceSet = new ArrayList<Resource>();
		AgentSet = new ArrayList<Agent>();
		LinkSet = new ArrayList<Link>();

		if (numAgents <= 0 || numResourceTypes <= 0 || numTasks <= 0) {
			System.out.println("Error in inputs!");
			return false;
		}

		// Building the network randomly
		// adding resources
		for (int id = 0; id < numResourceTypes; id++) {
			Resource r = new Resource(id);
			ResourceSet.add(r);
		}

		// adding agents
		for (int id = 0; id < numAgents; id++) {
			Agent a = new Agent(id, numResourceTypes);
			AgentSet.add(a);
		}

		// adding tasks and locating them
		for (int id = 0; id < numTasks; id++) {
			Task t = new Task(id, numResourceTypes);
			int a = (int) (Math.random() * numAgents);
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
					a1.setNeighbor(a2);
					LinkSet.add(l);
				}
			}
		}
		if (LinkSet.size() < numAgents - 1)
			return false; // return "FALSE" if SMALLER than a TREE

		// assigning resources to agents
		for (Resource r : ResourceSet) {
			for (Agent a : AgentSet) {
				if (Math.random() >= 0.5) {
					a.setResourceSetAgent(
							(int) (Math.random() * maxEachTypePerAgent), r);
					r.setLocation(a);
				}
			}
		}

		// assigning resources to tasks
		for (Resource r : ResourceSet) {
			for (Task t : TaskSet) {
				if (Math.random() >= 0.5) {
					t.setRequiredResources(
							(int) (Math.random() * maxEachTypePerTask), r);
				}
			}
		}

		return true;
	}

	public void print() {
		// PRINTING ADJACENCY MATRIX
		System.out.println("\n\nADJACENCY MATRIX:");
		System.out.println("=================");
		System.out.print("    ");
		for (Agent agent : AgentSet)
			System.out.printf("A%-4d", agent.getID());
		System.out.println();
		for (Agent agent : AgentSet) {
			System.out.printf("A%-4d", agent.getID());
			for (Agent agent2 : AgentSet) {
				if (AgentSet.get(agent.getID()).isNeighbor(
						AgentSet.get(agent2.getID())))
					System.out.printf("%-5d", 1);
				else
					System.out.printf("%-5d", 0);
			}
			System.out.println();
		}

		// PRINTING AGENTS' LOCATIONS
		System.out.println("\nAGENTS' LOCATIONS:");
		System.out.println("==================");
		for (Agent agent : AgentSet) {
			System.out.printf("A%-2d:(%.2f,%.2f)\n", agent.getID(),
					agent.getX(), agent.getY());
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
				System.out.printf("%-5d",
						agent.getResourceSetAgent(resource.getID()));
			}
			System.out.println();
		}

		// PRINT TASK LOCATIONS
		System.out.println("\n\nTASK LOCATIONS:");
		System.out.println("===================");

		for (Agent agent : AgentSet) {
			System.out.printf("A%-4d: ", agent.getID());
			for (Task task : agent.getTaskSetAgent()) {
				System.out.print("t" + task.getID() + " ");
			}
			System.out.println();
		}

		// PRINT TASK REQUIREMENTS
		System.out.println("\n\nTASK REQUIREMENTS:");
		System.out.println("======================");
		for (Task task : TaskSet) {
			System.out.printf("t%-4d: ", task.getID());
			for (Resource resource : ResourceSet) {
				System.out.print(task.getRequiredResources(resource.getID())
						+ "*R" + resource.getID() + " ");
			}
			System.out.println();
		}
	}

	// public boolean saveNetwork(String file) throws FileNotFoundException,
	// UnsupportedEncodingException {
	// if (!file.endsWith(".txt")) {
	// file = file + ".txt";
	// }
	// PrintWriter writer = new PrintWriter(file, "UTF-8");
	// writer.println("#Agents: " + this.AgentSet.size());
	// writer.println("#Resource: " + this.ResourceSet.size());
	// writer.println("#Tasks: " + this.TaskSet.size());
	// writer.println("#Types per Task: " + this.maxEachTypePerTask);
	// writer.println("#########################################");
	// for (Agent agent : this.AgentSet) {
	// writer.println("A" + agent.getID() + ":");
	// writer.println("NEIGHBOURS:");
	// for (Agent agent2 : agent.getNeighbors())
	// writer.print("A" + agent2.getID() + " ");
	// writer.println("RESOURCES:");
	// for (Resource resource : this.ResourceSet)
	// writer.print(agent.getResourceSetAgent(resource.getID()) + "*R"
	// + resource.getID() + " ");
	// writer.println("TASKS:");
	// for (Task task : agent.getTaskSetAgent())
	// writer.print("T" + task.getID() + " ");
	// }
	//
	// writer.close();
	// return true;
	// }
	//
	// public static Network loadNetwork(String fileLocation) {
	// Network loaded = new Network();
	//
	// File file = new File(fileLocation);
	//
	// try {
	// Scanner sc = new Scanner(file);
	// loaded.numAgents = sc.nextInt();
	// loaded.numResourceTypes = sc.nextInt();
	// loaded.numTasks = sc.nextInt();
	//
	// loaded.readNetwork(sc.nextInt(),sc.nextInt(),sc.nextInt(),sc.nextInt(),sc.nextInt());
	// while (sc.hasNextLine()) {
	// // Neighbors
	// int tempAgent = sc.nextInt();
	// while (!sc.next().contains("RESOURCES:")) {
	// int neighboringAgent = sc.nextInt();
	// loaded.AgentSet.get(tempAgent).setNeighbor(
	// loaded.AgentSet.get(neighboringAgent));
	// }
	// // Resources
	// int _resource;
	// int _quantity;
	//
	// }
	// sc.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// return instance;
	// }
	//
	// public void readNetwork(int _numAgents, int _numTasks,
	// int _numResourceTypes, int _maxEachTypePerAgent,
	// int _maxEachTypePerTask) {
	// this.numAgents = _numAgents;
	// this.numTasks = _numTasks;
	// this.numResourceTypes = _numResourceTypes;
	// this.maxEachTypePerAgent = _maxEachTypePerAgent;
	// this.maxEachTypePerTask = _maxEachTypePerTask;
	//
	// if (numAgents <= 0 || numResourceTypes <= 0 || numTasks <= 0) {
	// System.out.println("Error in inputs!");
	// }
	//
	// // Building the network randomly
	// // adding resources
	// for (int id = 0; id < numResourceTypes; id++) {
	// Resource r = new Resource(id);
	// ResourceSet.add(r);
	// }
	//
	// // adding agents
	// for (int id = 0; id < numAgents; id++) {
	// Agent a = new Agent(id, numResourceTypes);
	// AgentSet.add(a);
	// }
	//
	// // adding tasks and locating them
	// for (int id = 0; id < numTasks; id++) {
	// Task t = new Task(id, numResourceTypes);
	// TaskSet.add(t);
	// }
	// }

	public boolean saveNetwork(String file) {
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(instance);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in:" + file + "\n");
			return true;
		} catch (IOException i) {
			i.printStackTrace();
			return false;
		}
	}

	public static Network loadNetwork(String fileLocation) {
		Network loaded = null;
		try {
			FileInputStream fileIn = new FileInputStream(fileLocation);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			loaded = (Network) in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			System.out.println("Network class not found");
			e.printStackTrace();
			return null;
		}
		if (loaded != null) {
			loaded.print();
			instance = loaded;
			return instance;
		} else
			return null;
	}


	public void draw() {
		 SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                JFrame frame = new JFrame("Door");
	                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	                Graph panel = new Graph();
	                frame.add(panel);
	                frame.pack();
	                frame.setVisible(true);
	            }
	        });
	}

}
