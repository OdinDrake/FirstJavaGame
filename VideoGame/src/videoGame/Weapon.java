/**
 * 
 */
package videoGame;

/**
 * @author Odin
 *
 */
public class Weapon extends GameElement {

	public int damage;
	public int range;
	public boolean piercing;
	
	public Weapon(int d, int r, boolean p) {
		damage = d;
		range = r;
		piercing = p;
	}
}
