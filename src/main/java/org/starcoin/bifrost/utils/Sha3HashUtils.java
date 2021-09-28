package org.starcoin.bifrost.utils;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class Sha3HashUtils {

    public static String hashStarcoinSignedUserTransaction(byte[] signedMessage) {
        byte[] bytesForHash = com.google.common.primitives.Bytes
                .concat(hashWithStarcoinPrefix("SignedUserTransaction"), signedMessage);
        return HexUtils.toHexString(new SHA3.Digest256().digest(bytesForHash));
    }

    public static byte[] hashWithStarcoinPrefix(String text) {
        return hash("STARCOIN::".getBytes(), text.getBytes());
    }

    public static byte[] hash(byte[] prefix, byte[] bytes) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        digestSHA3.update(prefix);
        digestSHA3.update(bytes);
        return digestSHA3.digest();
    }
}
