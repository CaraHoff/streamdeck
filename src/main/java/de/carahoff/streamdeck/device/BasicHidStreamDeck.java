package de.carahoff.streamdeck.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hid4java.HidDevice;

import de.carahoff.streamdeck.event.KeyEvent;
import de.carahoff.streamdeck.event.KeyListener;
import de.carahoff.streamdeck.event.KeyEvent.Type;
import de.carahoff.streamdeck.util.ImageData;

public abstract class BasicHidStreamDeck implements StreamDeck {
    private final HidDevice device;
    private final List<KeyListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Thread keyController;



    public BasicHidStreamDeck(HidDevice device) {
        if (!device.isOpen()) {
            device.open();
        }

        this.device = device;
        this.keyController = new Thread(new KeyController());
        this.keyController.start();
        setBrightness(100);
    }

    public abstract int getColumns();

    public abstract int getRows();

    public abstract int getPixels();

    protected abstract int getDPI();

    protected abstract int getPadding();

    protected abstract int getImagePageSize();

    protected abstract int getImagePageHeaderSize();

    protected abstract int getFeatureReportSize();

    protected abstract int getFirmwareOffset();

    protected abstract int getKeyStateOffset();

    protected abstract byte[] getGetFirmwareCommand();

    protected abstract byte[] getSetBrightnessCommand();

    protected abstract byte[] getResetCommand();

    protected abstract BitSet getKeyStates();

    protected abstract byte[] imagePageHeader(int pageIndex, int keyIndex, int payloadLength, boolean lastPage);

    protected abstract int translateKeyIndex(int index, int columns);

    protected abstract Image flipImage(Image img);

    protected abstract byte[] toImageFormat(Image img);

    @Override
    public void reset() {
        sendFeatureReport(getResetCommand());
    }

    @Override
    public void close() {
        executorService.shutdown();
        if (keyController != null) {

            this.keyController.interrupt();
            try {
                this.keyController.join();
            }
            catch (InterruptedException e) {
                System.out.println("Unable to stop keyController thread");
            }
        }
        this.setBrightness(0);
        this.clear();
        device.close();
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }

    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }

    }

    @Override
    public void clear(int keyIndex) {
        int pixels = getPixels();
        BufferedImage img = new BufferedImage(pixels, pixels, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, pixels, pixels);
        g2d.dispose();

        setImage(keyIndex, img);
    }

    @Override
    public void clear() {
        int pixels = getPixels();
        BufferedImage img = new BufferedImage(pixels, pixels, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, pixels, pixels);
        g2d.dispose();

        setImage(img);
    }

    @Override
    public void setImage(int keyIndex, Image img) {
        if (keyIndex >= getKeys() || keyIndex < 0) {
            throw new IllegalArgumentException("Key Index out of bounds");
        }
        if (img.getHeight(null) > getPixels() || img.getWidth(null) > getPixels()) {
            throw new IllegalArgumentException(String.format("Supplied image has wrong dimensions, expected %dx%d pixels", getPixels(), getPixels()));
            //TODO CH: could add/ offer resize Method -> Could lead to pixelated images
            //could offer resize Method that only downsizes lager images and centers image over black backround for smaller images
        }

        Image flippedImg = flipImage(img);

        byte[] imageBytes = toImageFormat(flippedImg);
        ImageData imageData = new ImageData(imageBytes, getImagePageSize() - getImagePageHeaderSize());
        byte[] data = new byte[getImagePageSize()];

        int page = 0;
        int pageCount = imageData.pageCount();
        boolean lastPage = false;
        while (page < pageCount) {
            lastPage = (page == pageCount - 1);

            byte[] payload = imageData.page(page);
            byte[] header = imagePageHeader(page, translateKeyIndex(keyIndex, getColumns()), payload.length, lastPage);

            System.arraycopy(header, 0, data, 0, header.length);
            System.arraycopy(payload, 0, data, header.length, payload.length);

            write(data);
            page++;
        }
    }

    @Override
    public void setImage(Image img) {
        for (int i = 0; i < getKeys(); i++) {
            setImage(i, img);
        }
    }

    @Override
    public void setBrightness(int percentBrightness) {
        if (percentBrightness > 100) {
            percentBrightness = 100;
        }
        if (percentBrightness < 0) {
            percentBrightness = 0;
        }
        byte[] report = new byte[getSetBrightnessCommand().length + 1];
        System.arraycopy(getSetBrightnessCommand(), 0, report, 0, getSetBrightnessCommand().length);
        report[report.length - 1] = (byte) percentBrightness;
        sendFeatureReport(report);
    }

    public HidDevice getDevice() {
        return this.device;
    }

    public String getFirmwareVersion() {
        byte[] b = getFeatureReport(getGetFirmwareCommand());
        return new String(Arrays.copyOfRange(b, getFirmwareOffset(), b.length)).trim();
    }


    private byte[] getFeatureReport(byte[] payload) {
        byte reportId = payload[0];
        byte[] b = new byte[getFeatureReportSize() - 1];

        if (payload.length > 1) {
            System.arraycopy(payload, 1, b, 0, payload.length - 1);
        }
        device.getFeatureReport(b, reportId);
        byte[] res = new byte[getFeatureReportSize()];
        res[0] = reportId;
        System.arraycopy(b, 0, res, 1, b.length);
        return res;
    }

    private void sendFeatureReport(byte[] payload) {
        byte reportId = payload[0];
        byte[] b = new byte[getFeatureReportSize() - 1];
        if (payload.length > 1) {
            System.arraycopy(payload, 1, b, 0, payload.length - 1);
        }
        device.sendFeatureReport(b, reportId);
    }

    private void write(byte[] data) {
        byte reportId = data[0];
        byte[] b = new byte[data.length - 1];
        if (data.length > 1) {
            System.arraycopy(data, 1, b, 0, data.length - 1);
        }
        device.write(b, b.length, reportId);
    }

    private void emitKeyEvent(KeyEvent event) {
        for (KeyListener listener : listeners) {
            executorService.submit(() -> listener.onEvent(event));
        }
    }

    private BitSet parseKeyStates(byte[] dataReceived) {
        BitSet keyStates = new BitSet(getKeys());
        for (int i = getKeyStateOffset(); i < dataReceived.length; i++) {
            keyStates.set(translateKeyIndex(i - getKeyStateOffset(), getColumns()), (dataReceived[i] == 1));
        }

        return keyStates;
    }

    /**
     * Blocks until event is received
     * 
     * @return
     */
    private BitSet readKeyStates() {
        byte[] keyBuffer = new byte[getKeyStateOffset() + getKeys()];
        int res;
        while (((res = device.read(keyBuffer, 2)) == 0) && !this.keyController.isInterrupted())
            ;
        if (res > 0) {
            return parseKeyStates(keyBuffer);
        }
        else {
            return null;
        }
    }

    private class KeyController implements Runnable {

        @Override
        public void run() {
            int keyCount = getKeys();
            BitSet keyStates = new BitSet(keyCount);
            while (!Thread.currentThread().isInterrupted()) {
                BitSet newKeyStates = readKeyStates();
                if (newKeyStates == null || Thread.currentThread().isInterrupted()) {
                    return;
                }
                for (int i = 0; i < keyCount; i++) {
                    boolean oldKeyState = keyStates.get(i);
                    boolean newKeyState = newKeyStates.get(i);
                    if (oldKeyState == newKeyState) {
                        continue;
                    }
                    KeyEvent keyEvent;
                    if (newKeyState) {
                        keyEvent = new KeyEvent(i, Type.PRESSED);
                        //key was pressed
                    }
                    else {
                        keyEvent = new KeyEvent(i, Type.RELEASED);
                        //key was released
                    }
                        emitKeyEvent(keyEvent);
                }
                keyStates = newKeyStates;
            }
        }
    }
}
