package elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.*;

import problem.Network;

public class Agent {
	int ID;
	int[] ResourceSetAgent;
	float x, y;

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	List<Task> TaskSetAgent;
	List<Agent> Neighbors;

	public Agent(int iD, int _maxNumberResourceTypes) {
		ID = iD;
		Neighbors = new ArrayList<Agent>();
		TaskSetAgent = new ArrayList<Task>();
		ResourceSetAgent = new int[_maxNumberResourceTypes];

		Random r = new Random();
		x = r.nextFloat() * 80 + 5;
		y = r.nextFloat() * 80 + 5;

	}

	public int getResourceSetAgent(int i) {
		return ResourceSetAgent[i];
	}

	public void setResourceSetAgent(int quantity, Resource resource) {
		ResourceSetAgent[resource.getID()] = quantity;
	}

	public List<Task> getTaskSetAgent() {
		return TaskSetAgent;
	}

	public void setTaskSetAgent(Task task) {
		TaskSetAgent.add(task);
	}

	public List<Agent> getNeighbors() {
		return Neighbors;
	}

	public void setNeighbor(Agent neighbor) {
		if (!Neighbors.contains(neighbor)) {
			Neighbors.add(neighbor);
			neighbor.setNeighbor(this);
		}
	}

	public int getID() {
		return ID;
	}

	public boolean isNeighbor(Agent agent) {
		for (Agent a : Neighbors) {
			if (agent == a)
				return true;
		}
		return false;
	}

	public int[] getResourceSetAgent() {
		// TODO Auto-generated method stub
		return this.ResourceSetAgent;
	}

	public void draw(Graphics g) {

		g.setColor(Color.red);
		Font f = new Font("Times New Roman (Headings CS)", Font.BOLD, 18);

		g.setFont(f);
		g.drawString("A" + ID, (int) x * 10 + 10, (int) y * 10 + 10);

		// tasks
		f = new Font("Calibri", Font.BOLD, 14);
		g.setFont(f);
		String taskString = " ";
		for (int i = 0; i < TaskSetAgent.size(); i++) {
			String tmp = "T" + TaskSetAgent.get(i).getID() + " ";
			taskString = taskString + tmp;
		}

		g.drawString(taskString, (int) (x * 10 + 20), (int) (y * 10 + 17));

		// resources
		g.setColor(Color.blue);
		f = new Font("Calibri", Font.BOLD, 14);
		g.setFont(f);
		String resourceString = "";
		for (int i = 0; i < ResourceSetAgent.length; i++)
			if (ResourceSetAgent[i] > 0) {
				String tmp = ResourceSetAgent[i] + "*R" + i + " ";
				resourceString = resourceString + tmp;
			}

		g.drawString(resourceString, (int) (x * 10 + 10) + 10,
				(int) (y * 10 + 27));
	}
}
