package Utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static byte[] hash(char[] password, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
            byte[] bytes = Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash no disponible: " + algorithm, ex);
        }
    }

    public static boolean matches(byte[] computedHash, byte[] storedHash) {
        return MessageDigest.isEqual(computedHash, storedHash);
    }
}
