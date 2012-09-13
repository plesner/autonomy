package org.au.tonomy.client.widget;

import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.widget.WorldWidget.IColorScheme;
import org.au.tonomy.client.widget.WorldWidget.IColorScheme.IConstants;
import org.au.tonomy.shared.util.Assert;

import com.google.gwt.core.client.GWT;

/**
 * An adapter that turns CSS colors from the color scheme constants
 * into WebGL compatible objects.
 */
public class RenderColorScheme {

  private static RenderColorScheme instance = null;
  private final Color tileColor;

  public static RenderColorScheme get() {
    if (instance == null) {
      IColorScheme scheme = GWT.create(IColorScheme.class);
      instance = new RenderColorScheme(scheme.getConstants());
    }
    return instance;
  }

  public RenderColorScheme(IConstants constants) {
    this.tileColor = parseCssColor(constants.ccTileColor());
  }

  public Color getTileColor() {
    return tileColor;
  }

  private static Color parseCssColor(String color) {
    Assert.equals('#', color.charAt(0));
    Assert.equals(7, color.length());
    double r = Integer.parseInt(color.substring(1, 3), 16);
    double g = Integer.parseInt(color.substring(3, 5), 16);
    double b = Integer.parseInt(color.substring(5, 7), 16);
    return Color.create(r / 255, g / 255, b / 255, 1.0);
  }

}
