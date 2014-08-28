package uy.gub.agesic.connector;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import uy.gub.agesic.connector.exceptions.ConnectorException;

public class PasswordManager {

	private static final String ALGO = "AES";
	private static final byte[] keyValue = new byte[] { 'A', 'g', 'e', 'o',
			'f', '8', '8', '.', ';', '4', '5', '6', 'a', 's', 'q', 'w' };

	public static String encrypt(String data) throws ConnectorException {
		if (data == null || data.length() == 0) {
			return data;
		}
		try {
			Key key = generateKey();
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encVal = c.doFinal(data.getBytes());
			String encryptedValue = new BASE64Encoder().encode(encVal);
			return encryptedValue;
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	public static String decrypt(String encryptedData) throws ConnectorException {
		if (encryptedData == null || encryptedData.length() == 0) {
			return encryptedData;
		}			
		try {
			Key key = generateKey();
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			String decryptedValue = new String(decValue);
			return decryptedValue;
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	private static Key generateKey() {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}

}
