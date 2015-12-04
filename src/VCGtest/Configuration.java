package VCGtest;

import elements.Resource;
import user_interface.Interface;

public class Configuration {
	static int nextID = 0;
	Resource resource;
	int[] allocation = new int[Interface.network.AgentSet.size()];
	int ID;

	public int getID() {
		return ID;
	}

	public Configuration(Resource resource) {
		ID = nextID++;
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public int[] getAllocation() {
		return allocation;
	}

}
