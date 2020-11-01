/**
 * 
 */
package videoGame;

import java.awt.Color;
import java.util.ArrayList;

/**
 * @author Odin
 *
 */
public class Player extends GameElement {
	
	public Weapon weapon;
	public int row, column;
	public int health, maxHealth;
	
	public Player(Maze maze) {
		name = "Player";
		color = Color.green;
		
		weapon = new Weapon(1, 1, false);
		
		maxHealth = 100;
		health = maxHealth;
		
		boolean flag = true;
		while(flag) {
			int r = (int)(Math.random() * maze.rows);
			int c = (int)(Math.random() * maze.columns);
			
			if(!maze.dungeon[r][c].isWall) {
				column = c;
				row = r;
				flag = false;
				maze.dungeon[r][c].holding = this;
			}
		}
	}
	
	public void move(Maze maze, int r, int c) {
		if(r >= 0 && r < maze.rows && c >= 0 && c < maze.columns) {
			//NORMAL MOVEMENT
			if(!maze.dungeon[r][c].isWall && maze.dungeon[r][c].holding == null) {
				maze.dungeon[r][c].holding = this;
				maze.dungeon[row][column].holding = null;
				row = r;
				column = c;
			}
			else if(maze.dungeon[r][c].holding == null) {
				//DIGGING CODE GOES HERE
				maze.dungeon[r][c].isWall = false;
			}
		}
	}
	
	public int attack(Maze maze, int r, int c, ArrayList<Enemy> enemyList) { //r & c range from -1 to 1 and tell which direction to attack
		int pointsEarned = 0;
		for(int x = 1; x <= weapon.range; x++) {
			if(row + r >= 0 && row + r < maze.rows && column + c >= 0 && column + c < maze.columns) {
				if(maze.dungeon[row + (r * x)][column + (c * x)].holding != null && maze.dungeon[row + (r * x)][column + (c * x)].holding.name.equals("Enemy")) {
					for(Enemy e : enemyList) {
						if(e.row == row + (r * x) && e.column == column + (c * x)) {
							if(e.health - weapon.damage <= 0)
								pointsEarned += e.pointsWorth;
							
							e.health -= weapon.damage;
							if(!e.aggro) {
								e.color = Color.black;
							}
							else {
								e.color = new Color((int)(255.0 * e.health / e.maxHealth), 0, 0);
							}
							if(!weapon.piercing)
								return pointsEarned;
						}
					}
				}
			}
		}
		return pointsEarned;
	}
}
