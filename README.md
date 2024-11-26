# StreamDeck Java Library

`streamdeck` is a Java library designed to interact with Stream Decks, specifically the Elgato Mk2 Stream Deck. This library provides a native Java implementation, eliminating the need for a separate Stream Deck application on the client side.

## Features

- Native Java support for Elgato Mk2 Stream Deck
- Handle device attachment and detachment
- Control key images, brightness, and sleep state
- Add custom key listeners to react to button presses
- No need for external Stream Deck software

## Acknowledgments
This library uses hid4java and jna (see [pom.xlm](./pom.xml) for the detailed version). 
It draws heavy inspiration from these open-source projects:

- [StreamDeckCore](https://github.com/VVEIRD/StreamDeckCore/tree/master?tab=readme-ov-file) by VVEIRD
- [streamdeck](https://github.com/muesli/streamdeck/tree/master?tab=readme-ov-file) by muesli

I would also like to give credit to NEXUS / CHILI GmbH, who commissioned the development of this library and graciously allowed its publication as open source.

## Getting Started

### Using the Library in Your Project

To use the `streamdeck` library in your project, you can include it as a Maven dependency. Add the following to your `pom.xml` file:

```xml
<dependency>
    <groupId>de.carahoff</groupId>
    <artifactId>streamdeck</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Example Code

Here’s a quick example of how to use the library to connect with a Stream Deck, manage key events, and handle device attachment/detachment:

```java
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import de.carahoff.streamdeck.device.*;
import de.carahoff.streamdeck.elgato.*;
import de.carahoff.streamdeck.event.*;

public class Application {

    public static void main(String[] args) throws Exception {

        // Setup StreamDeck factories
        List<HidStreamDeckFactory> factories = new ArrayList<>();
        StreamDeckMK2Factory mk2Factory = new StreamDeckMK2Factory();
        factories.add(mk2Factory);
        HidStreamDecks.setFactories(factories);

        // Identify attached Stream Decks
        List<HidStreamDeckInfo> deckInfos = HidStreamDecks.identify();
        if (deckInfos.isEmpty()) {
            System.out.println("No stream deck found");
            return;
        }

        // Initialize StreamDeckMK2
        StreamDeckMK2 deck;
        if (deckInfos.get(0).getFactory().equals(mk2Factory)) {
            deck = (StreamDeckMK2) deckInfos.get(0).createStreamDeck();
        } else {
            System.out.println("No StreamDeckMK2 found");
            return;
        }

        // Wrap in an AwareStreamDeck for handling attachment/detachment
        AwareStreamDeck awareDeck = new AwareStreamDeck(deck);

        // Add device listener for attachment/detachment events
        DeviceListener deviceListener = new DeviceListener() {

            @Override
            public void onEvent(DeviceEvent event) {
                if (event.getType() == DeviceEvent.Type.ATTACHED) {
                    System.out.println("Device attached");
                    if (awareDeck.hasAttachedDeck()) return;

                    HidStreamDeckInfo newDeckInfo = HidStreamDecks.identifyDevice(event.getHidDevice());
                    if (newDeckInfo != null) {
                        StreamDeckMK2 newStreamDeck = (StreamDeckMK2) newDeckInfo.createStreamDeck();
                        if (newStreamDeck != null) {
                            awareDeck.attachDeck(newStreamDeck);
                        }
                    }
                }

                if (event.getType() == DeviceEvent.Type.DETACHED) {
                    System.out.println("Device detached");
                    awareDeck.detachDeck();
                }
            }
        };

        HidStreamDecks.addDeviceListener(deviceListener);

        // Set up key images, brightness, and key listeners
        try {
            awareDeck.clear();
            BufferedImage img1 = ImageIO.read(new File("resources", "icon1.png"));
            awareDeck.setImage(0, img1);
            BufferedImage img2 = ImageIO.read(new File("resources", "icon2.png"));
            awareDeck.setImage(1, img2);
            awareDeck.setBrightness(50);

            awareDeck.addKeyListener(new KeyListener() {
                @Override
                public void onEvent(KeyEvent event) {
                    System.out.println("Received event for key " + event.getIndex() + ", type " + event.getType());
                }
            });

            awareDeck.setSleepAware(true);

            // Keep application alive to test attachment/detachment
            while (true) {
                System.out.println("ALIVE");
                Thread.sleep(1000);
            }
        } finally {
            awareDeck.close();
        }
    }
}
```

### Key Components

- **AwareStreamDeck**: This class extends `BasicHidStreamDeck` and adds the ability to handle device attachment and detachment events. It also provides features like automatic reconnection and sleep awareness, ensuring that the Stream Deck stays active or goes to sleep as needed.
  
- **BasicHidStreamDeck**: A base class that handles core interactions with the Stream Deck hardware, such as setting key images and responding to key events. It does not automatically manage device connection changes or sleep states, which makes `AwareStreamDeck` more powerful in dynamic environments.

### Example Features:

- **Key Image**: You can set images to specific keys by specifying the key index and loading a `BufferedImage`.
- **Brightness Control**: Adjust the Stream Deck's brightness with `setBrightness()`.
- **Key Listener**: Attach a `KeyListener` to respond to key presses or releases.
- **Sleep Mode**: Use `setSleepAware()` to enable or disable automatic sleep when the deck is idle.

### Attachment and Detachment Handling

`AwareStreamDeck` can automatically handle situations where the Stream Deck is unplugged or plugged back in, making it resilient in environments where the device might be temporarily disconnected. If a device is re-attached, it tries to reinitialize it seamlessly.
