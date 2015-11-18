package problem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.media.j3d.Canvas3D;
import javax.swing.JComponent;
import javax.swing.JPanel;

import user_interface.Interface;
import edu.uci.ics.jung.*;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import elements.*;

public class Graph extends JPanel {

	public Graph() {
		super.setLayout(new BorderLayout());

		final JPanel drawingPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				for (Agent a : Interface.network.AgentSet) 
					a.draw(g);
				
				for (Link l : Interface.network.LinkSet)
					l.draw(g);
				
			}
		};
		drawingPanel.setBackground(Color.yellow);
		add(drawingPanel);
		JPanel holder = new JPanel();
		super.add(holder, BorderLayout.SOUTH);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(900, 800);
	}

	private void repaintText() {
		// currentStateOfDoor.setText(door.getMessage());
		// These methods are from Door class.
	}
}
