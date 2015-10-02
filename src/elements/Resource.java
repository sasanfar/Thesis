package elements;

public class Resource {
	int ID;
	//int quantity;
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
}
