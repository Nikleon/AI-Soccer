package not.my.code;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Main extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		JFrame frame = new JFrame("AI Soccer");
		frame.setSize(1200, 800);
		frame.setUndecorated(true);
		frame.setLocation(0, 0);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Main());
		frame.setVisible(true);
	}

	Timer t;
	GameState gameState;

	public Main() {
		gameState = new GameState();
		t = new Timer(1000 / 50, this);
		t.start();
		new GameListener(this);
	}

	public void paintComponent(Graphics g) {
		gameState.draw(g, getWidth(), getHeight());
	}

	public void actionPerformed(ActionEvent e) {
		if (GameListener.keyboard[32]) {
			gameState.update();
			gameState.update();
			gameState.update();
		}
		gameState.update();
		repaint();
	}
}
