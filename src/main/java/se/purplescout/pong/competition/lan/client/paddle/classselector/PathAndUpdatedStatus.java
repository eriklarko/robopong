package se.purplescout.pong.competition.lan.client.paddle.classselector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

class PathAndUpdatedStatus {

    private final Path path;
    private final String hash;
    private final boolean isUpdated;

    public PathAndUpdatedStatus(Path path, String previousHash) {
        this.path = path;

        hash = calculateHash();
        if (previousHash == null || hash == null) {
            this.isUpdated = false;
        } else {
            this.isUpdated = !Objects.equals(previousHash, calculateHash());
        }
        //System.out.println(path + ": " + previousHash + " -> " + hash + " -> updated " + isUpdated);
    }

    public Path getPath() {
        return path;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String getHash() {
        return hash;
    }

    private String calculateHash() {
        String calculatedHash = null;
        try {
            byte[] fileContents = Files.readAllBytes(path);
            calculatedHash = hash(fileContents, "SHA-1");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Unable to calculate hash for " + path + ". " + e.getMessage());
        }

        return calculatedHash;
    }

    private static String hash(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = digest.digest(data);

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            if (algorithm.equals("MD5")) {
                throw ex;
            } else {
                return hash(data, "MD5");
            }
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuffer.toString();
    }
}
