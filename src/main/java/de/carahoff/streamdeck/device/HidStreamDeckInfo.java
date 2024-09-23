package de.carahoff.streamdeck.device;

import org.hid4java.HidDevice;

public class HidStreamDeckInfo {

    private final HidDevice device;
    private final HidStreamDeckFactory factory;

    public HidStreamDeckInfo(HidDevice device, HidStreamDeckFactory factory) {
        this.device = device;
        this.factory = factory;
    }

    public HidDevice getDevice() {
        return device;
    }

    public HidStreamDeckFactory getFactory() {
        return factory;
    }

    public StreamDeck createStreamDeck() {
        return factory.openAndCreateStreamDeck(device);
    }

}
