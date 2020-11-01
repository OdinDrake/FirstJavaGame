/**
 * 
 */
package videoGame;

/**
 * @author drakeot
 *
 */
public class MazePiece {

	public boolean isChangeable;
	public boolean isWall;
	public GameElement holding; //stores what is currently on this space
	public boolean isExit;
	
	public MazePiece() {
		isWall = true;
		isChangeable = true;
		holding = null;
		isExit = false;
	}
	
	public void copy(MazePiece m) {
		isWall = m.isWall;
		isChangeable = m.isChangeable;
		holding = m.holding;
	}
}
