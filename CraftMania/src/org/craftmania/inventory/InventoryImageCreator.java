/*******************************************************************************
 * Copyright 2012 Martijn Courteaux <martijn.courteaux@skynet.be>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.craftmania.inventory;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.craftmania.Side;
import org.craftmania.blocks.BlockType;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.MathHelper;
import org.craftmania.math.Vec2f;
import org.craftmania.math.Vec3f;
import org.newdawn.slick.opengl.Texture;

/**
 * 
 * @author martijncourteaux
 */
public class InventoryImageCreator
{

	private int _size;

	public InventoryImageCreator(int size)
	{
		_size = size;
	}

	/**
	 * Default size 32 * 32
	 */
	public InventoryImageCreator()
	{
		this(32);
	}

	/**
	 * Creates an inventory image of the blocktype
	 * 
	 * @param bt
	 * @return
	 */
	public BufferedImage createInventoryImage(BlockType bt)
	{

		// Changed from TYPE_4BYTES_ABGR to TYPE_INT_ARGB
		BufferedImage bi = new BufferedImage(_size, _size, BufferedImage.TYPE_INT_ARGB);

		Vec2f leftPos = bt.getBrush().getTexturePositionInGridFor(Side.LEFT);
		Vec2f topPos = bt.getBrush().getTexturePositionInGridFor(Side.TOP);
		Vec2f frontPos = bt.getBrush().getTexturePositionInGridFor(Side.FRONT);

		Vec3f leftColor = bt.getBrush().getColorFor(Side.LEFT);
		Vec3f topColor = bt.getBrush().getColorFor(Side.TOP);
		Vec3f frontColor = bt.getBrush().getColorFor(Side.FRONT);

		Texture texture = TextureStorage.getTexture("terrain");

		BufferedImage srcImage = textureToBufferedImage(texture);

		Graphics2D g = bi.createGraphics();
		g.scale(_size / 16.0f, _size / 16.0f);
		// Left
		{
			AffineTransform transform = new AffineTransform();
			transform.translate(0, 3.72f);
			transform.scale(0.5, 0.5);
			transform.shear(0.0, 0.36);
			BufferedImage leftSide = getSubTexture(srcImage, (int) leftPos.x(), (int) leftPos.y());
			leftSide = applyColorFilter(leftSide, leftColor.x(), leftColor.y(), leftColor.z());
			g.drawImage(leftSide, transform, null);
		}
		// Front
		{
			AffineTransform transform = new AffineTransform();
			transform.translate(8, 6.62f);
			transform.scale(0.5, 0.5);
			transform.shear(0.0, -0.36);
			BufferedImage frontSide = getSubTexture(srcImage, (int) frontPos.x(), (int) frontPos.y());
			frontSide = applyColorFilter(frontSide, frontColor.x(), frontColor.y(), frontColor.z());
			g.drawImage(frontSide, transform, null);
		}
		// Top
		{
			AffineTransform transform = new AffineTransform();
			transform.scale(16.0d / 22.6d, 0.26);
			transform.translate(22.6d / 2.0f, 3);
			transform.rotate(Math.PI / 4.0d);

			BufferedImage topSide = getSubTexture(srcImage, (int) topPos.x(), (int) topPos.y());
			topSide = applyColorFilter(topSide, topColor.x(), topColor.y(), topColor.z());
			g.drawImage(topSide, transform, null);
		}

		g.dispose();

		return bi;
	}

	private static BufferedImage textureToBufferedImage(Texture t)
	{
		int w = t.getTextureWidth();
		int h = t.getTextureHeight();
		byte[] data = t.getTextureData();
		boolean hasAlpha = t.hasAlpha();
		int bytesPerPixel = data.length / (w * h);
		int imageFormat = 0;
		if (hasAlpha)
		{
			if (bytesPerPixel == 4)
			{
				imageFormat = BufferedImage.TYPE_4BYTE_ABGR;
			} else
			{
				imageFormat = BufferedImage.TYPE_CUSTOM;
			}
		} else
		{
			if (bytesPerPixel == 3)
			{
				imageFormat = BufferedImage.TYPE_3BYTE_BGR;
			} else
			{
				imageFormat = BufferedImage.TYPE_CUSTOM;
			}
		}

		BufferedImage bi = new BufferedImage(w, h, imageFormat);

		for (int x = 0; x < w; ++x)
		{
			for (int y = 0; y < h; ++y)
			{
				int pixelOffset = (y * w + x) * bytesPerPixel;
				byte red = data[pixelOffset + 0];
				byte green = data[pixelOffset + 1];
				byte blue = data[pixelOffset + 2];
				byte alpha = 0;
				if (hasAlpha)
				{
					alpha = data[pixelOffset + 3];
				}
				int argb = (alpha << 24) + (red << 16) + (green << 8) + blue;
				bi.setRGB(x, y, argb);
			}
		}
		return bi;
	}

	private static BufferedImage getSubTexture(BufferedImage srcImage, int x, int y)
	{
		return srcImage.getSubimage(x * 16, y * 16, 16, 16);
	}

	private static BufferedImage applyColorFilter(BufferedImage img, float r, float g, float b)
	{
		BufferedImage bi = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int x = 0; x < img.getWidth(); ++x)
		{
			for (int y = 0; y < img.getHeight(); ++y)
			{
				int rgb = img.getRGB(x, y);

				int alpha = (byte) ((rgb >> 24) & 0xFF);
				float red = ((rgb >> 16) & 0xFF);
				float green = ((rgb >> 8) & 0xFF);
				float blue = (rgb & 0xFF);

				red *= r;
				green *= g;
				blue *= b;

				int rr = MathHelper.floor(red);
				int gg = MathHelper.floor(green);
				int bb = MathHelper.floor(blue);

				rr = MathHelper.clamp(rr, 0, 255);
				gg = MathHelper.clamp(gg, 0, 255);
				bb = MathHelper.clamp(bb, 0, 255);

				rgb = (alpha << 24) | (rr << 16) | (gg << 8) | bb;
				bi.setRGB(x, y, rgb);
			}
		}
		return bi;
	}
}
