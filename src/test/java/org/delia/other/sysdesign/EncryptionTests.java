package org.delia.other.sysdesign;
//package org.dnal.other.sysdesign;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Test;
//
//public class EncryptionTests {
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
//	// -- TODO: add the CAESAR_SHIPT encryptor class here
//	// -- TODO: add the AES encryptor class here
//	
//	public static class MyDataEncryptorFactory implements DataEncryptorFactory {
//
//		@Override
//		public DataEncryptor create(EncryptionMethod method) {
//			switch(method) {
//			case NONE:
//				return new DoNothingEncryptor();
//			//TODO: add cases for other encryption methods
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
//		String key = "";
//		String encryptedMessage = encryptor.encrypt(key, plainText);
//		System.out.println("Enc: " + encryptedMessage);
//		
//		String output = encryptor.decrypt(key, encryptedMessage);
//		System.out.println("Dec: " + encryptedMessage);
//		assertEquals(plainText, output);
//	}
//
//	@Test
//	public void testCaesarShift() {
//		DataEncryptorFactory factory = new MyDataEncryptorFactory();
//		DataEncryptor encryptor = factory.create(EncryptionMethod.AES);
//		
//		String plainText = "this is a secret message";
//		String key = "2"; //# of letters to shift each character
//		String encryptedMessage = encryptor.encrypt(key, plainText);
//		System.out.println("Enc: " + encryptedMessage);
//		
//		String output = encryptor.decrypt(key, encryptedMessage);
//		System.out.println("Dec: " + encryptedMessage);
//		assertEquals(plainText, output);
//	}
//	@Test
//	public void testAES() {
//		DataEncryptorFactory factory = new MyDataEncryptorFactory();
//		DataEncryptor encryptor = factory.create(EncryptionMethod.AES);
//		
//		String plainText = "this is a secret message";
//		String key = "ssshhhhhhhhhhh!!!!";
//
//		String encryptedMessage = encryptor.encrypt(key, plainText);
//		System.out.println("Enc: " + encryptedMessage);
//		
//		String output = encryptor.decrypt(key, encryptedMessage);
//		System.out.println("Dec: " + encryptedMessage);
//		assertEquals(plainText, output);
//	}
//}
