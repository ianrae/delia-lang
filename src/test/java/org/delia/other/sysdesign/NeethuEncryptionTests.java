package org.delia.other.sysdesign;
//package org.dnal.other.sysdesign;
////package org.dnal.other.sysdesign;
//
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.UnsupportedEncodingException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//import java.util.Base64;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//
//import org.junit.Test;
//
//
//public class NeethuEncryptionTests {
//
//	public enum EncryptionMethod {
//		NONE,
//		CAESAR_SHIFT,
//		AES
//	};
//
//	public interface DataEncryptor {
//		String encrypt(String key, String message);
//		String decrypt(String key, String encryptedMessage);
//	}
//	public interface DataEncryptorFactory {
//		DataEncryptor create(EncryptionMethod method);
//	}
//
//	//-- the do-nothing encryptor
//	public static class DoNothingEncryptor implements DataEncryptor {
//
//		@Override
//		public String encrypt(String key, String message) {
//			return message;
//		}
//
//		@Override
//		public String decrypt(String key, String encryptedMessage) {
//			return encryptedMessage;
//		}
//
//	}
//	
//	
//	
//	
//	
//	
//	
//
//	// -- TODO: add the CAESAR_SHIPT encryptor class here
//	public static class CeaserShiftEncryptor implements DataEncryptor {
//
//		@Override
//		public String encrypt(String key, String message) {
//			String encryptedMessage = "";
//			int k = Integer.parseInt(key);
//			for(int i = 0; i<message.length(); i++) {
//				char c = message.charAt(i);
//				if(Character.isLowerCase(c)) {
//					c = (char) (c + k);
//					if(c > 'z') {
//						c = (char) (c - 'z' + 'a' - 1);
//					}
//					encryptedMessage += c;
//				}else if(Character.isUpperCase(c)) {
//					c = (char) (c + k);
//					if(c > 'Z') {
//						c = (char) (c - 'Z' + 'A' - 1);
//					}
//					encryptedMessage += c;
//				}
//				else
//					encryptedMessage += c;
//			}
//			return encryptedMessage;
//		}
//
//		@Override
//		public String decrypt(String key, String encryptedMessage) {
//			String decryptedMessage = "";
//			int k = Integer.parseInt(key);
//			for(int i = 0; i<encryptedMessage.length(); i++) {
//				char c = encryptedMessage.charAt(i);
//				if(Character.isLowerCase(c)) {
//					c = (char) (c - 3);
//					if(c < 'a') {
//						c = (char) (c + 'z' - 'a' + 1);
//					}
//					decryptedMessage += c;
//				}else if(Character.isUpperCase(c)) {
//					c = (char) (c - 3);
//					if(c < 'A') {
//						c = (char) (c + 'Z' - 'A' + 1);
//					}
//					decryptedMessage += c;
//				}
//				else
//					decryptedMessage += c;
//			}
//			return decryptedMessage;
//		}
//		
//	}
//	
//	
//	
//	
//	
//	
//	
//	// -- TODO: add the AES encryptor class here
//	public static class AESEncryptor implements DataEncryptor {
//		
//		private static SecretKeySpec secretKey;
//	    private static byte[] key;
//		
//		public static void setKey(String myKey) 
//	    {
//	        MessageDigest sha = null;
//	        try {
//	            key = myKey.getBytes("UTF-8");
//	            sha = MessageDigest.getInstance("SHA-1");
//	            key = sha.digest(key);
//	            key = Arrays.copyOf(key, 16); 
//	            secretKey = new SecretKeySpec(key, "AES");
//	        } 
//	        catch (NoSuchAlgorithmException e) {
//	            e.printStackTrace();
//	        } 
//	        catch (UnsupportedEncodingException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	 
//		
//	    public String encrypt(String key, String message) 
//	    {
//	        try
//	        {
//	            setKey(key);
//	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//	            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//	            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes("UTF-8")));
//	        } 
//	        catch (Exception e) 
//	        {
//	            System.out.println("Error while encrypting: " + e.toString());
//	        }
//	        return null;
//	    }
//	 
//	    public String decrypt(String key, String encrpytedMessage) 
//	    {
//	        try
//	        {
//	            setKey(key);
//	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
//	            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//	            return new String(cipher.doFinal(Base64.getDecoder().decode(encrpytedMessage)));
//	        } 
//	        catch (Exception e) 
//	        {
//	            System.out.println("Error while decrypting: " + e.toString());
//	        }
//	        return null;
//	    }
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	public static class MyDataEncryptorFactory implements DataEncryptorFactory {
//
//		@Override
//		public DataEncryptor create(EncryptionMethod method) {
//			switch(method) {
//			case NONE:
//				return new DoNothingEncryptor();
//			case CAESAR_SHIFT:
//				return new CeaserShiftEncryptor();
//			case AES:
//				return new AESEncryptor();
//			default:
//				return null;
//			}
//		}
//
//	}
//
//
//	@Test
//	public void testNone() {
//		DataEncryptorFactory factory = new MyDataEncryptorFactory();
//		DataEncryptor encryptor = factory.create(EncryptionMethod.NONE);
//
//		String plainText = "this is a secret message";
//		String encryptedMessage = encryptor.encrypt("", plainText);
//		System.out.println("Enc: " + encryptedMessage);
//
//		String output = encryptor.decrypt("", encryptedMessage);
//		System.out.println("Dec: " + output);
//		assertEquals(plainText, output);
//	}
//
//	@Test
//	public void testCaesarShift() {
//		DataEncryptorFactory factory = new MyDataEncryptorFactory();
//		DataEncryptor encryptor = factory.create(EncryptionMethod.CAESAR_SHIFT);
//
//		String plainText = "this is a secret message";
//		String encryptedMessage = encryptor.encrypt("3", plainText);
//		System.out.println("Enc: " + encryptedMessage);
//
//		String output = encryptor.decrypt("3", encryptedMessage);
//		System.out.println("Dec: " + output);
//		assertEquals(plainText, output);
//	}
//	
//	@Test
//	public void testAES() {
//		DataEncryptorFactory factory = new MyDataEncryptorFactory();
//		DataEncryptor encryptor = factory.create(EncryptionMethod.AES);
//		
//		String plainText = "this is a secret message";
//        String encryptedMessage = encryptor.encrypt("3", plainText);
//		System.out.println("Enc: " + encryptedMessage);
//
//		String output = encryptor.decrypt("3", encryptedMessage);
//		System.out.println("Dec: " + output);
//		assertEquals(plainText, output);
//	}
//}
//
//
//
//
//
