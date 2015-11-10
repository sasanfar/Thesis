package user_interface;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import elements.Agent;
import elements.Resource;
import elements.Task;
import problem.*;

public class Interface {
	public static Network network = Network.getInstance();

	public static void main(String[] args) throws FileNotFoundException,
			UnsupportedEncodingException {
		Scanner keyboard = new Scanner(System.in);
		boolean execute = true;
		while (!network.initializeNetwork(80, 50, 20, 10, 5))
			;
		// network.initializeNetwork(80, 50, 20, 10, 5);
		// mapping(network);
		String command;
		while (execute) {
			System.out
					.println("\"N\" for network related material, \"S\" to solve the problem, \"E\" to exit the program:");
			command = keyboard.nextLine();
			command = command.toLowerCase();
			switch (command) {
			case "n":
				networkInterface(network);
				break;

			case "s":
				solveInterface(network);
				break;

			case "e":
				execute = false;
				keyboard.close();
				break;

			default:
				System.out.println("Wrong input!");
				break;
			}
		}
	}

	public static void networkInterface(Network network)
			throws FileNotFoundException, UnsupportedEncodingException {
		Scanner keyboard = new Scanner(System.in);
		String command;
		System.out
				.println("\"N\" for new network, \"P\" to print the network, \"S\" to save current network, \"L\" for loading a network:");
		command = keyboard.nextLine();
		command = command.toLowerCase();
		switch (command) {
		case "n": {
			System.out
					.println("Put in number of agents, number of tasks, number of resource types, max of each type per agent, max of each type per task:");
			int _numAgents, _numTasks, _numResourceTypes, _maxEachTypePerAgent, _maxEachTypePerTask;
			_numAgents = keyboard.nextInt();
			_numTasks = keyboard.nextInt();
			_numResourceTypes = keyboard.nextInt();
			_maxEachTypePerAgent = keyboard.nextInt();
			_maxEachTypePerTask = keyboard.nextInt();
			network = Network.newNetwork(_numAgents, _numTasks,
					_numResourceTypes, _maxEachTypePerAgent,
					_maxEachTypePerTask);

			// mapping(network);
		}
			break;
		case "p":
			network.print();
			break;

		case "s": {
			System.out.println("Put in the file directory:");
			String file = keyboard.nextLine();
			if (network.saveNetwork(file))
				System.out.println("Saved successfully...");
			else
				System.out.println("Not saved...");
		}
			break;

		case "l": {
			System.out.println("Put in the file directory:");
			String file = keyboard.nextLine();
			if (Network.loadNetwork(file) != null) {
				network = Network.getInstance();
				System.out.println("Loaded successfully...");
			// mapping(network);
			} else {
				System.out
						.println("Not loaded, using default values for a new network...");
				network = Network.getInstance();
				// network.initializeNetwork(100, 50, 20, 10, 5);
			// mapping(network);
			 }
		}
			break;

		default:
			System.out.println("Wrong input!");
			break;
		}
		// keyboard.close();
	}

	public static void solveInterface(Network network) {
		mapping(network);
		Scanner keyboard = new Scanner(System.in);
		System.out
				.println("\"B\" to solve using barter mechanism, \"V\" to solve using VCG mechanism:");
		String command;
		command = keyboard.nextLine();
		command = command.toLowerCase();
		switch (command) {
		case "b":
			ColumnGenBarter columnGenBarter = new ColumnGenBarter();
			columnGenBarter.runColumnGeneration();
			break;

		case "v":
			ColumnGenVCG columnGenVCG = new ColumnGenVCG();
			columnGenVCG.runColumnGeneration();
			break;

		default:
			System.out.println("Wrong input!");
			keyboard.close();
			break;
		}
	}

	public static void mapping(Network network) {
		for (Resource r : network.ResourceSet) {
			ColumnGenBarter.all_resources.put(r.getID(), r);
			ColumnGenVCG.all_resources.put(r.getID(), r);
		}
		for (Task t : network.TaskSet) {
			ColumnGenBarter.all_tasks.put(t.getID(), t);
			ColumnGenVCG.all_tasks.put(t.getID(), t);
		}
		for (Agent a : network.AgentSet) {
			ColumnGenBarter.all_agents.put(a.getID(), a);
			ColumnGenVCG.all_agents.put(a.getID(), a);
		}
	}
}
