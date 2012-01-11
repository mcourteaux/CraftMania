package org.craftmania.inventory;



import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.Point;
import org.lwjgl.util.ReadablePoint;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author martijncourteaux
 */
public class RegularInventoryRaster implements InventoryRaster
{

    private int _x, _y;
    private int _cellCountX, _cellCountY;
    private int _cellW, _cellH;
    private int _marginX, _marginY;

    public RegularInventoryRaster(int x, int y, int cellCountX, int cellCountY, int cellW, int cellH, int marginX, int marginY)
    {
        this._x = x;
        this._y = y;
        this._cellCountX = cellCountX;
        this._cellCountY = cellCountY;
        this._cellW = cellW;
        this._cellH = cellH;
        this._marginX = marginX;
        this._marginY = marginY;
    }

    @Override
    public boolean isInsideRasterAABB(int x, int y)
    {
        return (x > _x + _marginX) && (x < _x + _cellCountX * (_cellW + _marginX))
                && (y > _y + _marginY) && (y < _y + _cellCountY * (_cellH + _marginY));
    }

    @Override
    public ReadablePoint getCenterOfCell(int index)
    {
        int x = getCenterOfCell(index).getX();
        int y = getCenterOfCell(index).getY();
        return new Point((int) (_x + (x + 1) * _marginX + (x + 0.5f) * _cellW),
                (int) (_y + (y + 1) * _marginY + (y + 0.5f) * _cellH));
    }

    @Override
    public Rectangle getCellAABB(int x, int y)
    {
        return new Rectangle(_x + (x + 1) * _marginX + x * _cellW, _y + (y + 1) * _marginY + y * _cellH, _cellW, _cellH);
    }

    @Override
    public int getCellAt(int x, int y)
    {
        x -= _x;
        y -= _y;

        x = (x - _marginX) / (_cellW + _marginX);
        y = (y - _marginY) / (_cellH + _marginY);

        return y * _cellCountY + y;

        /*
         *      x * _cellW + (x + 1) * _marginX = pX
         *            x * _cellW + x * _marginX = pX - _marginX
         *              x * (_cellW + _marginX) = pX - _marginX
         *                                    x = (pX - _marginX) / (_cellW + _marginX)
         */
    }

    public void renderRaster()
    {
        glLineWidth(1);
        glDisable(GL_TEXTURE_2D);
        for (int x = 0; x < _cellCountX; ++x)
        {
            for (int y = 0; y < _cellCountY; ++y)
            {
                Rectangle r = getCellAABB(x, y);
                glBegin(GL_LINE_LOOP);
                glVertex2i(r.getX(), r.getY());
                glVertex2i(r.getX() + r.getWidth(), r.getY());
                glVertex2i(r.getX() + r.getWidth(), r.getY() + r.getHeight());
                glVertex2i(r.getX(), r.getY() + r.getHeight());
                glEnd();
            }
        }
    }

    @Override
    public int getCellWidth()
    {
        return _cellW;
    }

    @Override
    public int getCellHeight()
    {
        return _cellH;
    }

    @Override
    public int getCellCount()
    {
        return _cellCountX * _cellCountY;
    }
}
