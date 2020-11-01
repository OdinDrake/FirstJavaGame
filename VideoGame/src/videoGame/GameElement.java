/**
 * 
 */
package videoGame;

import java.awt.Color;

/**
 * @author Odin
 *
 */
public abstract class GameElement {
	
	public String name;
	public Color color;
	public int health;
	
	public GameElement() {
		name = "";
		color = null;
	}
}
