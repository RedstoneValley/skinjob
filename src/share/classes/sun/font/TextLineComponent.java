package sun.font;

import static java.awt.SkinJob.rangeMaybeCopy;

import android.graphics.Rect;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.SkinJob;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Created by cryoc on 2016-10-15.
 */
public class TextLineComponent {
  // TODO i18n: Do width methods need to flip the sign for RTL text?

  public static final int LEFT_TO_RIGHT = 1;
  public static final int RIGHT_TO_LEFT = -1;
  public static final int UNCHANGED = 0;
  private static final char LEFT_TO_RIGHT_OVERRIDE = '\u202d';
  private static final char RIGHT_TO_LEFT_OVERRIDE = '\u202e';
  protected final Decoration decorator;
  private final char[] chars;
  private final Font font;
  private final CoreMetrics coreMetrics;
  private final int indexOffset; // For abstracting away an LTR-override or RTL-override prefix

  public TextLineComponent(
      char[] chars, Font font, CoreMetrics coreMetrics, Decoration decorator) {
    this(chars, font, coreMetrics, decorator, 0);
  }

  public TextLineComponent(
      char[] chars, Font font, CoreMetrics coreMetrics, Decoration decorator, int indexOffset) {
    this.chars = chars;
    this.font = font;
    this.coreMetrics = coreMetrics;
    this.decorator = decorator;
    this.indexOffset = indexOffset;
  }

  public boolean isSimple() {
    // TODO
    return false;
  }

  public CoreMetrics getCoreMetrics() {
    return coreMetrics;
  }

  public float getAdvance() {
    return getAdvanceBetween(0, getNumCharacters());
  }

  public AffineTransform getBaselineTransform() {
    // TODO
    return null;
  }

  public float getCharX(int indexInArray) {
    // TODO
    return 0;
  }

  public float getCharY(int indexInArray) {
    // TODO
    return 0;
  }

  public int getNumCharacters() {
    return chars.length - indexOffset;
  }

  public Rectangle getPixelBounds(FontRenderContext frc, float v, float v1) {
    // TODO
    return null;
  }

  public boolean caretAtOffsetIsValid(int i) {
    // TODO
    return false;
  }

  public Rectangle2D getCharVisualBounds(int indexInTlc) {
    return getBounds(indexInTlc, 1);
  }

  public void draw(Graphics2D g2, float v, float v1) {
    // TODO
  }

  public Shape getOutline(float loc, float loc1) {
    // TODO
    return null;
  }

  public int getNumJustificationInfos() {
    // TODO
    return 0;
  }

  public void getJustificationInfos(
      GlyphJustificationInfo[] infos, int infoPosition, int rangeMin, int rangeMax) {
    // TODO
  }

  public TextLineComponent applyJustificationDeltas(float[] deltas, int i, boolean[] flags) {
    // TODO
    return new TextLineComponent();
  }

  public Rectangle2D getItalicBounds() {
    // TODO
    return null;
  }

  public Rectangle2D getVisualBounds() {
    return getBounds(0, chars.length);
  }

  public float getCharAdvance(int indexInArray) {
    return getAdvanceBetween(indexInArray + indexOffset, 1);
  }

  public float getAdvanceBetween(int measureStart, int measureLimit) {
    return font.getAndroidPaint().measureText(chars, measureStart + indexOffset, measureLimit);
  }

  protected Rectangle2D getBounds(int measureStart, int measureLimit) {
    Rect bounds = new Rect();
    font.getAndroidPaint().getTextBounds(chars, measureStart + indexOffset, measureLimit, bounds);
    return SkinJob.androidRectToRectangle2D(bounds);
  }

  public int getLineBreakIndex(int i, float width) {
    // TODO
    return 0;
  }

  public TextLineComponent getSubset(int start, int length, int subsetFlag) {
    switch (subsetFlag) {
      case UNCHANGED:
        return new TextLineComponent(
            rangeMaybeCopy(chars, start + indexOffset, length),
            font,
            coreMetrics,
            decorator);
      case LEFT_TO_RIGHT:
      case RIGHT_TO_LEFT:
        // Subset is to have a specific text direction, so prepend an override character to the
        // substring
        char[] subsetWithOverride = new char[length + 1];
        subsetWithOverride[0] = subsetFlag == RIGHT_TO_LEFT ? RIGHT_TO_LEFT_OVERRIDE
            : LEFT_TO_RIGHT_OVERRIDE;
        System.arraycopy(chars, start + indexOffset, subsetWithOverride, 1, length);
        return new TextLineComponent(subsetWithOverride, font, coreMetrics, decorator, 1);
      default:
        throw new IllegalArgumentException("Unrecognized subsetFlag value " + subsetFlag);
    }
  }
}
