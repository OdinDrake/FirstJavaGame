package videoGame;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * @author drakeot
 *
 */
public interface Constants {

	public static final int WIDE = 1000;
	public static final int HIGH = 800;
	public static final int MARGIN = 50;
	
	public static enum State {INTRO, PLAY, EXIT, GAMEOVER, LEADERBOARD};
	
	//Fonts:
	public static final Font VERY_BIG_FONT = new Font("Helvetica", Font.BOLD, 100);
	public static final Font BIG_FONT = new Font("Helvetica", Font.BOLD, 60);
	public static final Font SMALL_FONT = new Font("Helvetica", Font.PLAIN, 20);
	public static final Font MEDIUM_FONT = new Font("Helvetica", Font.BOLD, 40);
	
}
