package videoGame;

import java.awt.Color; 
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics; 
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.awt.event.KeyEvent; 
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame; 
import javax.swing.JPanel; 
import javax.swing.Timer; 
 
 
public class Visual implements ActionListener, KeyListener, MouseListener, MouseMotionListener, Constants {
 
    private JFrame frame;       //REQUIRED! The outside shell of the window
    public DrawingPanel panel;  //REQUIRED! The interior window
    private Timer visualtime;   //REQUIRED! Runs/Refreshes the screen. 
    
    //any other variables needed, go here.
    State gameState;
    public int choosePlay; //0 = Play, 1 = exit, 3 = leaderboard
    public Maze maze;
    public Player p1;
    public ArrayList<Enemy> enemyList;
    public int ticks;
    public int playerPoints;
    public int levelsCleared;
    public Scanner fin;
    public ArrayList<LeaderboardScore> topTenScores;	//used for leaderboard
    public static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public int leadL1, leadL2, leadL3;	//The index of the 3 letters of the leaderboard name in the alphabet string
    public int leadLetter;				//Tells which of the 3 letters is currently being edited
    public String leadName;				//The leaderboard name
    public boolean onLeaderboard;
    
    public Visual() throws IOException
    {
        frame = new JFrame("Dungeon Crawler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new DrawingPanel();
        panel.setPreferredSize(new Dimension(WIDE, HIGH)); //width, height in #pixels.
        frame.getContentPane().add(panel);
        panel.setFocusable(true);
        panel.requestFocus();
        panel.addKeyListener(this);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); 
 
        //Initialize all global variables here:  
        Initialize();
 
        visualtime = new Timer(20, this);     
        visualtime.start();
    } 
 
    public void Initialize() throws IOException
    {
    	gameState = State.INTRO;
    	choosePlay = 0;
    	maze = new Maze(100, 100);
    	maze.whiteNoiseMaze(.4, 3, 3, 4);
    	p1 = new Player(maze);
    	enemyList = new ArrayList<Enemy>();
    	enemyInit();
    	ticks = 0;
    	playerPoints = 0;
    	levelsCleared = 0;
    	onLeaderboard = false;

    	leadL1 = 0;
    	leadL2 = 0;
    	leadL3 = 0;
    	leadLetter = 0;
    	leadName = "";
    	
    	topTenScores = new ArrayList<LeaderboardScore>();
    	FileReader input = new FileReader("Leaderboard.txt");
    	fin = new Scanner(input);
    	int count = 0;
    	
    	while(fin.hasNext() && count < 10) {
    		topTenScores.add(new LeaderboardScore(fin.next(), fin.nextInt(), fin.nextInt()));
    		count++;
    	}
    	fin.close();
    }
    
    public void enemyInit() {
    	enemyList = new ArrayList<Enemy>();
    	
    	//Fills 1% of tiles with enemies and randomly determines their stats
    	for(int x = 0; x < maze.area * .01; x++) {
    		boolean canDig = false, canMoveDiagonal = false;
    		if(Math.random() < .3)
    			canDig = true;
    		if(Math.random() < .1)
    			canMoveDiagonal = true;
    			
    		enemyList.add(new Enemy(maze, (int)(Math.random() * 3 + 1), (int)(Math.random() * 6 + 1), (int)(Math.random() * 6 + 1), canDig, canMoveDiagonal));
    	}
    }
   
    public void actionPerformed(ActionEvent e)
    {    
    	
    	
        panel.repaint();
    }
 
    //Called whenever the player moves to a new floor
    public void reinitializeFloor() {
    	maze.resetHolding();
		maze.whiteNoiseMaze(.4, 5, 3, 4);
		
		p1.health += 10;
		if(p1.health > p1.maxHealth)
			p1.health = p1.maxHealth;
		
		boolean flag = true;
		while(flag) {
			int r = (int)(Math.random() * maze.rows);
			int c = (int)(Math.random() * maze.columns);
			
			if(!maze.dungeon[r][c].isWall) {
				p1.column = c;
				p1.row = r;
				flag = false;
				maze.dungeon[r][c].holding = p1;
			}
		}
		enemyInit();
    }
    
    public void gameUpdate(int row, int col, boolean attackOrMove) {	//attackOrMove: true if attack, false if move
    	int exitR = -1, exitC = -1;
    	for(int r = 0; r < maze.rows; r++) {
    		for(int c = 0; c < maze.columns; c++) {
    			if(maze.dungeon[r][c].isExit) {
    				exitR = r;
    				exitC = c;
    			}
    		}
    	}
    	
    	if(attackOrMove)
    		playerPoints += p1.attack(maze, row, col, enemyList);
    	else {
    		p1.move(maze, p1.row + row, p1.column + col);
    		
    		//Reload the room if you're on the exit
    		if(p1.row == exitR && p1.column == exitC) {
    			reinitializeFloor();
        		
        		levelsCleared++;
        		playerPoints += Math.pow(1.5, levelsCleared);
        		
    			return;
    		}
    	}
    	//If enemies have no health, remove them. Otherwise, check if they should move this turn
    	for(int x = 0; x < enemyList.size(); x++) {
    		if(enemyList.get(x).health <= 0) {
    			maze.dungeon[enemyList.get(x).row][enemyList.get(x).column].holding = null;
    			enemyList.remove(x);
    			x--;
    		}
    		else if(ticks % enemyList.get(x).speed == 0)
    			enemyList.get(x).AIMove(p1, maze);
    	}
    	
		ticks++;
		
		if(p1.health <= 0) {
			gameState = State.GAMEOVER;
		}
    }
    
    public void keyPressed(KeyEvent e)
    {  
    	if(e.getKeyCode() == KeyEvent.VK_1) {
    		p1.health = 0;
    	}
    	if(e.getKeyCode() == KeyEvent.VK_2) {
    		playerPoints += 1;
    	}
    	if(e.getKeyCode() == KeyEvent.VK_3) {
    		printLeaderboard();
    	}
    	if(e.getKeyCode() == KeyEvent.VK_W && gameState == State.PLAY)
        {
    		gameUpdate(-1, 0, false);
        }
    	if(e.getKeyCode() == KeyEvent.VK_A && gameState == State.PLAY)
        {
    		gameUpdate(0, -1, false);
        }
    	if(e.getKeyCode() == KeyEvent.VK_S && gameState == State.PLAY)
        {
    		gameUpdate(1, 0, false);
        }
    	if(e.getKeyCode() == KeyEvent.VK_D && gameState == State.PLAY)
        {
    		gameUpdate(0, 1, false);
        }
    	if(e.getKeyCode() == KeyEvent.VK_UP && gameState == State.PLAY)
        {
    		gameUpdate(-1, 0, true);
        }
    	if(e.getKeyCode() == KeyEvent.VK_LEFT && gameState == State.PLAY)
        {
    		gameUpdate(0, -1, true);
        }
    	if(e.getKeyCode() == KeyEvent.VK_DOWN && gameState == State.PLAY)
        {
    		gameUpdate(1, 0, true);
        }
    	if(e.getKeyCode() == KeyEvent.VK_RIGHT && gameState == State.PLAY)
        {
    		gameUpdate(0, 1, true);
        }
    	if(e.getKeyCode() == KeyEvent.VK_RIGHT && gameState == State.INTRO)
        {
    		choosePlay++;
    		if(choosePlay > 2)
    			choosePlay = 0;
        }
    	if(e.getKeyCode() == KeyEvent.VK_LEFT && gameState == State.INTRO)
    	{
    		choosePlay--;
    		if(choosePlay < 0)
    			choosePlay = 2;
    	}
    	if(e.getKeyCode() == KeyEvent.VK_RIGHT && gameState == State.GAMEOVER)
        {
    		leadLetter++;
    		if(leadLetter > 2)
    			leadLetter = 0;
        }
    	if(e.getKeyCode() == KeyEvent.VK_LEFT && gameState == State.GAMEOVER)
    	{
    		leadLetter--;
    		if(leadLetter < 0)
    			leadLetter = 2;
    	}
    	if(e.getKeyCode() == KeyEvent.VK_UP && gameState == State.GAMEOVER)
    	{
    		if(leadLetter == 0) {
    			leadL1--;
    			if(leadL1 < 0)
    				leadL1 = alphabet.length() - 1;
    		}
    		else if(leadLetter == 1) {
    			leadL2--;
    			if(leadL2 < 0)
    				leadL2 = alphabet.length() - 1;
    		}
    		else if(leadLetter == 2) {
    			leadL3--;
    			if(leadL3 < 0)
    				leadL3 = alphabet.length() - 1;
    		}
    	}
    	if(e.getKeyCode() == KeyEvent.VK_DOWN && gameState == State.GAMEOVER)
    	{
    		if(leadLetter == 0) {
    			leadL1++;
    			if(leadL1 > alphabet.length() - 1)
    				leadL1 = 0;
    		}
    		else if(leadLetter == 1) {
    			leadL2++;
    			if(leadL2 > alphabet.length() - 1)
    				leadL2 = 0;
    		}
    		else if(leadLetter == 2) {
    			leadL3++;
    			if(leadL3 > alphabet.length() - 1)
    				leadL3 = 0;
    		}
    	}
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
        {
        	if(gameState == State.INTRO) {
        		if(choosePlay == 0) {
        			gameState = State.PLAY;
        			p1 = new Player(maze);
        			playerPoints = 0;
        			levelsCleared = 0;
        			reinitializeFloor();
        		}
        		else if(choosePlay == 1)
        			System.exit(0);
        		else
        			gameState = State.LEADERBOARD;
        	}
        	if(gameState == State.GAMEOVER) {
        		boolean flag = true;
        		for(int x = 0; x < topTenScores.size(); x++) {
        			if(flag && playerPoints > topTenScores.get(x).score) {
        				topTenScores.add(x, new LeaderboardScore(leadName, playerPoints, levelsCleared));
        				flag = false;
        			}
        		}
        		
        		FileWriter output = null;
				try {
					output = new FileWriter("Leaderboard.txt");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				BufferedWriter fout = new BufferedWriter(output);
				for(int x = 0; x < topTenScores.size(); x++) {
					LeaderboardScore s = topTenScores.get(x);
					try {
						fout.write(s.name + " " + s.score + " " + s.levelsCleared);
						fout.newLine();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				try {
					fout.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
        		gameState = State.INTRO;
        	}
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
			if(gameState == State.LEADERBOARD) {
				gameState = State.INTRO;
				
			}
        }  
        if(e.getKeyCode() == KeyEvent.VK_HOME)
        {
				try {
					Initialize();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        }  
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0); 
    }

    //If you do anything with the mouse, these will probably be used.
    public void mouseClicked(MouseEvent e) {
    }	
    public void mouseMoved(MouseEvent e) {
    }	
    
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

//BIG NOTE:  The coordinate system for the output screen is as follows:     
//  (x,y) = (0,0) is the TOP LEFT corner of the output screen;    
//  (x,y) = (800,0) is the TOP RIGHT corner of the output screen;     
//  (x,y) = (0,500) is the BOTTOM LEFT corner of the screen;   
//  (x,y) = (800,500) is the BOTTOM RIGHT corner of the screen;
//Strings are refenced from the Bottom Left Corner of their Drawing "Box"
//ALL OTHER OBJECTS are referenced from the TOP Left Corner of their Drawing "Box"

    private class DrawingPanel extends JPanel { 

        public void paintComponent(Graphics g)         
        {
            super.paintComponent(g);
        
            if(gameState == State.INTRO)
            	drawIntro(g);
            else if(gameState == State.PLAY)
            	drawPlay(g);
            else if(gameState == State.LEADERBOARD)
            	drawLeaderboard(g);
            else if(gameState == State.GAMEOVER)
            	drawGameOver(g);
            else if(gameState == State.EXIT)
            	drawExit(g);
        }
        
        public void drawIntro(Graphics g) {
        	onLeaderboard = false;
        	panel.setBackground(Color.green);
        	g.setFont(VERY_BIG_FONT);
        	g.setColor(Color.black);
        	
        	FontMetrics fontvals = g.getFontMetrics();
        	int adjust = fontvals.stringWidth("Dungeon Crawler");
        	g.drawString("Dungeon Crawler", WIDE / 2 - adjust / 2, 150);
        	
        	
        	g.setFont(BIG_FONT);
        	g.setColor(Color.black);
        	fontvals = g.getFontMetrics();
        	
        	g.drawString("PLAY", 20, 400);
        	g.drawString("EXIT", 260, 400);
        	g.drawString("LEADERBOARD", 510, 400);
        	
        	g.setColor(Color.blue);
        	if(choosePlay == 0)
        		g.drawString("PLAY", 20, 400);
        	else if(choosePlay == 1)
        		g.drawString("EXIT", 260, 400);
        	else
        		g.drawString("LEADERBOARD", 510, 400);
        	
        	//Print controls:
        	g.setFont(MEDIUM_FONT);
        	g.setColor(Color.black);
        	fontvals = g.getFontMetrics();
        	adjust = fontvals.stringWidth("_____________________________________________");
        	g.drawString("_____________________________________________", WIDE / 2 - adjust / 2, 450);
        	
        	adjust = fontvals.stringWidth("Movement: WASD");
        	g.drawString("Movement: WASD", WIDE / 2 - adjust / 2, 510);
        	adjust = fontvals.stringWidth("Attack: Arrow Keys");
        	g.drawString("Attack: Arrow Keys", WIDE / 2 - adjust / 2, 510 + MEDIUM_FONT.getSize());
        	adjust = fontvals.stringWidth("Find the yellow exit");
        	g.drawString("Find the yellow exit", WIDE / 2 - adjust / 2, 510 + MEDIUM_FONT.getSize() * 3);
        	adjust = fontvals.stringWidth("Your vision is tied to your health, so be careful!");
        	g.drawString("Your vision is tied to your health, so be careful!", WIDE / 2 - adjust / 2, 510 + MEDIUM_FONT.getSize() * 4);
        	adjust = fontvals.stringWidth("Put your score on the leaderboard!");
        	g.drawString("Put your score on the leaderboard!", WIDE / 2 - adjust / 2, 510 + MEDIUM_FONT.getSize() * 5);

        	adjust = fontvals.stringWidth("Arrow keys and space to select");
        	g.drawString("Arrow keys and space to select", WIDE / 2 - adjust / 2, 510 + MEDIUM_FONT.getSize() * 7);
        }
        
        public void drawPlay(Graphics g) {
        	panel.setBackground(Color.blue);
        	
        	g.setColor(Color.green);
        	g.drawRect(MARGIN, 5, 500, MARGIN - 10);
        	g.fillRect(MARGIN, 5, p1.health * 5, MARGIN - 10);
        	g.setColor(Color.white);
        	g.setFont(SMALL_FONT);
        	g.drawString(p1.health + "/" + p1.maxHealth, MARGIN + 220, MARGIN / 2 + 5);
        	g.drawString("Score: " + playerPoints, MARGIN + 700, MARGIN / 2 + 5);
        	
        	maze.draw(g, p1);
        }
        
        public void drawLeaderboard(Graphics g) {
        	panel.setBackground(Color.gray);
        	
        	g.setColor(Color.black);
        	g.setFont(BIG_FONT);
    		FontMetrics fontvals = g.getFontMetrics();
    		int adjust = fontvals.stringWidth("LEADERBOARD");
        	g.drawString("LEADERBOARD", WIDE / 2 - adjust / 2, 100);
        	
        	for(int x = 0; x < topTenScores.size(); x++) {
        		adjust = fontvals.stringWidth(x + ". " + topTenScores.get(x).toString());
        		if(x < 9)
        			g.drawString((x + 1) + ".   " + topTenScores.get(x).toString(), 100, x * BIG_FONT.getSize() + 200);
        		else
        			g.drawString((x + 1) + ". " + topTenScores.get(x).toString(), 100, x * BIG_FONT.getSize() + 200);
        	}
        	
        	g.setFont(SMALL_FONT);
        	fontvals = g.getFontMetrics();
        	adjust = fontvals.stringWidth("Press enter to return to menu.");
        	g.drawString("Press enter to return to menu.", WIDE / 2 - adjust / 2, HIGH - 20);
        }
        
        public void drawGameOver(Graphics g) {
        	
        	panel.setBackground(Color.black);
        	g.setColor(Color.red);
        	g.setFont(VERY_BIG_FONT);
        	
        	FontMetrics fontvals = g.getFontMetrics();
        	int adjust = fontvals.stringWidth("YOU DIED");
        	g.drawString("YOU DIED", WIDE / 2 - adjust / 2, HIGH / 2 - 100);
        	
        	g.setFont(SMALL_FONT);
        	g.setColor(Color.white);
        	
        	fontvals = g.getFontMetrics();
        	
        	adjust = fontvals.stringWidth("Your score was: " + playerPoints + " points");
        	g.drawString("Your score was: " + playerPoints + " points", WIDE / 2 - adjust / 2, HIGH / 2 - 50);
        	
        	
        	for(int x = 0; x < topTenScores.size(); x++) {
        		if(!onLeaderboard && playerPoints > topTenScores.get(x).score) {
        			topTenScores.remove(topTenScores.size() - 1);
        			onLeaderboard = true;
        		}
        	}
        	
        	if(!onLeaderboard) {
        		adjust = fontvals.stringWidth("Press space to continue.");
        		g.drawString("Press space to continue.", WIDE / 2 - adjust / 2, HIGH / 2 + 50);
        	}
        	else {
        		adjust = fontvals.stringWidth("Enter your name, then press space to continue.");
        		g.drawString("Enter your name, then press space to continue.", WIDE / 2 - adjust / 2, HIGH / 2 + 100);
        		
        		leadName = "" + alphabet.charAt(leadL1) + alphabet.charAt(leadL2) + alphabet.charAt(leadL3);
        		g.setFont(BIG_FONT);
        		fontvals = g.getFontMetrics();
        		adjust = fontvals.stringWidth(leadName);
        		g.drawString(leadName, WIDE / 2 - adjust / 2, HIGH / 2 + 50);
        	}
        }
        
        public void drawExit(Graphics g) {
        	panel.setBackground(Color.red);
        }
        
    }
    
    public void printLeaderboard() {
    	System.out.println("PRINTING SCORES");
		for(int x = 0; x < topTenScores.size(); x++) {
			LeaderboardScore s = topTenScores.get(x);
			if(x < 9)
				System.out.println((x + 1) + ".  " + s.name + "   " + s.score + "   " + s.levelsCleared);
			else
				System.out.println((x + 1) + ". " + s.name + "   " + s.score + "   " + s.levelsCleared);
		}
    }
}