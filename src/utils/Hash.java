package utils;

import configs.Codes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {
    private Hash() {

    }

    public static String computeHashOfString(String string, String algorithm) {
        StringBuilder hashAsHex = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hash = messageDigest.digest(string.getBytes(StandardCharsets.UTF_8));

            hashAsHex = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hashAsHex.append('0');
                hashAsHex.append(hex);
            }
        } catch (NoSuchAlgorithmException exception) {
            System.err.println("Error: the specified hashing algorithm (" + algorithm + ")" + "is not implemented");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return new String(hashAsHex);
    }
}
