package user_interface;

import java.awt.RenderingHints.Key;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import problem.*;
import elements.*;

public class Interface {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		Scanner keboard = new Scanner(System.in);
		boolean execute = true;
		Network _network = Network.getInstance();
		String command;
		while (execute) {
			System.out.println("What is your command? \"New\" for new network, \"Print\" to print the network, \"Save\" to save current network, \"Load\" for loading a network.");
			command = keboard.next();
			command = command.toLowerCase();
			if (command.equals("new")) {
				_network.initializeNetwork();
			}
			else if (command.equals("print"))
				_network.print();
			else if (command.equals("save")){
				String file = keboard.next();
				if(_network.saveNetwork(file))
					System.out.println("Saved successfully");
				else 
					System.out.println("Could not save...");
			}
			else if (command.equals("load")){
				String file = keboard.next();
				if (_network.loadNetwork(file))
					System.out.println("Loaded successfully!");
				else 
					System.out.println("Could not load...");
			}
			
		}
	}
}
