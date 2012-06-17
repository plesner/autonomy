package org.au.tonomy.gwt.client.canvas;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;

/**
 * The geography of a hexagon:
 *
 *        N
 *       / \
 *  NW /     \ NE <- north shoulder
 *    |       |
 *    E       W
 *    |       |
 *  SW \     / SE <- south shoulder
 *       \ /
 *        S
 */
public class WorldRenderer {

  private static final double LONG = Math.sin(Math.PI / 3);
  private static final double SHORT = Math.cos(Math.PI / 3);
  private static final double WIDTH = 2 * LONG;
  private static final double HEIGHT = 1 + SHORT;

  private final Canvas canvas;
  private int offsetX = 0;
  private int offsetY = 0;

  public WorldRenderer(Canvas canvas) {
    this.canvas = canvas;
  }

  public void move(int dx, int dy) {
    offsetX += dx;
    offsetY += dy;
  }

  public void repaint() {
    Context2d context = canvas.getContext2d();
    context.setFillStyle("red");
    context.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
    context.beginPath();
    context.setStrokeStyle("grey");
    double edge = 20;
    for (int x = 0; x < 9; x++) {
      for (int y = 0; y < 4; y++) {
        // Move to the north western corner.
        double westX = offsetX + (WIDTH * x * edge);
        double northShoulderY = offsetY + ((2 * HEIGHT) * y * edge);
        context.moveTo(westX, northShoulderY);
        // Eastern edge.
        double southShoulderY = northShoulderY + edge;
        context.lineTo(westX, southShoulderY);
        // South western edge.
        double middleX = westX + (LONG * edge);
        double southY = southShoulderY + (SHORT * edge);
        context.lineTo(middleX, southY);
        // Eastern edge below.
        context.lineTo(middleX, southY + edge);
        // Move back to the north west corner.
        context.moveTo(westX, northShoulderY);
        // North western edge
        double northY = northShoulderY - (SHORT * edge);
        context.lineTo(middleX, northY);
        // North eastern edge
        double eastX = middleX + (LONG * edge);
        context.lineTo(eastX, northShoulderY);
        // Move to the south corner.
        context.moveTo(middleX, southY);
        // South eastern edge.
        context.lineTo(eastX, southShoulderY);
      }
    }
    context.stroke();
  }

}
