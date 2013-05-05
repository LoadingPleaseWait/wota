package de.wota.graphics;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.nio.FloatBuffer;


import de.wota.Player;
import de.wota.gameobjects.Ant;
import de.wota.gameobjects.AntObject;
import de.wota.gameobjects.GameWorld;
import de.wota.gameobjects.GameWorldParameters;
import de.wota.testing.TestWorld;
import de.wota.utility.Vector;

/**
 * Renders everything.
 * 
 * Preliminary: Use this to test.
 * 
 * @author Daniel
 */
public class View {
	// hardcoded number of players = 8
	private static final Color[] colors = { Color.RED, Color.BLUE, Color.GREEN,
			Color.CYAN, Color.PINK, Color.MAGENTA, Color.ORANGE, Color.YELLOW };

	private GameWorld world;

	public View(GameWorld world) {
		this.world = world;
	}

	public void setup(int width, int height) {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		// coordinate system origin at lower left with width and height same as
		// the window
		glOrtho(0, GameWorldParameters.SIZE_X, 0, GameWorldParameters.SIZE_Y, -1, 1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glViewport(0, 0, width, height);
	}

	private static final int ANT_RADIUS = 2;

	public void render(int width, int height) {
		glClear(GL_COLOR_BUFFER_BIT);

		glLoadIdentity();

		for (Player player : world.players) {
			Color color = colors[player.getId()];
			float[] colorComponents = color.getColorComponents(null);
			glColor3f(colorComponents[0], colorComponents[1], colorComponents[2]);
			
			for (AntObject antObject : player.antObjects) {
				renderCircle(antObject.getPosition(), ANT_RADIUS);
			}

			renderCircle(player.hillObject.getPosition(), GameWorldParameters.HILL_RADIUS);
		}

	}

	private void translate(Vector p) {
		glTranslated(p.x, p.y, 0);
	}

	private void renderCircle(Vector p, double radius) {
		glPushMatrix();
		translate(p);
		glScaled(radius, radius, radius);
		renderUnitCircle();
		glPopMatrix();
	}

	final int numberOfCircleCorners = 24;

	private void renderUnitCircle() {
		glBegin(GL_TRIANGLE_FAN);
		glVertex2f(0, 0);
		for (int i = 0; i <= numberOfCircleCorners; i++) {
			final double angle = 2 * Math.PI * i / numberOfCircleCorners;
			glVertex2d(Math.cos(angle), Math.sin(angle));
		}
		glEnd();
	}

	/**
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		GameWorld gameWorld = TestWorld.testWorld();
		View view = new View(gameWorld);
		final int width = 700;
		final int height = 700;
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// init OpenGL here

		view.setup(width, height);
		
		while (!Display.isCloseRequested()) {
			gameWorld.tick();
			view.render(width, height);
			Display.update();
		}
		
		Display.destroy();
	}
}