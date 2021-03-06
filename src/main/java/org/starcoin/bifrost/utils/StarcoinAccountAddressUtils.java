package org.starcoin.bifrost.utils;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.types.Ed25519PublicKey;
import org.starcoin.utils.HexUtils;
import org.starcoin.utils.SignatureUtils;

import java.util.Arrays;
import java.util.Collections;

public class StarcoinAccountAddressUtils {
    public static final byte SCHEME_ID_ED25519 = 0;
    public static final byte SCHEME_ID_MULTIED25519 = 1;
    private static final int ACCOUNT_ADDRESS_LENGTH = 16;

    public static String trimAddress(String address) {
        String a = address.startsWith("0x") ? address.substring(2) : address;
        if (a.length() > ACCOUNT_ADDRESS_LENGTH * 2) {
            String removed = a.substring(0, a.length() - ACCOUNT_ADDRESS_LENGTH * 2);
            if (!String.join("", Collections.nCopies(removed.length(), "0")).equals(removed)) {
                throw new RuntimeException("CANNOT trim " + address);
            }
            return (address.startsWith("0x") ? "0x" : "") + a.substring(removed.length());
        }
        return address;
    }

    public static String getAddressFromPrivateKey(Ed25519PrivateKey privateKey) {
        Ed25519PublicKey publicKey = SignatureUtils.getPublicKey(privateKey);
        return getAddressFromPublicKey(publicKey);
    }

    public static String getAddressFromPublicKey(Ed25519PublicKey publicKey) {
        byte[] rawBytes = com.google.common.primitives.Bytes.concat(publicKey.value.content(), new byte[]{SCHEME_ID_ED25519});
        byte[] digestedBytes = new SHA3.Digest256().digest(rawBytes);
        byte[] addressBytes = Arrays.copyOfRange(digestedBytes, digestedBytes.length - ACCOUNT_ADDRESS_LENGTH, digestedBytes.length);
        return HexUtils.toHexString(addressBytes);
    }
}
