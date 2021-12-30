package com.mergebase.log4j.allowlist;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mergebase.log4j.utils.Bytes.digest;

/**
 * Hash (SHA256) based Allowlists for known artifacts that are not affected.
 */
public final class Allowlists {

    private static final Logger logger = Logger.getLogger(Allowlists.class.getSimpleName());
    private static final Allowlists INSTANCE = new Allowlists();
    public static Allowlists getInstance() {
        return INSTANCE;
    }

    private final Map<String, Set<String>> allowlists = new HashMap<>();

    public Allowlists() {
        Set<String> checksums = loadAllowlist("log4j-over-slf4j-checksums-unique.txt");
        Set<String> checksumsCompat = loadAllowlist("log4j-1.2-api-checksums-unique.txt");
        allowlists.put("log4j-over-slf4j", checksums);
        allowlists.put("log4j-1.2-api", checksumsCompat);
    }

    public AllowlistResult isOnAllowlist(byte[] filebytes) {
        if(filebytes == null) {
            return AllowlistResult.notFound();
        }
        String hash = digest(filebytes);
        if(hash.isEmpty()) {
            return AllowlistResult.notFound();
        }

        for (Map.Entry<String, Set<String>> allowlist : allowlists.entrySet()) {
            if(allowlist.getValue().contains(hash)) {
                return new AllowlistResult(true, allowlist.getKey());
            }
        }
        return AllowlistResult.notFound();
    }

    private Set<String> loadAllowlist(String name) {
        FileSystem fs = null;
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
            if(resource == null) {
                logger.warning("Allowlist " + name + " could not be found. Continuing without.");
                return new HashSet<>();
            }
            // Why in the fuck is this so messed up?
            String resourceUri = resource.toURI().toString();
            Path path;
            if(resourceUri.startsWith("jar:")) {
                // We need to create a FileSystem before we can read from within our JarFile
                final String[] array = resourceUri.split("!");
                fs = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>());
                path = fs.getPath(array[1]);
            } else {
                // When runnning in a test case we're reading from file:/ instead of jar:/ so we don't need to
                // create a FileSystem
                path = Paths.get(resource.toURI());
            }
            List<String> strings = Files.readAllLines(path);
            return new HashSet<>(strings);
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.FINE, e.getMessage(), e);
            logger.warning("Failed to load Allowlist " + name + ". Continuing without allowlist.");
            return new HashSet<>();
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                // nothing to be done here...
            }
        }
    }

}
