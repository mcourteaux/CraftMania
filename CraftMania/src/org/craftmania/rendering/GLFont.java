package org.craftmania.rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.craftmania.game.TextureStorage;
import org.craftmania.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class GLFont
{
	// the font that was used to generate character set

	Font font;
	// display list base for characters
	int fontListBase = -1;
	// texture containing a grid of 100 printable characters
	int fontTextureHandle = 0;
	// these will be set by getFontImageSize() and used by buildFont()
	int fontSize = 0;
	int textureSize = 0;
	// temp values for buildFont()
	int[] charwidths = new int[100];

	private Texture texture;

	/**
	 * Dynamically create a texture mapped character set with the given Font.
	 * Text color will be white on a transparent background.
	 * 
	 * @param f
	 *            Java Font object
	 */
	public GLFont(Font f)
	{
		makeFont(f, new float[] { 1, 1, 1, 1 }, new float[] { 0, 0, 0, 0 });
	}

	/**
	 * Create a texture mapped character set with the given Font, Text color and
	 * background color.
	 * 
	 * @param f
	 *            Java Font object
	 * @param fgColor
	 *            foreground (text) color as rgb or rgba values in range 0-1
	 * @param bgColor
	 *            background color as rgb or rgba values in range 0-1 (set alpha
	 *            to 0 to make transparent)
	 */
	public GLFont(Font f, float[] fgColor, float[] bgColor)
	{
		makeFont(f, fgColor, bgColor);
	}

	/**
	 * Return the handle to the texture holding the character set.
	 */
	public int getTexture()
	{
		return fontTextureHandle;
	}

	/**
	 * Prepare a texture mapped character set with the given Font, text color
	 * and background color. Characters will be textured onto quads and stored
	 * in display lists. The base display list id is stored in the fontListBase
	 * variable. After makeFont() is run the print() function can be used to
	 * render text in this font.
	 * 
	 * @param f
	 *            the font to draw characters
	 * @param fgColor
	 *            foreground (text) color as rgb or rgba values in range 0-1
	 * @param bgColor
	 *            background color as rgb or rgba values in range 0-1 (set alpha
	 *            to 0 to make transparent)
	 * 
	 * @see createFontImage()
	 * @see print()
	 */
	public void makeFont(Font f, float[] fgColor, float[] bgColor)
	{
		int charsetTexture = 0;
		if ((charsetTexture = makeFontTexture(f, fgColor, bgColor)) > 0)
		{
			// create 100 display lists, one for each character
			// textureSize and fontSize are calculated by createFontImage()
			buildFont(charsetTexture, textureSize, fontSize);
			fontTextureHandle = charsetTexture;
			font = f;
		}
	}

	/**
	 * Return a texture containing a character set with the given Font arranged
	 * in a 10x10 grid of printable characters.
	 * 
	 * @param f
	 *            the font to draw characters
	 * @param fgColor
	 *            foreground (text) color as rgb or rgba values in range 0-1
	 * @param bgColor
	 *            background color as rgb or rgba values in range 0-1 (set alpha
	 *            to 0 to make transparent)
	 * @see createFontImage()
	 * @see print()
	 */
	public int makeFontTexture(Font f, float[] fgColor, float[] bgColor)
	{
		int texture = 0;
		try
		{
			// Create a BufferedImage containing a 10x10 grid of printable
			// characters
			BufferedImage image = createFontImage(f, // the font
					fgColor, // text color
					bgColor); // background color
			// make a texture with the buffered image
			this.texture = TextureStorage.loadTexture("font-" + f.getFontName(), image);
			texture = this.texture.getTextureID();
		} catch (Exception e)
		{
			System.out.println("makeChar(): exception " + e);
		}
		return texture;
	}

	/**
	 * Return a texture containing the given single character with the Courier
	 * font. TO DO: pass Font as a parameter.
	 * 
	 * @param onechar
	 *            character to draw into texture
	 */
	public static int makeCharTexture(Font f, String onechar, float[] fgColor, float[] bgColor)
	{
		int texture = 0;
		try
		{
			// Create a BufferedImage with one character
			BufferedImage image = createCharImage(onechar, f, // the font
					fgColor, // text
					bgColor); // background
			// make a texture from the image
			Texture tex = TextureStorage.loadTexture("Font-" + f.getFontName() + "-" + onechar, image);
			texture = tex.getTextureID();
		} catch (Exception e)
		{
			System.out.println("makeChar(): exception " + e);
		}
		return texture;
	}

	/**
	 * return a BufferedImage containing the given character drawn with the
	 * given font. Character will be drawn on its baseline, and centered
	 * horizontally in the image.
	 * 
	 * @param text
	 *            a single character to render
	 * @param font
	 *            the font to render with
	 * @param fgColor
	 *            foreground (text) color as rgb or rgba values in range 0-1
	 * @param bgColor
	 *            background color as rgb or rgba values in range 0-1 (set alpha
	 *            to 0 to make transparent)
	 * @return
	 */
	public static BufferedImage createCharImage(String text, Font font, float[] fgColor, float[] bgColor)
	{
		Color bg = bgColor == null ? new Color(0, 0, 0, 0) : (bgColor.length == 3 ? new Color(bgColor[0], bgColor[1], bgColor[2], 1) : new Color(bgColor[0], bgColor[1],
				bgColor[2], bgColor[3]));
		Color fg = fgColor == null ? new Color(1, 1, 1, 1) : (fgColor.length == 3 ? new Color(fgColor[0], fgColor[1], fgColor[2], 1) : new Color(fgColor[0], fgColor[1],
				fgColor[2], fgColor[3]));
		boolean isAntiAliased = true;
		boolean usesFractionalMetrics = false;

		// get size of texture image neaded to hold largest character of this
		// font
		int maxCharSize = getFontSize(font);
		int imgSize = MathHelper.getPowerOfTwoBiggerThan(maxCharSize);
		if (imgSize > 2048)
		{
			throw new RuntimeException("GLFont.createCharImage(): texture size will be too big (" + imgSize + ") Make the font size smaller.");
		}

		// we'll draw text into this image
		BufferedImage image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// Clear image with background color (make transparent if color has
		// alpha value)
		if (bg.getAlpha() < 255)
		{
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, (float) bg.getAlpha() / 255f));
		}
		g.setColor(bg);
		g.fillRect(0, 0, imgSize, imgSize);

		// prepare to draw character in foreground color
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.setColor(fg);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, isAntiAliased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, usesFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// place the character (on baseline, centered horizontally)
		FontMetrics fm = g.getFontMetrics();
		int cwidth = fm.charWidth(text.charAt(0));
		int height = fm.getHeight();
		int ascent = fm.getAscent();
		int vborder = (int) ((float) (imgSize - height) / 2f);
		int hborder = (int) ((float) (imgSize - cwidth) / 2f);
		g.drawString(text, hborder, vborder + ascent);
		g.dispose();

		return image;
	}

	/**
	 * Return a BufferedImage containing 100 printable characters drawn with the
	 * given font. Characters will be arranged in a 10x10 grid.
	 * 
	 * @param text
	 * @param font
	 * @param imgSize
	 *            a power of two (32 64 256 etc)
	 * @param fgColor
	 *            foreground (text) color as rgb or rgba values in range 0-1
	 * @param bgColor
	 *            background color as rgb or rgba values in range 0-1 (set alpha
	 *            to 0 to make transparent)
	 * @return
	 */
	public BufferedImage createFontImage(Font font, float[] fgColor, float[] bgColor)
	{
		Color bg = bgColor == null ? new Color(0, 0, 0, 0) : (bgColor.length == 3 ? new Color(bgColor[0], bgColor[1], bgColor[2], 1) : new Color(bgColor[0], bgColor[1],
				bgColor[2], bgColor[3]));
		Color fg = fgColor == null ? new Color(1, 1, 1, 1) : (fgColor.length == 3 ? new Color(fgColor[0], fgColor[1], fgColor[2], 1) : new Color(fgColor[0], fgColor[1],
				fgColor[2], fgColor[3]));
		boolean isAntiAliased = false;
		boolean usesFractionalMetrics = false;

		// get size of texture image neaded to hold 10x10 character grid
		fontSize = getFontSize(font);
		textureSize = MathHelper.getPowerOfTwoBiggerThan(fontSize * 10);
		System.out.println("GLFont.getFontImageSize(): build font with fontsize=" + fontSize + " gridsize=" + (fontSize * 10) + " texturesize=" + textureSize);
		if (textureSize > 2048)
		{
			throw new RuntimeException("GLFont.createFontImage(): texture size will be too big (" + textureSize + ") Make the font size smaller.");
		}

		// create a buffered image to hold charset
		BufferedImage image = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// Clear image with background color (make transparent if color has
		// alpha value)
		if (bg.getAlpha() < 255)
		{
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, (float) bg.getAlpha() / 255f));
		}
		g.setColor(bg);
		g.fillRect(0, 0, textureSize, textureSize);

		// prepare to draw characters in foreground color
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.setColor(fg);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, isAntiAliased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, usesFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// get font measurements
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getMaxAscent();

		// draw the grid of 100 characters
		for (int r = 0; r < 10; r++)
		{
			for (int c = 0; c < 10; c++)
			{
				char ch = (char) (32 + ((r * 10) + c));
				g.drawString(String.valueOf(ch), (c * fontSize), (r * fontSize) + ascent);
				charwidths[(r * 10) + c] = fm.charWidth(ch);
			}
		}
		g.dispose();

		return image;
	}

	/**
	 * Return the maximum character size of the given Font. This will be the max
	 * of the vertical and horizontal font dimensions, so can be used to create
	 * a square image large enough to hold any character rendered with this
	 * Font.
	 * <P>
	 * Creates a BufferedImage and Graphics2D graphics context to get font sizes
	 * (is there a more efficient way to do this?).
	 * <P>
	 * 
	 * @param font
	 *            Font object describes the font to render with
	 * @return power-of-two texture size large enough to hold the character set
	 */
	public static int getFontSize(Font font)
	{
		boolean isAntiAliased = true;
		boolean usesFractionalMetrics = false;

		// just a dummy image so we can get a graphics context
		BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// prepare to draw character
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, isAntiAliased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, usesFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// get character measurements
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getMaxAscent();
		int descent = fm.getMaxDescent();
		int advance = fm.charWidth('W'); // width of widest char, more reliable
											// than getMaxAdvance();
		int leading = fm.getLeading();

		// calculate size of 10x10 character grid
		int fontHeight = ascent + descent + (leading / 2);
		int fontWidth = advance;
		int maxCharSize = Math.max(fontHeight, fontWidth);
		return maxCharSize;
	}

	/**
	 * Build the character set display list from the given texture. Creates one
	 * quad for each character, with one letter textured onto each quad. Assumes
	 * the texture is a 256x256 image containing every character of the charset
	 * arranged in a 16x16 grid. Each character is 16x16 pixels. Call
	 * destroyFont() to release the display list memory.
	 * 
	 * Should be in ORTHO (2D) mode to render text (see setOrtho()).
	 * 
	 * Special thanks to NeHe and Giuseppe D'Agata for the "2D Texture Font"
	 * tutorial (http://nehe.gamedev.net).
	 * 
	 * @param charSetImage
	 *            texture image containing 100 characters in a 10x10 grid
	 * @param fontWidth
	 *            how many pixels to allow per character on screen
	 * 
	 * @see destroyFont()
	 */
	public void buildFont(int fontTxtrHandle, int textureSize, int fontSize)
	{
		int unitSize = fontSize; // pixel size of one block in 10x10 grid
		float usize = (float) unitSize / (float) (textureSize); // UV size of
																// one block in
																// grid
		float chU, chV; // character UV position

		// Create 100 Display Lists
		fontListBase = GL11.glGenLists(100);

		// make a quad for each character in texture
		for (int i = 0; i < 100; i++)
		{
			int x = (i % 10); // column
			int y = (i / 10); // row

			// make character UV coordinate
			// the character V position is tricky because we have to invert the
			// V coord
			// (char # 0 is at top of texture image, but V 0 is at bottom)
			chU = (float) (x * unitSize) / (float) textureSize;
			chV = (float) (y * unitSize) / (float) textureSize;
			// chV = (float) (textureSize - (y * unitSize) - unitSize) / (float)
			// textureSize;

			GL11.glNewList(fontListBase + i, GL11.GL_COMPILE); // start display
																// list
			{
				GL11.glBegin(GL11.GL_QUADS); // Make A unitSize square quad
				{
					GL11.glTexCoord2f(chU, chV); // Texture Coord (Bottom Left)
					GL11.glVertex2i(0, unitSize);

					GL11.glTexCoord2f(chU + usize, chV); // Texture Coord
															// (Bottom Right)
					GL11.glVertex2i(unitSize, unitSize);

					GL11.glTexCoord2f(chU + usize, chV + usize); // Texture
																	// Coord
																	// (Top
																	// Right)
					GL11.glVertex2i(unitSize, 0);

					GL11.glTexCoord2f(chU, chV + usize); // Texture Coord (Top
															// Left)
					GL11.glVertex2i(0, 0);

				}
				GL11.glEnd();
				GL11.glTranslatef(charwidths[i], 0, 0); // shift right the width
														// of the character
			}
			GL11.glEndList(); // done display list
		}
	}

	/**
	 * Clean up the allocated display lists for the character set.
	 */
	public void destroyFont()
	{
		if (fontListBase != -1)
		{
			GL11.glDeleteLists(fontListBase, 100);
			fontListBase = -1;
		}
	}

	/**
	 * Render a text string in 2D over the scene, using the character set
	 * created by this GLFont object.
	 * 
	 * @see makeFont()
	 */
	public void print(int x, int y, String msg)
	{
		if (msg != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// enable the charset texture
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			texture.bind();
			// draw the text
			GL11.glTranslatef(x, y, 0); // Position The Text (in pixel coords)
			for (int i = 0; i < msg.length(); i++)
			{
				GL11.glCallList(fontListBase + (msg.charAt(i) - 32));
			}
			GL11.glPopMatrix();

		}
	}
}
