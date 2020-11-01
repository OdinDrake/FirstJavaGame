/**
 * 
 */
package videoGame;

import java.awt.Color;

/**
 * @author Odin
 *
 */
public class Enemy extends GameElement {
	
	public int speed, damage, health, maxHealth, aggroRange;
	public int row, column;
	public boolean aggro, canDig, canMoveDiag;
	public int pointsWorth;
	
	public Enemy(Maze maze, int s, int d, int h, boolean dig, boolean diagonal) {
		name = "Enemy";
		
		canDig = dig;
		canMoveDiag = diagonal;
		
		health = h;
		maxHealth = health;
		
		speed = s;
		aggroRange = speed * 5;
		
		if(speed == 1) {
			health = 1;
			maxHealth = 1;
		}
		
		
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
		
		damage = d;
		aggro = false;
		
		color = Color.black;
		
		
		pointsWorth = damage + health;
		
		if(canDig)
			pointsWorth += 2;
		if(canMoveDiag)
			pointsWorth += 5;
		if(speed == 1)
			pointsWorth += 5;
		
	}
	
	//Enemies will only be visible and moving if they are within a certain range of the player
	public void AIMove(Player p, Maze maze) {
		
		aggro = distanceToPlayer(p, row, column) < aggroRange;
		int closestRow = row, closestCol = column;
		
		if(aggro) {
			for(int r = -1; r < 2; r++) {
				for(int c = -1; c < 2; c++) {
					if(canMoveDiag || (r != 0 && c == 0) || (r == 0 && c != 0)) {
						if(distanceToPlayer(p, row + r, column + c) < distanceToPlayer(p, closestRow, closestCol)) {
							closestRow = row + r;
							closestCol = column + c;
						
						}
					}
				}
			}
		}
		
		if(!maze.dungeon[closestRow][closestCol].isWall) {
			if(maze.dungeon[closestRow][closestCol].holding == p) {
				p.health -= damage;
			}
			else if(maze.dungeon[closestRow][closestCol].holding == null){
				maze.dungeon[closestRow][closestCol].holding = this;
				maze.dungeon[row][column].holding = null;
				row = closestRow;
				column = closestCol;
			}
		}
		else {
			maze.dungeon[closestRow][closestCol].isWall = false;
		}
		
		//When not aggressive, enemies look the same as walls
		if(!aggro) {
			color = Color.black;
		}
		else {
			color = new Color((int)(255.0 * health / maxHealth), 0, 0);
		}
	}
	
	public double distanceToPlayer(Player p, int r, int c) {
		return Math.sqrt(Math.pow(r - p.row, 2) + Math.pow(c - p.column, 2));
	}
}
