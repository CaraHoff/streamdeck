package de.carahoff.streamdeck.elgato;

import java.awt.Image;
import java.util.BitSet;

import org.hid4java.HidDevice;

import de.carahoff.streamdeck.device.BasicHidStreamDeck;
import de.carahoff.streamdeck.util.ImageUtils;

public class StreamDeckMK2 extends BasicHidStreamDeck {

    private static final int COLUMNS = 5;
    private static final int ROWS = 3;
    private static final int KEYS = 15;
    private static final int PIXELS = 72;
    private static final int DPI = 124;
    private static final int PADDING = 16;
    private static final int IMAGE_PAGE_SIZE = 1024;
    private static final int IMAGE_PAGE_HEADER_SIZE = 8;

    private static final int FEATURE_REPORT_SIZE = 32;
    private static final int FIRMWARE_OFFSET = 6;

    private static final int KEY_STATE_OFFSET = 4;

    private static final byte[] GET_FIRMWARE_COMMAND = new byte[] { 0x05 };

    private static final byte[] SET_BRIGHTNESS_COMMAND = new byte[] { 0x03, 0x08 };

    private static final byte[] RESET_COMMAND = new byte[] { 0x03, 0x02 };

    private static final BitSet KEY_STATES = new BitSet(KEYS);

    public StreamDeckMK2(HidDevice device) {
        super(device);
    }

    @Override
    public int getColumns() {
        return COLUMNS;
    }

    @Override
    public int getRows() {
        return ROWS;
    }

    @Override
    public int getKeys() {
        return KEYS;
    }

    @Override
    public int getPixels() {
        return PIXELS;
    }

    @Override
    protected int getDPI() {
        return DPI;
    }

    @Override
    protected int getPadding() {
        return PADDING;
    }

    @Override
    protected int getImagePageSize() {
        return IMAGE_PAGE_SIZE;
    }

    @Override
    protected int getImagePageHeaderSize() {
        return IMAGE_PAGE_HEADER_SIZE;
    }

    @Override
    protected int getFeatureReportSize() {
        return FEATURE_REPORT_SIZE;
    }

    @Override
    protected int getFirmwareOffset() {
        return FIRMWARE_OFFSET;
    }

    @Override
    protected int getKeyStateOffset() {
        return KEY_STATE_OFFSET;
    }

    @Override
    protected byte[] getGetFirmwareCommand() {
        return GET_FIRMWARE_COMMAND;
    }

    @Override
    protected byte[] getSetBrightnessCommand() {
        return SET_BRIGHTNESS_COMMAND;
    }

    @Override
    protected byte[] getResetCommand() {
        return RESET_COMMAND;
    }

    @Override
    protected BitSet getKeyStates() {
        return KEY_STATES;
    }

    @Override
    protected byte[] imagePageHeader(int pageIndex, int keyIndex, int payloadLength, boolean lastPage) {
        byte lastPageByte = (byte) (lastPage ? 1 : 0);
        return new byte[] { 0x02, 0x07, (byte) keyIndex, lastPageByte,
                (byte) (payloadLength), (byte) (payloadLength >> 8),
                (byte) (pageIndex), (byte) (pageIndex >> 8) };
    }

    @Override
    protected int translateKeyIndex(int index, int columns) {
        return index;
    }

    @Override
    protected Image flipImage(Image img) {
        return ImageUtils.flipHorizontallyAndVertically(img);
    }

    @Override
    protected byte[] toImageFormat(Image img) {
        return ImageUtils.convertToJPGByteArray(img);
    }
}
