package user_interface;

import ilog.concert.IloException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import newBarter.CG_Barter;
import newVCG.CG_VCG;
import Barter.ColumnGenBarter;
import VCG.ColumnGenVCG;
import problem.*;

public class Interface {
	public static Network network = Network.getInstance();
	public static int _numAgents = 20, _numTasks = 15, _numResourceTypes = 10, _maxEachTypePerAgent = 5,
			_maxEachTypePerTask = 5;

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IloException {
		Scanner keyboard = new Scanner(System.in);
		boolean execute = true;
		while (!network.initializeNetwork(_numAgents, _numTasks, _numResourceTypes, _maxEachTypePerAgent,
				_maxEachTypePerTask))
			;
		String command;
		while (execute) {
			System.out.println(
					"\"N\" for network related material, \"S\" to solve the problem, \"E\" to exit the program:");
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

	public static void networkInterface(Network network) throws FileNotFoundException, UnsupportedEncodingException {
		Scanner keyboard = new Scanner(System.in);
		String command;
		System.out.println(
				"\"N\" for new network, \"D\" to draw the network, \"P\" to print the network, \"S\" to save current network, \"L\" for loading a network:");
		command = keyboard.nextLine();
		command = command.toLowerCase();
		switch (command) {
		case "n": {
			System.out.println(
					"Put in number of agents, number of tasks, number of resource types, max of each type per agent, max of each type per task:");

			_numAgents = keyboard.nextInt();
			_numTasks = keyboard.nextInt();
			_numResourceTypes = keyboard.nextInt();
			_maxEachTypePerAgent = keyboard.nextInt();
			_maxEachTypePerTask = keyboard.nextInt();
			network = Network.newNetwork(_numAgents, _numTasks, _numResourceTypes, _maxEachTypePerAgent,
					_maxEachTypePerTask);

		}
			break;
		case "p":
			network.print();
			break;

		case "d":
			network.draw();
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
				System.out.println("Not loaded, using default values for a new network...");
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

	public static void solveInterface(Network network) throws IloException {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("\"B\" to solve using barter mechanism, \"V\" to solve using VCG mechanism:");
		String command;
		command = keyboard.nextLine();
		command = command.toLowerCase();
		switch (command) {
		case "b":
			CG_Barter barter = new CG_Barter();
			barter.runBarter();
			break;

		case "v":
			CG_VCG cg = new CG_VCG();
			cg.runVCG();
			break;

		default:
			System.out.println("Wrong input!");
			keyboard.close();
			break;
		}
	}
}
