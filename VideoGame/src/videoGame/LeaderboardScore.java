/**
 * 
 */
package videoGame;

/**
 * @author Odin
 *
 */
public class LeaderboardScore {
	public String name;
	public int score, levelsCleared;
	
	public LeaderboardScore(String n, int s, int l) {
		name = n;
		score = s;
		levelsCleared = l;
	}
	
	public String toString() {
		return name + "   " + score + "   " + levelsCleared;
	}
}
