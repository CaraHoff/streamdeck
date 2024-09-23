package de.carahoff.streamdeck.elgato;

import org.hid4java.HidDevice;

import de.carahoff.streamdeck.device.HidStreamDeckFactory;
import de.carahoff.streamdeck.device.HidStreamDeckInfo;

public class StreamDeckMK2Factory implements HidStreamDeckFactory {
    public static final int VENDOR_ID_ELGATO = 0x0fd9;
    public static final int PRODUCT_ID_STREAMDECK_MK2 = 0x0080;

    @Override
    public int getVendorId() {
        return VENDOR_ID_ELGATO;
    }

    @Override
    public int getProductId() {
        return PRODUCT_ID_STREAMDECK_MK2;
    }

    @Override
    public boolean recognize(int vendorId, int productId) {
        return (VENDOR_ID_ELGATO == vendorId && PRODUCT_ID_STREAMDECK_MK2 == productId);
    }

    @Override
    public HidStreamDeckInfo createStreamDeckInfo(HidDevice device) {
        return new HidStreamDeckInfo(device, this);
    }

    @Override
    public StreamDeckMK2 openAndCreateStreamDeck(HidDevice device) {
        device.open();
        return new StreamDeckMK2(device);
    }
}
