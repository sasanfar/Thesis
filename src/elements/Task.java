package elements;

import java.awt.Color;
import java.awt.Graphics;

public class Task {

	int ID;
	int[] requiredResources; // array of quantity and type of each resource
								// in that task
	int utility;
	Agent location;

	public Task(int iD, int _maxResourceTypes) {
		super();
		ID = iD;
		utility = 0;
		location = null;
		requiredResources = new int[_maxResourceTypes];
	}

	public int getUtility() {
		return utility;
	}

	public void setUtility(int utility) {
		this.utility = utility;
	}

	public int getRequiredResources(int i) {
		return requiredResources[i];
	}

	public void setRequiredResources(int requiredResources, Resource resource) {
		this.requiredResources[resource.getID()] = requiredResources;
	}

	public int getID() {
		return ID;
	}

	public Agent getLocation() {
		return location;
	}

	public void setLocation(Agent location) {
		this.location = location;
	}
	public boolean equals(Task task){
		if(this.ID == task.ID)
			return true;
		return false;
	}
}
