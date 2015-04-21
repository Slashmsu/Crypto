package Crypto;

import java.nio.charset.Charset;

/**
 *
 * @author SlashMSU
 */
public class TsdCryptoAlgoritm {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
			String text;
			text = "привет";
			//AES
                    String k = "1a25s8fe5dsg65ad";
                    System.out.println("Text clar: "+text);
                    byte[] enc = AES.encrypt(text.getBytes(), k.getBytes());
                    System.out.println("Text cripted text by AES: "+new String(enc));
                    
                        String test = new String(enc);
                        byte[] b = test.getBytes();
                        System.out.println("=======  "+enc);
                        System.out.println("=======  "+b);
                        
			byte[] dec = AES.decrypt(enc, k.getBytes());
			System.out.println("Text decripted text: "+new String(dec));
			System.out.println("------------------");
		
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
}
