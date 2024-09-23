package de.carahoff.streamdeck.device;

import java.awt.Image;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.carahoff.streamdeck.event.KeyEvent;
import de.carahoff.streamdeck.event.KeyListener;

public class AwareStreamDeck implements StreamDeck {

    private StreamDeck attachedDeck;
    private Map<Integer, Image> keyImageMap;

    private final List<KeyListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final long FADE_DELAY_MS = 1000 / 30;
    private int currentBrightness;

    private boolean asleep;
    private boolean sleepAware;
    private Timer timer;
    private Duration sleepTimeoutDuration = Duration.ofSeconds(120);
    private Duration sleepFadeDuration = Duration.ofSeconds(1);
    private int sleepBrightness = 10;
    private int beforeSleepBrightness;
    private final KeyListener sleepAwareListener = new KeyListener() {

        @Override
        public void onEvent(KeyEvent event) {
            if (asleep()) {
                if (event.getType() == KeyEvent.Type.PRESSED) {
                    return;
                }
                synchronized (this) {
                    asleep = false;
                    wake();
                }

                return;
            }
            if (isSleepAware()) {
                setSleepCountdown();
            }
            emitKeyEvent(event);
        }
    };

    public AwareStreamDeck(StreamDeck streamDeck) {
        this.attachedDeck = streamDeck;
        setBrightness(100);
        this.keyImageMap = new HashMap<>();
        for(int i =0; i< getKeys(); i++) {
            keyImageMap.put(i, null); //TODO CH: Could add method "Set to Black" to set each image to black instead of null

        }
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
        if (timer != null) {
            timer.cancel();
        }
    }
        executorService.shutdown();
        if (attachedDeck != null) {
            attachedDeck.close();
        }
    }

    @Override
    public int getKeys() {
        if (attachedDeck != null) {
            return attachedDeck.getKeys();
        }
        else {
            return keyImageMap.keySet().size();
        }
    }

    @Override
    public void reset() {
        if (attachedDeck != null) {
            attachedDeck.reset();
        }
        for (int i = 0; i < getKeys(); i++) {
            keyImageMap.put(i, null);
        }
        currentBrightness = 100;
        if (isSleepAware()) {
            if (asleep()) {
                synchronized (this) {
                    asleep = false;
                }
            }
            setSleepCountdown();

        }
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        synchronized (this.listeners) {
            if (listeners.isEmpty() && attachedDeck != null) {
                attachedDeck.addKeyListener(sleepAwareListener);
            }
            this.listeners.add(listener);
        }

    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
            if (listeners.isEmpty() && attachedDeck != null) {
                attachedDeck.removeKeyListener(sleepAwareListener);
            }
        }
    }

    @Override
    public void clear(int keyIndex) {
        if (attachedDeck != null) {
            attachedDeck.clear(keyIndex);
        }
        keyImageMap.put(keyIndex, null);
    }

    @Override
    public void clear() {
        if (attachedDeck != null) {
            attachedDeck.clear();
        }
            for (int i = 0; i < getKeys(); i++) {
                keyImageMap.put(i, null);
            }
    }

    @Override
    public void setImage(int keyIndex, Image img) {
        if (keyIndex >= getKeys() || keyIndex < 0) {
            throw new IllegalArgumentException("Key Index out of bounds");
        }
        wakeAndResetSleepCountdownt();
        synchronized (keyImageMap) {
        if (attachedDeck != null) {
            attachedDeck.setImage(keyIndex, img);
        }
        keyImageMap.put(keyIndex, img);
    }
    }

    @Override
    public void setImage(Image img) {
        wakeAndResetSleepCountdownt();
        synchronized (keyImageMap) {

        if (attachedDeck != null) {
            attachedDeck.setImage(img);
        }
        for (int i = 0; i < getKeys(); i++) {
            keyImageMap.put(i, img);
        }
    }
    }

    @Override
    public void setBrightness(int percentBrightness) {
        wakeAndResetSleepCountdownt();
        if (percentBrightness > 100) {
            percentBrightness = 100;
        }
        if (percentBrightness < 0) {
            percentBrightness = 0;
        }
        synchronized (this) {
        if (attachedDeck != null) {
            attachedDeck.setBrightness(percentBrightness);
        }
        currentBrightness = percentBrightness;

    }
    }

    private void setSleepBrightness(int percentBrightness) {
        if (percentBrightness > 100) {
            percentBrightness = 100;
        }
        if (percentBrightness < 0) {
            percentBrightness = 0;
        }
        synchronized (this) {
        if (attachedDeck != null) {
            attachedDeck.setBrightness(percentBrightness);
        }
        currentBrightness = percentBrightness;

    }
}

    public boolean isSleepAware() {
        synchronized (this) {
            return sleepAware;
        }
    }


    public void setSleepAware(boolean sleepAware) {
        synchronized (this) {
            if (sleepAware) {
                setSleepCountdown();
            }
            else {
                if (timer != null) {
                    timer.cancel();
                }
                asleep = false;
            }
            this.sleepAware = sleepAware;
        }
    }

    public void sleep() {
        synchronized (this) {
            asleep = true;
            beforeSleepBrightness = currentBrightness;
            if (timer != null) {
                timer.cancel();
            }
            fade(sleepBrightness, sleepFadeDuration);
        }

    }

    private void setSleepCountdown() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new SleepTask(), sleepTimeoutDuration.toMillis());
        }
    }

    public void setSleepTimeout(Duration duration) {
        //TODO CH: think about plausibility checks for sleepTimeout duration?
        synchronized (this) {
        this.sleepTimeoutDuration = duration;
        if (isSleepAware() && !asleep()) {
            setSleepCountdown();
        }
    }

    }

    public boolean asleep() {
        synchronized (this) {
            return asleep;
        }
    }

    public void wake() {
        //TODO CH: anything else to do on wake? 
        synchronized (this) {
            this.setSleepBrightness(beforeSleepBrightness);
            asleep = false;
        }
    }

    private void wakeAndResetSleepCountdownt() {
        if (isSleepAware()) {
            setSleepCountdown();
            if (asleep()) {
                wake();
            }
        }
    }

    public void fade(int percentEndBrightness, Duration duration) {
        if (percentEndBrightness > 100) {
            percentEndBrightness = 100;
        }
        if (percentEndBrightness < 0) {
            percentEndBrightness = 0;
        }
        if (percentEndBrightness == currentBrightness) {
            return;
        }

        double step = (double) (percentEndBrightness - currentBrightness) / duration.toMillis() * FADE_DELAY_MS;
        if (Double.isInfinite(step)) {
            return;
        }

        for (double current = currentBrightness;; current += step) {
            if (!((currentBrightness < percentEndBrightness && current < percentEndBrightness) || (currentBrightness > percentEndBrightness && current > percentEndBrightness))) {
                break;
            }
            if (asleep()) {
                setSleepBrightness((int) current);
            }
            else {
                setBrightness((int) current);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(FADE_DELAY_MS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    }

    public void setSleepFadeDuration(Duration duration) {
        //TODO CH: think about plausibility checks for sleepFadeDuration
        this.sleepFadeDuration = duration;

    }

    public void setSleepBrightnessValue(int percentBrightness) {
        if (percentBrightness > 100) {
            percentBrightness = 100;
        }
        if (percentBrightness < 0) {
            percentBrightness = 0;
        }
        synchronized (this) {
            this.sleepBrightness = percentBrightness;
        }
    }

    private class SleepTask extends TimerTask {
        @Override
        public void run() {
            sleep();
        }
    }

    private void emitKeyEvent(KeyEvent event) {
        for (KeyListener listener : listeners) {
            executorService.submit(() -> listener.onEvent(event));
        }
    }

    public void attachDeck(StreamDeck streamDeck) {
        synchronized (this) {
        if (attachedDeck != null) {
            try {
                attachedDeck.close();
            }
            catch (Exception e) {
                System.out.println("Unable to close previously attached Deck");
            }
        }

        if (streamDeck.getKeys() != getKeys()) {
            throw new IllegalArgumentException(String.format("Supplied Deck has different amount of Keys, expected %d Keys", getKeys()));
        }
        for (int i = 0; i < getKeys(); i++) {
            if (keyImageMap.get(i) == null) {
                streamDeck.clear(i);
            }
            else {
                streamDeck.setImage(i, keyImageMap.get(i));
            }
        }

        streamDeck.addKeyListener(sleepAwareListener);
        if (isSleepAware()) {
            wakeAndResetSleepCountdownt();//Set new timer if deck is sleepaware
        }
        else {
            streamDeck.setBrightness(currentBrightness);
        }

        this.attachedDeck = streamDeck;

        //TODO CH: Catch mismatch in pixel size, don't attach when mismatching?-> Problem pixel size not mandatory for StreamDeck (Multiple Key sizes for newer Decks)
    }
    }

    public void detachDeck() {
        synchronized (this) {
        if (attachedDeck != null) {
            try {
                attachedDeck.close();
            }
            catch (Exception e) {
                System.out.println("Unable to call close on detached deck");
            }
        }
        attachedDeck = null;
        if (sleepAware) { //SleepAwareneness is kept on detachment but timer is canceled until reatachment
            synchronized (this) {
                if (timer != null) {
                    timer.cancel();
                }
                if (!asleep()) {
                    sleep();
                }
            }
        }
    }

    }

    public boolean hasAttachedDeck() {
        synchronized (this) {
            if (attachedDeck != null) {
                return true;
            }
            return false;
        }
    }

    public StreamDeck getAttachedDeck() {
        return attachedDeck;
    }

}
