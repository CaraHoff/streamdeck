package de.carahoff.streamdeck.device;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.HidServicesSpecification;
import org.hid4java.event.HidServicesEvent;

import de.carahoff.streamdeck.event.DeviceEvent;
import de.carahoff.streamdeck.event.DeviceListener;
import de.carahoff.streamdeck.event.DeviceEvent.Type;

public class HidStreamDecks {
    private static List<HidStreamDeckFactory> factories;
    private static final List<DeviceListener> listeners = new CopyOnWriteArrayList<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final HidServices hidServices;
    private static final DeviceController deviceController = new DeviceController();

    static {
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoStart(false);
        hidServices = HidManager.getHidServices(hidServicesSpecification);
        hidServices.start();//TODO CH: remove from this method and make user call hidSercices.start() explicitly?
    }

    public static void setFactories(List<HidStreamDeckFactory> addFactories) {
        factories = addFactories;
    }

    public static List<HidStreamDeckInfo> identify() {

        List<HidStreamDeckInfo> streamDeckInfos = new ArrayList<>();
        for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            for (HidStreamDeckFactory factory : factories) {
                if (factory.recognize(hidDevice.getVendorId(), hidDevice.getProductId())) {
                    streamDeckInfos.add(factory.createStreamDeckInfo(hidDevice));
                }
            }
        }
        return streamDeckInfos;
    }

    public static HidStreamDeckInfo identifyDevice(HidDevice hidDevice) {
        for (HidStreamDeckFactory factory : factories) {
            if (factory.recognize(hidDevice.getVendorId(), hidDevice.getProductId())) {
                return factory.createStreamDeckInfo(hidDevice);
            }
        }
        return null;
    }



    public static void addDeviceListener(DeviceListener listener) {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                hidServices.addHidServicesListener(deviceController);
            }
            listeners.add(listener);
        }
    }

    public static void removeDeviceListener(DeviceListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                hidServices.removeHidServicesListener(deviceController);
            }
        }
    }

    private static void emitDeviceEvent(DeviceEvent event) {
        for (DeviceListener listener : listeners) {
            executorService.submit(() -> listener.onEvent(event));
        }
    }
    
    private static class DeviceController implements HidServicesListener {
        @Override
        public void hidDeviceAttached(HidServicesEvent event) {
            DeviceEvent deviceEvent = new DeviceEvent(Type.ATTACHED, event.getHidDevice());
            emitDeviceEvent(deviceEvent);
        }

        @Override
        public void hidDeviceDetached(HidServicesEvent event) {
            DeviceEvent deviceEvent = new DeviceEvent(Type.DETACHED, event.getHidDevice());
            emitDeviceEvent(deviceEvent);
        }

        @Override
        public void hidFailure(HidServicesEvent event) {
            DeviceEvent deviceEvent = new DeviceEvent(Type.FAILURE, event.getHidDevice());
            emitDeviceEvent(deviceEvent);
        }
    }
}
