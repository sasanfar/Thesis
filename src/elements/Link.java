package elements;

import java.awt.Color;
import java.awt.Graphics;

public class Link {
	int ID;
	Agent firstNode;
	Agent secondNode;

	public Link(int id, Agent _firstNode, Agent _secondNode) {
		this.ID = id;
		this.firstNode = _firstNode;
		this.secondNode = _secondNode;
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

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.drawLine((int) firstNode.getX() * 10 + 10,
				(int) firstNode.getY() * 10 + 10,
				(int) secondNode.getX() * 10 + 10,
				(int) secondNode.getY() * 10 + 10);
	}

}
