package org.craftmania.items;


import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex3f;

import org.craftmania.blocks.Block;
import org.craftmania.blocks.BlockManager;
import org.craftmania.blocks.BlockType;
import org.craftmania.blocks.BlockType.BlockClass;
import org.craftmania.game.TextureStorage;
import org.craftmania.math.Vec2i;

/**
 *
 * @author martijncourteaux
 */
public abstract class Tool extends TexturedItem
{

    public enum Material
    {

        WOOD, STONE, IRON, DIAMOND, GOLD
    }
    private BlockClass _blockClass;
    private float _health;
    private Material _material;

    protected Tool(String name, BlockClass blockClass, Material material, Vec2i texturePosition, float animationSpeed)
    {
        super(name, animationSpeed, TextureStorage.getTexture("items"), texturePosition);

        this._blockClass = blockClass;
        this._material = material;
    }

    @Override
    public void update()
    {
        // Do nothing
    }

    @Override
    public float calcDamageFactorToBlock(Block block)
    {
        byte blockTypeByte = block.getBlockType().getID();
        BlockType bt = BlockManager.getInstance().getBlockType(blockTypeByte);

        if (bt.getBlockClass() == getBlockClass())
        {
            return (_material.ordinal() / 4.0f) + 4.0f;
        }
        return 1.2f;
    }

    @Override
    public float calcDamageInflictedByBlock(Block block)
    {
        byte blockTypeByte = block.getBlockType().getID();
        BlockType bt = BlockManager.getInstance().getBlockType(blockTypeByte);
        float materialResistance = (0.2f / (float) Math.pow(_material.ordinal(), 1.2d));
        if (bt.getBlockClass() == getBlockClass())
        {
            return materialResistance * (0.05f * bt.getResistance());
        }
        return bt.getResistance() * materialResistance;
    }

    @Override
    public void renderHoldableObject()
    {
        _texture.bind();

        float hw = 0.1f;
        float hh = 0.1f;


        glColor3f(0.5f, 0.5f, 0.5f);
        /* Render the texture */
        for (float i = 0.0f; i < 0.02f; i += 0.002f)
        {
            if (i > 0.016f)
            {
                glColor3f(1.0f, 1.0f, 1.0f);
            }
            glBegin(GL_QUADS);
            glTexCoord2f(_texPosUpLeft.x(), _texPosUpLeft.y());
            glVertex3f(-hw, hh, 0.0f + i);
            glTexCoord2f(_texPosDownRight.x(), _texPosUpLeft.y());
            glVertex3f(hw, hh, 0.0f + i);
            glTexCoord2f(_texPosDownRight.x(), _texPosDownRight.y());
            glVertex3f(hw, -hh, 0.0f + i);
            glTexCoord2f(_texPosUpLeft.x(), _texPosDownRight.y());
            glVertex3f(-hw, -hh, 0.0f + i);
            glEnd();
        }

    }

    public BlockClass getBlockClass()
    {
        return _blockClass;
    }

    @Override
    public boolean isStackable()
    {
        return false;
    }

    @Override
    public void inflictDamage(float toolDamage)
    {
        this._health -= toolDamage;
    }

    @Override
    public float getHealth()
    {
        return _health;
    }
}
