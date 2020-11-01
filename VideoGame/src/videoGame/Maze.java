/**
 * 
 */
package videoGame;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @author drakeot
 *
 */
public class Maze implements Constants {
	
	public MazePiece[][] dungeon;
	public int area;
	public int columns;
	public int rows;
	
	public Maze(int row, int col) {
		dungeon = new MazePiece[row][col];
		area = row * col;
		rows = row;
		columns = col;
		
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				dungeon[r][c] = new MazePiece();
			}
		}
	}
	
	public void whiteNoiseMaze(double percentStartWall, int numLoops, int deathLim, int birthLim) {
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				dungeon[r][c].isWall = Math.random() < percentStartWall;
			}
		}
		
		for(int x = 0; x < numLoops; x++) {
			advanceGeneration(deathLim, birthLim);
		}

		boolean flag = true;
		while(flag) {
			int r = (int)(Math.random() * rows);
			int c = (int)(Math.random() * columns);
			
			if(!dungeon[r][c].isWall) {
				flag = false;
				dungeon[r][c].isExit = true;
			}
		}
	}
	
	public void advanceGeneration(int deathLimDown, int birthLim) {
		//Copy the maze into a temp that we will edit
		MazePiece[][] temp = new MazePiece[dungeon.length][dungeon[0].length];
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				temp[r][c] = new MazePiece();
				temp[r][c].copy(dungeon[r][c]);
			}
		}
		
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				if(dungeon[r][c].isWall) {
					if(numNeighbors(r, c) <= deathLimDown) {
						temp[r][c].isWall = false;
					}
				}
				else {
					if(numNeighbors(r, c) >= birthLim)
						temp[r][c].isWall = true;
				}
			}
		}
		dungeon = temp;
	}
	
	//returns number of walls in 3x3 around block
	public int numNeighbors(int row, int col) {
		int count = 0;
		
		for(int r = -1; r < 2; r++) {
			for(int c = -1; c < 2; c++) {
				if(row + r >= 0 && row + r < rows && col + c >= 0 && col + c < columns) {
					if(dungeon[row + r][col + c].isWall && (r != 0 || c != 0))
						count++;
				}
				else {
					count++;
				}
			}
		}
		
		return count;
	}
	
	//unfinished part of secondary maze generation algorithm
//	public void generateRooms(int maxRooms, int entrancesPerRoom) {
//		for(int x = 0; x < maxRooms; x++) {
//			int width, height, startRow, startCol;
//			width = (int)(Math.random() * (columns * .1) + 6);
//			height = (int)(Math.random() * (Math.pow(area, .25) / width) + 6);
//			
//			startRow = (int)(Math.random() * (rows - height));
//			startCol = (int)(Math.random() * (columns - width));
//			
//			//Makes sure the room will be placed in a valid spot, if impossible, it just won't
//			int count = 0;
//			while(!isRectValid(width, height, startRow, startCol) && count < 10001) {
//				startRow = (int)(Math.random() * (rows - height));
//				startCol = (int)(Math.random() * (columns - width));
//				count++;
//			}
//			
//			if(count < 10000) {
//				int entrances = 0;
//				for(int r = startRow; r <= startRow + height; r++) {
//					for(int c = startCol; c <= startCol + width; c++) {
//						if(r == startRow || r == startRow + height || c == startCol || c == startCol + width) {
//							if(!isCorner(r, c, width, height, startRow, startCol) && entrances < entrancesPerRoom && Math.random() < .2)
//								entrances++;
//							else {
//								//START WORKING HERE!!!!
//							}
//						}
//					}
//				}
//			}
//		}
//	}
	
	public void draw(Graphics g, Player p) {
		int blockWidth = (WIDE - (MARGIN * 2)) / columns;
		int blockHeight = (HIGH - (MARGIN * 2)) / rows;
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				if(distanceToPlayer(p, r, c) > visionRange(p)) {
					g.setColor(Color.black);
				}
				else if(dungeon[r][c].isWall) {
					g.setColor(Color.black);
				}
				else if(dungeon[r][c].isExit) {
					g.setColor(Color.yellow);
				}
				else if(dungeon[r][c].holding == null) {
					g.setColor(Color.gray);
				}
				else {
					g.setColor(dungeon[r][c].holding.color);
				}
				
				
				g.fillRect(MARGIN + (blockWidth * c), MARGIN + (blockHeight * r), blockWidth, blockHeight);
			}
		}
	}
	
	public int visionRange(Player p) {
		int minDimension = Math.min(rows, columns);
		return (int)(minDimension * ((p.health * .5) / p.maxHealth) * ((p.health * .5) / p.maxHealth)) + 5;
	}
	
	public double distanceToPlayer(Player p, int r, int c) {
		return Math.sqrt(Math.pow(r - p.row, 2) + Math.pow(c - p.column, 2));
	}
	
	private boolean isCorner(int row, int col, int width, int height, int startRow, int startCol) {
		if(row == startRow && col == startCol)
			return true;
		if(row == startRow + height && col == startCol)
			return true;
		if(row == startRow && col == startCol + width)
			return true;
		if(row == startRow + height && col == startCol + width)
			return true;
		
		return false;
	}
	
	private boolean isRectValid(int width, int height, int startRow, int startCol) {
		if(startRow + height >= dungeon[0].length || startCol + width >= dungeon.length)
			return false;
		
		for(int r = startRow; r <= startRow + width; r++) {
			for(int c = startCol; c <= startCol + height; c++) {
				if(!dungeon[r][c].isChangeable)
					return false;
			}
		}
		return true;
	}
	
	public void resetHolding() {
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < columns; c++) {
				dungeon[r][c].holding = null;
			}
		}
	}
}
