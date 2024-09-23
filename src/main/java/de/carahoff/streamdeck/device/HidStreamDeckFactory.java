package de.carahoff.streamdeck.device;

import org.hid4java.HidDevice;

public interface HidStreamDeckFactory {

    /**
     * Returns the VendorId for the StreamDeck that can be created by the StreamDeckFactory
     * 
     * @return VendorId of the StreamDeck
     */
    public int getVendorId();

    /**
     * Returns the ProductId for the StreamDeck that can be created by the StreamDeckFactory
     * 
     * @return ProductId of the StreamDeck
     */
    public int getProductId();

    /**
     * Returns true if the given vendorId and productId match with the vendorId and productId
     * for the StreamDeck that can be created by the StreamDeckFactory
     * 
     * @param vendorId
     *            of the StreamDeck
     * @param productId
     *            of the StreamDeck
     * @return
     */
    public boolean recognize(int vendorId, int productId);

    /**
     * Creates a HidStreamDeckInfo for the given device and the StreamDeckFactory that is used using the device.
     * 
     * @param device
     *            HidDevice representation of a StreamDeck
     * @return HidStreamDeckInfo for device
     */
    public HidStreamDeckInfo createStreamDeckInfo(HidDevice device);

    /**
     * Calls the open method on the given device and initializes a StreamDeck.
     * 
     * @param device
     *            HidDevice for the StreamDeck that is to be initialized
     * @return StreamDeck that was created
     */
    public StreamDeck openAndCreateStreamDeck(HidDevice device);
}
