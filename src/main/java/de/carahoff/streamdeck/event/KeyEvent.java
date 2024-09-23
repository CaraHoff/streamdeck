package de.carahoff.streamdeck.event;


public class KeyEvent {
    private final int index;
    private final Type type;

    public KeyEvent(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    public enum Type {
        PRESSED, RELEASED;
    }

    public int getIndex() {
        return index;
    }

    public Type getType() {
        return type;
    }

}
