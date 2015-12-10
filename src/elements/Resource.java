package elements;

public class Resource {
	@Override
	public String toString() {
		return "Resource [ID=" + ID + "]";
	}
	int ID;
	Agent location;

	public Agent getLocation() {
		return location;
	}

	public void setLocation(Agent location) {
		this.location = location;
	}

	public int getID() {
		return ID;
	}

	public Resource(int iD) {
		ID = iD;
		location=null;
		//quantity = 0;		
	}
	public boolean equals(Resource resource){
		if(this.ID == resource.ID)
			return true;
		return false;
	}
}
