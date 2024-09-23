package de.carahoff.streamdeck.device;

import java.awt.Image;

import de.carahoff.streamdeck.event.KeyListener;

public interface StreamDeck extends AutoCloseable {


    /**
     * Returns the number of keys the StreamDeck has
     * 
     * @return number of keys
     */
    public int getKeys();

    /**
     * Resets the StreamDeck to its initial state.
     */
    public void reset();

    /**
     * Adds a KeyListener to the StreamDeck. The listener will be informed of KeyEvents
     * 
     * @param listener
     *            that is to be added
     */
    public void addKeyListener(KeyListener listener);

    /**
     * Removes a KeyListener from the StreamDeck.
     * 
     * @param listener
     *            that is to be removed
     */
    public void removeKeyListener(KeyListener listener);

    /**
     * Clears the image of the key at the given keyIndex.
     * 
     * @param keyIndex
     *            of key that is to be cleared
     */
    public void clear(int keyIndex);

    /**
     * Clears the images of all keys.
     */
    public void clear();

    /**
     * Sets the image of the key at the given keyIndex to the given Image.
     * 
     * @param keyIndex
     *            of key that is to be set
     * @param img
     *            that is to be set
     */
    public void setImage(int keyIndex, Image img);

    /**
     * Sets all keys to display the given Image.
     * 
     * @param img
     *            that is to be set
     */
    public void setImage(Image img);

    /**
     * Sets the brightness of the StreamDeck to the given percentage from 0 - 100 %
     * 
     * @param percentBrightness
     *            brightness in percent
     */
    public void setBrightness(int percentBrightness);

}
