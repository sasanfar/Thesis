package problem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import user_interface.Interface;
import elements.*;

public class Graph extends JPanel {

	public Graph() {
		super.setLayout(new BorderLayout());

		final JPanel drawingPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				for (Link l : Interface.network.LinkSet)
					l.draw(g);
				
				
				for (Agent a : Interface.network.AgentSet) 
					a.draw(g);
				
			}
		};
		
		drawingPanel.setBackground(Color.white);
		add(drawingPanel);
		JPanel holder = new JPanel();
		super.add(holder, BorderLayout.SOUTH);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(1200, 800);
	}

}
