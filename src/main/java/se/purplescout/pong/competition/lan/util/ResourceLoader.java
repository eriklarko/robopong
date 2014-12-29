package se.purplescout.pong.competition.lan.util;

import java.net.URL;
import java.util.MissingResourceException;

/**
 * Helper methods to load a resource on the class path.
 */
public final class ResourceLoader {

    public static URL getResource(Class<?> clazz, String resourceLocation) {
        URL resource = clazz.getResource(resourceLocation);
        if (resource == null) {
            throw new MissingResourceException("Unable to find " + resourceLocation + " in the classpath for "
                    + clazz + " (" + clazz.getPackage() + ")", clazz.getName(), resourceLocation);
        }
        return resource;
    }

    private ResourceLoader() {
    }
}
