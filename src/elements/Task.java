package elements;

import java.util.Arrays;

public class Task implements Comparable<Task>{

	int ID;
	int[] requiredResources; // array of required quantity of each resource
								// for the task
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

	public boolean equals(Task task) {
		if (this.ID == task.ID)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "Task [ID=" + ID + ", requiredResources="
				+ Arrays.toString(requiredResources) + ", utility=" + utility
				+ ", location=" + location + "]";
	}
	
	@Override
	public int compareTo(Task t){
		if (this.utility< t.utility)
			return -1;
		else if (this.utility>t.utility)
			return 1;
		else 
			return 0;
	}
}
