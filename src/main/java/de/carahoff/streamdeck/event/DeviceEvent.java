package de.carahoff.streamdeck.event;

import org.hid4java.HidDevice;

public class DeviceEvent {

    private final Type type;
    private final HidDevice hidDevice;

    public DeviceEvent(Type type, HidDevice hidDevice) {
        this.hidDevice = hidDevice;
        this.type = type;
    }

    public enum Type {
        ATTACHED, DETACHED, FAILURE;
    }

    public HidDevice getHidDevice() {
        return hidDevice;
    }

    public Type getType() {
        return type;
    }
}
