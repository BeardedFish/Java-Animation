/*
 * By: Darian Benam
 * Date: April 9th, 2018
 * 
 * Program Description: This program is an animation of different objects. It has two squares that move in opposite directions. When 
 * they reach the end of the line, the line and two squares rotate 180.0 degrees and move again. Every so often,
 * the background changes due to a expanding colorful circle (which has several colors). There is also a moving 
 * ball. If the moving ball intersects with the circle in the middle then the border of circle becomes colored 
 * and thick.
 */

package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class JavaAnimation extends JFrame implements KeyListener
{
	/************************************/
	/*     BEGIN CONSTANT VARIABLES     */
	/************************************/
	
	private final int WINDOW_SIZE = 500; // The length and width of the window, in pixels.
	private final int SQUARE_SIZE = 25; // The length and width of the two squares for the animation, in pixels.
	private final int LINE_WIDTH = 100; // The width of the line for the animation, in pixels.
	private final int BALL_SIZE = 25; // The length and width of the ball, in pixels
	
	/* ==== END CONSTANT VARIABLES ==== */
	
	/************************************/
	/*      BEGIN GLOBAL VARIABLES      */
	/************************************/
	
	private DrawingPanel mainPanel = new DrawingPanel(); // The panel which all the graphics will be drawn onto.
	
	private Direction ballDirection = Direction.RIGHT; // The direction the ball should move.
	
	private Rectangle[] animationObjects; // An array which will contain all the rectangles used for the animation.
	
	private double angle = 0.0; // The current angle of the two squares and the line.
	
	private boolean animationPaused = false; // States whether the animation has been paused or not.
	private boolean showHeadsUpDisplay = true; // States whether the heads-up display is shown or not.
	private boolean flipped = false; // States whether the squares and line have been rotated from it's original position or not.
	private boolean circleShouldGrow = true;
	private boolean switchColors = false; // States whether square one and square two's colors have been switched.
	private boolean moveSquares = true; // States whether the two squares should move or not.
	private boolean circleHasThickBorder = false; // States whether the animation's circular background border is thick or not.

	private Timer timer;
	private int timerSpeed = 5;
	private int timerInitialDelay = 1000;
	private int elapsedTime;

	private Color bgColor = new Color(27, 33, 30); // Background color of the window.
	private Color squareOneColor = new Color(255, 27, 81); // Color of square one.
	private Color squareTwoColor = new Color(255, 255, 255); // Color of square two.
	private Color ballColor = new Color(234, 17, 54); // Color of the moving ball.

	private int currentColorIndex = 0; // The current index for the 'colors' array which the background should be.
	private Color[] colors = 
	{
		new Color(197, 1, 225),
		new Color(156, 38, 248),
		new Color(150, 40, 249),
		new Color(98, 102, 253),
		new Color(46, 150, 249),
		new Color(98, 102, 253),
		new Color(4, 194, 230),
		new Color(23, 229, 208),
		new Color(46, 249, 160),
		new Color(106, 255, 109),
		new Color(198, 229, 1),
		new Color(233, 197, 1),	
		new Color(255, 108, 98),
		new Color(243, 44, 155),
		new Color(232, 48, 206),
		new Color(27, 33, 30)
	};
	
	/*  ==== END GLOBAL VARIABLES ====  */
	
	/*
	 * Main entry point of the program.
	 */
	public static void main(String[] args) 
	{
		// Run GUI codes in Event-Dispatching thread for thread safety:
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				new JavaAnimation();  // Let the constructor do the job.
			}
		});
	}
	
	/*
	 * Main constructor of the class.
	 */
	JavaAnimation()
	{
		setupObjectsArray();
		positionSquareDefaultLocations();	
		setupWindow();
		setupTimer();
	}
	
	/*
	 * Sets up the JFrame window for the animation.
	 */
	private void setupWindow()
	{
		this.setTitle("Loading Animation - By: Darian Benam");
		this.setSize(500, 500);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		this.addKeyListener(this);
		
		mainPanel.setBackground(bgColor);
		
		this.add(mainPanel);
		this.setVisible(true);
	}
	
	/*
	 * Sets up the timer for the animation.
	 */
	private void setupTimer()
	{
		timer = new Timer(timerSpeed, new TimerAL());
		timer.setInitialDelay(timerInitialDelay);
		timer.start();
		timer.setInitialDelay(0);
	}
	
	/*
	 * Sets up the fixed array for the animation that will hold all the rectangle objects.
	 */
	private void setupObjectsArray()
	{
		animationObjects = new Rectangle[6];
		
		animationObjects[0] = new Rectangle(WINDOW_SIZE / 2 - LINE_WIDTH / 2, WINDOW_SIZE / 2 - 20, LINE_WIDTH, 10); // The loadingIconObjects[0].
		animationObjects[1] = new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE); // Square 1.
		animationObjects[2] = new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE); // Square 2.
		animationObjects[3] = new Rectangle(animationObjects[0].x - 30 , animationObjects[0].y - animationObjects[0].width / 2 - 30, animationObjects[0].width + 60, animationObjects[0].width + 70); // Circle background.
		animationObjects[4] = null; // Expanding circle background.
		animationObjects[5] = new Rectangle(-BALL_SIZE, WINDOW_SIZE / 2 - BALL_SIZE, BALL_SIZE, BALL_SIZE); // Ball.
	}
	
	/*
	 * Positions the two squares to their default starting locations.
	 */
	private void positionSquareDefaultLocations()
	{
		int spacing = 10; // The spacing the squares should be apart from the line, in pixels.
		
		// Position Square #1:	
		animationObjects[1] .x = animationObjects[0].x;
		animationObjects[1] .y = animationObjects[0].y - SQUARE_SIZE - spacing;
		
		// Position Square #2:	
		animationObjects[2].x = animationObjects[0].x + animationObjects[0].width - SQUARE_SIZE;
		animationObjects[2].y = animationObjects[0].y + animationObjects[0].height + spacing;
	}
	
	/*
	 * This timer handles all the logic behind the animation (such as moving the two squares).
	 */
	private class TimerAL implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			elapsedTime++;
			
			if (animationPaused) // Don't do anything else if the animation is paused.
			{
				return;
			}
			
			moveBall(); // Move the ball.
			growAndShrinkCircle(); // Grow and shrink the background circle of the animation.
			expandCircle(); // Expand the background circle
			moveSquares(); // Move the two squares of the animation.
			switchSquareColors(); // Switch the colors of the two squares if there x locations match.
			flipObjects(); // Flip the two squares and lines when necessary.

			// Update the main panels graphics:
			mainPanel.repaint();		
		}

		/*
		 * Moves the squares in their respective direction according to the loadingIconObjects[0]s flipped state.
		 */
		private void moveSquares()
		{
			if (moveSquares)
			{
				if (flipped)
				{
					animationObjects[1].x--;
					animationObjects[2].x++;
				}
				else
				{
					animationObjects[1].x++;
					animationObjects[2].x--;
				}
			}
		}
		
		/*
		 * Switches the two squares colors if their 'X' locations match.
		 */
		private void switchSquareColors()
		{
			if (animationObjects[1].x == animationObjects[2].x - 1)
			{
				switchColors = !switchColors;
			}
		}
		
		/*
		 * Flips the two squares and the line of the animation 180.0 degrees when the two squares reach the end of the line.
		 */
		private void flipObjects()
		{
			if (animationObjects[1].x == animationObjects[0].x + animationObjects[0].width - SQUARE_SIZE)
			{
				moveSquares = false;
		
				if (angle <= 180.0)
				{
					angle += 1.0;
					
					if (angle == 180.0)
					{
						flipped = true;
						moveSquares = true;
					}
				}
			}
			
			if (flipped && animationObjects[1].x == animationObjects[0].x)
			{
				moveSquares = false;
				
				if (angle <= 360.0)
				{
					angle += 1.0;
					
					if (angle >= 360.0)
					{
						angle = 0.0; // Reset the angle to 0.0 degrees if it reaches 360.0 degrees.
						flipped = false;
						moveSquares = true;
					}
				}	
			}
		}
		
		/*
		 * Grows and shrinks the background circle of the animation.
		 */
		private void growAndShrinkCircle()
		{
			if (circleShouldGrow)
			{
				if (animationObjects[3].width >= animationObjects[0].width + 150)
				{
					circleShouldGrow = false;
					return;
				}
				
				animationObjects[3].x -= 1;
				animationObjects[3].width += 2;

				animationObjects[3].y -= 1;
				animationObjects[3].height += 2;
			}
			else
			{			
				if (animationObjects[3].width <= animationObjects[0].width + 70)
				{
					circleShouldGrow = true;
					return;
				}
				
				animationObjects[3].x += 1;
				animationObjects[3].width -= 2;

				animationObjects[3].y += 1;
				animationObjects[3].height -= 2;
			}
		}
		
		/*
		 * Expands the background circle which will change the background color of the entire window.
		 */
		private void expandCircle()
		{
			if (elapsedTime % 500 == 0)
			{
				animationObjects[4] = new Rectangle(animationObjects[0].x + animationObjects[0].width / 2, animationObjects[0].y + animationObjects[0].height / 2, 0, 0);
				
				currentColorIndex++; // Move onto the next color in the color array.
				
				if (currentColorIndex >= colors.length) // Reset the color index to 0 if it goes out of bounds.
				{
					currentColorIndex = 0;
				}
				
				bgColor = colors[currentColorIndex]; // Set the current background color to the current color index/
			}
			
			if (animationObjects[4] != null)
			{
				// Expand the circle in the x and y directions:
				
				animationObjects[4].x -= 1;
				animationObjects[4].width += 2;

				animationObjects[4].y -= 1;
				animationObjects[4].height += 2;
				
				// Dispose of the expanding circle if it goes off the screen:
				if (animationObjects[4].width == getWidth() + 250)
				{
					mainPanel.setBackground(bgColor);
					animationObjects[4] = null;
				}

			}
		}
		
		/*
		 * Moves the ball in it's respective direction, and switches the direction to a new one if it goes out of bounds on the window.
		 */
		private void moveBall()
		{
			// Move the loadingIconObjects[5] in its respective direction:
			switch (ballDirection)
			{
				default:
				case UP:
					animationObjects[5].y--;
					break;
				case DOWN:
					animationObjects[5].y++;
					break;
				case LEFT:
					animationObjects[5].x--;
					break;
				case RIGHT:
					animationObjects[5].x++;
					break;
			}
			
			// Check if the moving ball is out of bounds for its current direction, if it is then change the location and direction:			
			
			if (animationObjects[5].x >= getWidth() + BALL_SIZE && ballDirection == Direction.RIGHT)
			{
				animationObjects[5].x = WINDOW_SIZE / 2 - BALL_SIZE / 2;
				animationObjects[5].y = -BALL_SIZE;
				ballDirection = Direction.DOWN;
			}
			
			if (animationObjects[5].y >= getHeight() + BALL_SIZE && ballDirection == Direction.DOWN)
			{
				animationObjects[5].x = WINDOW_SIZE + BALL_SIZE / 2;
				animationObjects[5].y = WINDOW_SIZE / 2 - BALL_SIZE;
				ballDirection = Direction.LEFT;
			}
			
			if (animationObjects[5].x <= -BALL_SIZE && ballDirection == Direction.LEFT)
			{
				animationObjects[5].x = WINDOW_SIZE / 2 - BALL_SIZE / 2;
				animationObjects[5].y = WINDOW_SIZE + BALL_SIZE / 2;
				ballDirection = Direction.UP;
			}
			
			if (animationObjects[5].y <= -BALL_SIZE && ballDirection == Direction.UP)
			{
				animationObjects[5].x = -BALL_SIZE;
				animationObjects[5].y = WINDOW_SIZE / 2 - BALL_SIZE;
				ballDirection = Direction.RIGHT;
			}
						
			if (animationObjects[5].intersects(animationObjects[3]))
			{
				circleHasThickBorder = true;
			}
			else
			{
				circleHasThickBorder = false;
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class DrawingPanel extends JPanel 
	{
		@Override
		public void paintComponent(Graphics g) 
		{	
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
			
			//g2d.setStroke(new BasicStroke(10));
			
			// Draw the expanding background circle with it's current color index (that is, if it's not null):
			g.setColor(colors[currentColorIndex]);
			if (animationObjects[4] != null)
			{
				g.fillOval(animationObjects[4].x, animationObjects[4].y, animationObjects[4].width, animationObjects[4].height);
			}
			
			// Draw the moving ball:
			g.setColor(ballColor);
			g.fillOval(animationObjects[5].x, animationObjects[5].y, animationObjects[5].width, animationObjects[5].height);
			
			// Draw the animation's circular background:
			g.setColor(new Color(40, 46, 43));
			g.fillOval(animationObjects[3].x, animationObjects[3].y, animationObjects[3].width, animationObjects[3].height);
			
			// Draw the animation's circular background border (it will be thick if the moving ball intersects with it, and not thick if it doesn't):
			int strokeSize; // The thickness of the border, in pixels.		
			if (circleHasThickBorder)
			{
				g2d.setStroke(new BasicStroke(25));
				strokeSize = 25;
				g.setColor(ballColor);
			}
			else
			{
				g2d.setStroke(new BasicStroke(10));
				strokeSize = 10;
				g.setColor(Color.WHITE);
			}
			g.drawOval(animationObjects[3].x - strokeSize / 2, animationObjects[3].y - strokeSize / 2, animationObjects[3].width + strokeSize - 1, animationObjects[3].height + strokeSize - 1);
			
			// Rotate the two squares and the line to the current angle value (everything after g2d.setTransform(old) will be rotated):
			
			/******************/
			/* BEGIN ROTATING */
			/******************/
			
			AffineTransform transform = new AffineTransform();
			transform.rotate(Math.toRadians(angle), animationObjects[0].getX() + animationObjects[0].width / 2, animationObjects[0].getY() + animationObjects[0].height / 2);
			AffineTransform old = g2d.getTransform();
			g2d.transform(transform);

			g.setColor(Color.WHITE);		
			g.fillRect(animationObjects[0].x, animationObjects[0].y, animationObjects[0].width, animationObjects[0].height);
			
			// Draw square one:
			if (switchColors)
			{
				g.setColor(squareTwoColor);
			}
			else
			{
				g.setColor(squareOneColor);
			}	
			g.fillRect(animationObjects[1].x, animationObjects[1].y, animationObjects[1].width, animationObjects[1].height);
			
			// Draw square two:
			if (switchColors)
			{
				g.setColor(squareOneColor);
			}
			else
			{
				g.setColor(squareTwoColor);
			}
			g.fillRect(animationObjects[2].x, animationObjects[2].y, animationObjects[2].width, animationObjects[2].height);
			
			g2d.setTransform(old);
			
			/******************/
			/*  END ROTATING  */
			/******************/
			
			// Draw Heads-Up Display information on the panel:		
			if (showHeadsUpDisplay)
			{
				g.setColor(Color.WHITE);
				g.drawString("Time: " + elapsedTime, 15, 25);
				g.drawString("Paused: " + animationPaused, 15, 40);
				g.drawString("Line Angle: " + angle, 15, 55);
				g.drawString("Ball Direction: " + ballDirection, 15, 70);			
				g.drawString("By: Darian Benam", WINDOW_SIZE - 115, WINDOW_SIZE - 45);
			}
			
			g2d.dispose();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		switch (e.getKeyCode())
		{
			// Pause the animation:
			case KeyEvent.VK_SPACE:
				animationPaused = !animationPaused;
				break;
			
			// Toggle Heads-Up Display:
			case KeyEvent.VK_H:
				showHeadsUpDisplay = !showHeadsUpDisplay;
				break;
		}
		
		mainPanel.repaint();	
	}

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }
}
