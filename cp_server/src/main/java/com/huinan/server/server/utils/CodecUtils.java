package com.huinan.server.server.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * use ssl to encode and decode data
 * 
 * ashley
 */
public class CodecUtils {
    private CodecUtils(){}
    static String private_pem_file = "/pkcs8_rsa_private_key.pem";
    static String public_pem_file = "/public_key.pem";
    static RSAPrivateKey privateKey;
    static RSAPublicKey publicKey;
    
    public static void init() throws IOException, GeneralSecurityException{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, CodecUtils.getPublicKey());
        cipher.init(Cipher.DECRYPT_MODE, CodecUtils.getPrivateKey());
    }
    
    public static String getMD5HashValue(String data, String salt) {
        // use private key to generate a hash value
        return DigestUtils.md5Hex(data + salt);
    }

    public static String getMD5(String data) {
        // use private key to generate a hash value
        return DigestUtils.md5Hex(data);
    }

    /**
     * read and return key string form configuration
     *  
     * @param filename
     * @return
     * @throws IOException
     */
    private static String getKey(String filename) throws IOException {
        // Read key from file
        String strKeyPEM = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                CodecUtils.class.getResourceAsStream(filename)));
        String line;
        while ((line = br.readLine()) != null) {
            strKeyPEM += line + "\n";
        }
        br.close();
        return strKeyPEM;
    }

    /**
     * read and return an instance of RSAPrivateKey from default configuration 
     * 
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static RSAPrivateKey getPrivateKey() throws IOException,
            GeneralSecurityException {
        if(privateKey == null){
            privateKey = getPrivateKey(private_pem_file);
        }
        return privateKey;
    }

    /**
     * read and return instance of RSAPrivateKey from a file that the parameter specifed
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static RSAPrivateKey getPrivateKey(String filename)
            throws IOException, GeneralSecurityException {
        String privateKeyPEM = getKey(filename);
        return getPrivateKeyFromString(privateKeyPEM);
    }

    public static RSAPrivateKey getPrivateKeyFromString(String key)
            throws IOException, GeneralSecurityException {
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n",
                "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decodeBase64(privateKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
        return privKey;
    }

    /**
     * get an instance of RSAPublicKey from default configuation
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static RSAPublicKey getPublicKey() throws IOException,
            GeneralSecurityException {
        if(publicKey == null){
            publicKey = getPublicKey(public_pem_file);
        }
        return publicKey;
    }

    /**
     * get and instance of RSAPublicKey from a file that specified by parameter
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static RSAPublicKey getPublicKey(String filename)
            throws IOException, GeneralSecurityException {
        String publicKeyPEM = getKey(filename);
        return getPublicKeyFromString(publicKeyPEM);
    }

    public static RSAPublicKey getPublicKeyFromString(String key)
            throws IOException, GeneralSecurityException {
        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] encoded = Base64.decodeBase64(publicKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf
                .generatePublic(new X509EncodedKeySpec(encoded));
        return pubKey;
    }

    /**
     * get a signature of message via private key
     * 
     * @param privateKey
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    public static String sign(PrivateKey privateKey, String message)
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, UnsupportedEncodingException {
        Signature sign = Signature.getInstance("SHA1withRSA");
        sign.initSign(privateKey);
        sign.update(message.getBytes("UTF-8"));
        return new String(Base64.encodeBase64(sign.sign()), "UTF-8");
    }

    /**
     * verify signature via public key
     * 
     * @param publicKey
     * @param message
     * @param signature
     * @return
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static boolean verify(PublicKey publicKey, String message,
            String signature) throws SignatureException,
            NoSuchAlgorithmException, UnsupportedEncodingException,
            InvalidKeyException {
        Signature sign = Signature.getInstance("SHA1withRSA");
        sign.initVerify(publicKey);
        sign.update(message.getBytes("UTF-8"));
        return sign.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
    }

    /**
     * encode data via public key
     * 
     * @param rawText
     * @param publicKey
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String encrypt(String rawText, PublicKey publicKey)
            throws IOException, GeneralSecurityException {
        Cipher cipher = CliperThreadLocal.getInstance();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.encodeBase64String(cipher.doFinal(rawText
                .getBytes("UTF-8")));
    }

    /**
     * decode data via private key
     * 
     * @param cipherText
     * @param privateKey
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String decrypt(String cipherText, PrivateKey privateKey)
            throws IOException, GeneralSecurityException,IllegalBlockSizeException{
        //long startTime = System.currentTimeMillis();
        Cipher cipher = CliperThreadLocal.getInstance();
        //System.out.println("cipher: "+(System.currentTimeMillis() - startTime));
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //System.out.println("cipher init: "+(System.currentTimeMillis() - startTime));
        return new String(cipher.doFinal(Base64.decodeBase64(cipherText)),
                "UTF-8");
    }
    
    /**
     * 将utf8格式的字符串解码
     * @param raw
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decodeStringUTF8(String raw) throws UnsupportedEncodingException{
        return new String(raw.getBytes("UTF-8"), "UTF-8");
    }
}
