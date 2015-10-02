package elements;

public class Link {
	int ID;
	Agent firstNode;
	Agent secondNode;

	public Link(int id, Agent _firstNode, Agent _secondNode){
		this.ID=id;
		this.firstNode=_firstNode;
		this.secondNode=_secondNode;
	}
	
	public int getID() {
		return ID;
	}

	public Agent getFirstNode() {
		return firstNode;
	}
	public Agent getSecondNode() {
		return secondNode;
	}

}
