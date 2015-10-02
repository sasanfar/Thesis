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
			command = keboard.nextLine();
			command = command.toLowerCase();
			if (command.equals("new")) {
				_network.initializeNetwork();
			}
			else if (command.equals("print"))
				_network.print();
			else if (command.contains("save")){
				String file = command.substring(4, command.length()-1);
				if(_network.saveNetwork(file))
					System.out.println("Saved successfully");
				else 
					System.out.println("Not saved");
			}
			
			
		}
	}
}
