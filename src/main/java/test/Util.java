package test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;

import static javax.crypto.Cipher.getInstance;

/**
 * 介于java 不支持PKCS7Padding，只支持PKCS5Padding 但是PKCS7Padding 和 PKCS5Padding 没有什么区别
 * 要实现在java端用PKCS7Padding填充，需要用到bouncycastle组件来实现
 */
public class Util {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String decryptData(String data, String session_key, String iv) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        byte[] content = base64DecodeData(data);
        byte[] keyBytes = base64DecodeData(session_key);
        byte[] ivBytes = base64DecodeData(iv);
        return new String(AESDecodeData(content, keyBytes, ivBytes), "utf-8");
    }

    private static byte[] AESDecodeData(byte[] content, byte[] keyBytes, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        Key key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(content);
    }

    private static byte[] base64DecodeData(String s) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(s);
    }


    public static void checkSignature(String session, String rawData, String signature) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String data = rawData + session;
        System.out.println(data);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] bs = messageDigest.digest(data.getBytes("utf-8"));
        byte2Hex(bs);
        System.out.println(signature);
    }

    private static void byte2Hex(byte[] bs) {
        byte2Hex(bs, 0, bs.length);
    }

    public static void byte2Hex(byte[] bs, int start, int offset) {
        StringBuilder stringBuilder = new StringBuilder(2 * offset);
        for (int i = start; i < start + offset; i++) {
            byte high = (byte) ((byte) (bs[i] >> 4) & 0x0f);
            stringBuilder.append("0123456789abcdef".charAt(high));
            byte low = (byte) ((bs[i] & 0x0f));
            stringBuilder.append("0123456789abcdef".charAt(low));
        }
        System.out.println(stringBuilder.toString());
    }

}
