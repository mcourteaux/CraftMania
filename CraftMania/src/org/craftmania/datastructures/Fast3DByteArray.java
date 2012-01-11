package org.craftmania.datastructures;


/**
 *
 * @author martijncourteaux
 */
public class Fast3DByteArray
{
    private final byte _array[];
    private final int _lX, _lY, _lZ;
    private final int _size;

    /**
     * Init. a new 3D array with the given dimensions.
     */
    public Fast3DByteArray(int x, int y, int z) {
        _lX = x;
        _lY = y;
        _lZ = z;

        _size = _lX * _lY * _lZ;
        _array = new byte[_size];
    }

    /**
     * Returns the byte value at the given position.
     */
    public byte get(int x, int y, int z) {

        int pos = (x * _lX * _lY) + (y * _lX) + z;

        if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
            return 0;

        return _array[pos];
    }

    /**
     * Sets the byte value for the given position.
     */
    public void set(int x, int y, int z, byte b) {
        int pos = (x * _lX * _lY) + (y * _lX) + z;

        if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
            return;

        _array[pos] = b;
    }

    /**
     * Returns the raw byte at the given index.
     */
    public byte getRawByte(int i) {
        return _array[i];
    }

    /**
     * Sets the raw byte for the given index.
     */
    public void setRawByte(int i, byte b) {
        _array[i] = b;
    }

    /**
     * Returns the size of this array.
     */
    public int size() {
        return _size;
    }
}
