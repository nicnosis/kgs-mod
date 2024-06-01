/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RsaCrypto {
    public static byte[] encrypt(BigInteger expo, BigInteger modulus, byte[] data, int destLen) {
        byte[] out = new BigInteger(1, data).modPow(expo, modulus).toByteArray();
        if (data.length == destLen) {
            return out;
        }
        byte[] padded = new byte[destLen];
        if (out.length > destLen) {
            for (int i = 0; i < out.length - destLen; ++i) {
                if (out[i] == 0) continue;
                throw new IllegalArgumentException("Wanted " + destLen + " bytes, got " + (out.length - i) + ": expo=" + expo + ", modulus=" + modulus + "=" + modulus.toString(16) + ", data=" + RsaCrypto.hex(data) + "=" + new BigInteger(1, data) + ", result=" + RsaCrypto.hex(out) + "=" + new BigInteger(1, data).modPow(expo, modulus));
            }
            System.arraycopy(out, out.length - destLen, padded, 0, destLen);
        } else {
            System.arraycopy(out, 0, padded, destLen - out.length, out.length);
        }
        return padded;
    }

    private static String hex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (byte val : data) {
            int iVal = val & 0xFF;
            if (!first) {
                sb.append(':');
            }
            first = false;
            if (iVal <= 15) {
                sb.append('0');
            }
            sb.append(Integer.toString(iVal, 16));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        BigInteger gcd;
        BigInteger q;
        int numBits = 2048;
        SecureRandom rand = new SecureRandom();
        if (args.length > 0) {
            numBits = Integer.parseInt(args[0]);
        }
        BigInteger p = new BigInteger(numBits / 2, 30, rand);
        System.out.println("p = " + p);
        while ((q = new BigInteger(numBits / 2, 30, rand)).equals(p)) {
        }
        System.out.println("q = " + q);
        BigInteger modulus = p.multiply(q);
        System.out.println("Modulus = " + modulus);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        System.out.println("phi = " + phi);
        BigInteger pub = new BigInteger("65535");
        while (!(gcd = phi.gcd(pub)).equals(BigInteger.ONE)) {
            System.out.println("Exponent " + pub + " gives GCD " + gcd + ", trying again");
            pub = pub.subtract(BigInteger.ONE).subtract(BigInteger.ONE);
        }
        System.out.println("\nPublic key: " + modulus + "," + pub);
        BigInteger secret = pub.modInverse(phi);
        System.out.println("\nSecret key: " + modulus + "," + secret);
        System.out.println("\n(public * secret) % phi = " + pub.multiply(secret).divideAndRemainder(phi)[1]);
        byte[] data = new byte[]{72, 101, 108, 108, 111};
        System.out.println("");
        System.out.println("Initial data: len = " + data.length + ", text=" + new String(data));
        byte[] encrypted = RsaCrypto.encrypt(pub, modulus, data, 256);
        System.out.println("Encrypted data: len = " + encrypted.length);
        byte[] decrypted = RsaCrypto.encrypt(secret, modulus, encrypted, 5);
        System.out.println("Decrypted data: len = " + decrypted.length + ", text=" + new String(data));
        System.out.println("\nHex: Modulus=" + modulus.toString(16));
        System.out.println("\nHex: Public=" + pub.toString(16));
        System.out.println("\nHex: Secret=" + secret.toString(16));
    }
}
