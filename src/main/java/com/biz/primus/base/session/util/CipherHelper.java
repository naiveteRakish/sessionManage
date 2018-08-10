package com.biz.primus.base.session.util;
 
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.SortedMap;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;  
  
/** 
 * Created by Administrator on 2015/12/25. 
 */  
@SuppressWarnings("restriction")
public class CipherHelper {  
  
    /** 
     * 生成签名 
     * 
     * @param content 
     * @param privateKey 
     * @return 
     */  
    public static String sign(String content, String privateKey) {  
        return sign(content, str2PrivateKey(privateKey));  
    }  
  
    /** 
     * 验证签名 
     * 
     * @param content 
     * @param publicKey 
     * @return 
     */  
    public static boolean verify(String content, String sign, String publicKey) {  
        return verify(content, sign, str2PublicKey(publicKey));  
    }  
  
    /** 
     * @param content 
     * @param privateKey 
     * @return 
     */  
	public static String sign(String content, PrivateKey privateKey) {  
        try {  
            Signature signature = Signature.getInstance(SessionUtil.ALGORITHM);  
            signature.initSign(privateKey);  
            signature.update(content.getBytes(SessionUtil.CHARSET_NAME));  
            return new BASE64Encoder().encode(signature.sign());  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * @param content 
     * @param publicKey 
     * @return 
     */  
    public static boolean verify(String content, String sign, PublicKey publicKey) {  
        try {  
            Signature signature = Signature.getInstance(SessionUtil.ALGORITHM);  
            signature.initVerify(publicKey);  
            signature.update(content.getBytes(SessionUtil.CHARSET_NAME));  
            return signature.verify((new BASE64Decoder()).decodeBuffer(sign));  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  
    
  
    public static PublicKey str2PublicKey(String publicKeyPerm) {  
        try {  
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec((new BASE64Decoder()).decodeBuffer(publicKeyPerm));  
            KeyFactory keyFactory = KeyFactory.getInstance(SessionUtil.KEY_PAIR_GENERATOR_ALGORITHM);  
            return keyFactory.generatePublic(keySpec);  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    public static PrivateKey  str2PrivateKey(String privateKeyPerm) {  
        try {  
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec((new BASE64Decoder()).decodeBuffer(privateKeyPerm));  
            KeyFactory keyFactory = KeyFactory.getInstance(SessionUtil.KEY_PAIR_GENERATOR_ALGORITHM);  
            return  keyFactory.generatePrivate(keySpec);  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * 将所有POST 参数（sign除外）进行字典排序，组成字符串, 然后使用私钥产生签名 
     * @param params 
     * @return sign 
     */  
    public static String encodeParams(SortedMap<?, ?> params, String privateKey) {  
        StringBuilder sb = new StringBuilder();  
        for (Map.Entry<?, ?> entry : params.entrySet()) {  
            sb.append(entry.getKey())  
                    .append("=")  
                    .append(entry.getValue())  
                    .append("&");  
        }  
  
        sb.deleteCharAt(sb.length() - 1);  
        return sign(sb.toString(), privateKey);  
    }  
  
    public static void main1(String[] args) throws Exception {  
    	//生成密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(SessionUtil.KEY_PAIR_GENERATOR_ALGORITHM);  
        keyPairGenerator.initialize(SessionUtil.KEY_SIZE);  
        KeyPair keyPair = keyPairGenerator.genKeyPair();  
        PublicKey publicKey = keyPair.getPublic();  
        PrivateKey privateKey = keyPair.getPrivate();  
  
        BASE64Encoder base64Encoder = new BASE64Encoder();  
        System.out.println("-------------------public key----------------------");  
        System.out.println(base64Encoder.encode(publicKey.getEncoded()));  
        System.out.println("-------------------private key---------------------");  
        System.out.println(base64Encoder.encode(privateKey.getEncoded()));  
  
    }  
    
    public static KeyPair KeyPairGenerator(){
    	//生成密钥对
        KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(SessionUtil.KEY_PAIR_GENERATOR_ALGORITHM);
			keyPairGenerator.initialize(SessionUtil.KEY_SIZE);  
	        return keyPairGenerator.genKeyPair();  
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}  
        
    }
  
    public static void main(String[] args) throws NoSuchAlgorithmException {  
//        String publicKey = GOUQI_PUBLIC_KEY;  
//        String privateKey = GOUQI_PRIVATE_KEY;  
  
//        String appPrivateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAN0yqP" +  
//                "kLXlnhM+2H/57aHsYHaHXazr9pFQun907TMvmbR04wHChVsKVgGUF1hC0FN" +  
//                "9hfeYT5v2SXg1WJSg2tSgk7F29SpsF0I36oSLCIszxdu7ClO7c22mxEVuCj" +  
//                "mYpJdqb6XweAZzv4Is661jXP4PdrCTHRdVTU5zR9xUByiLSVAgMBAAECgYEAh" +  
//                "znORRonHylm9oKaygEsqQGkYdBXbnsOS6busLi6xA+iovEUdbAVIrTCG9t854" +  
//                "z2HAgaISoRUKyztJoOtJfI1wJaQU+XL+U3JIh4jmNx/k5UzJijfvfpT7Cv3ueM" +  
//                "tqyAGBJrkLvXjiS7O5ylaCGuB0Qz711bWGkRrVoosPM3N6ECQQD8hVQUgnHEVH" +  
//                "ZYtvFqfcoq2g/onPbSqyjdrRu35a7PvgDAZx69Mr/XggGNTgT3jJn7+2XmiGkH" +  
//                "M1fd1Ob/3uAdAkEA4D7aE3ZgXG/PQqlm3VbE/+4MvNl8xhjqOkByBOY2ZFfWKh" +  
//                "lRziLEPSSAh16xEJ79WgY9iti+guLRAMravGrs2QJBAOmKWYeaWKNNxiIoF7/4" +  
//                "VDgrcpkcSf3uRB44UjFSn8kLnWBUPo6WV+x1FQBdjqRviZ4NFGIP+KqrJnFHzN" +  
//                "gJhVUCQFzCAukMDV4PLfeQJSmna8PFz2UKva8fvTutTryyEYu+PauaX5laDjyQ" +  
//                "bc4RIEMU0Q29CRX3BA8WDYg7YPGRdTkCQQCG+pjU2FB17ZLuKRlKEdtXNV6zQ" +  
//                "FTmFc1TKhlsDTtCkWs/xwkoCfZKstuV3Uc5J4BNJDkQOGm38pDRPcUDUh2/";  
    	
    	//生成密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(SessionUtil.KEY_PAIR_GENERATOR_ALGORITHM);  
        keyPairGenerator.initialize(SessionUtil.KEY_SIZE);  
        KeyPair keyPair = keyPairGenerator.genKeyPair();  
        PublicKey publicKey = keyPair.getPublic();  
        PrivateKey privateKey = keyPair.getPrivate();  
        
        
        
        
        
        //使用公钥进行验签
		String sign = sign("nihaoa", new BASE64Encoder().encode(privateKey.getEncoded()));  
        System.out.println("----------------private :");
        System.out.println(new BASE64Encoder().encode(privateKey.getEncoded()));
        System.out.println("----------------");
        System.out.println(sign);
        System.out.println("----------------");
        System.out.println(verify("nihaoa", sign,  new BASE64Encoder().encode(publicKey.getEncoded()))); 
        System.out.println("----------------public ：");
        System.out.println( new BASE64Encoder().encode(publicKey.getEncoded()));
        
        
//        String privat = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJt/iSB6PqaSxteXnRWrFezvfqiSGexiI5AemaCQ2yU4OSxckVdVYOpNN4ihEajka+iHFbnILB5ymol0q2bFcNriRmxGOAmL5+s3QcSAZcSssOl4ddfl3a5wdDK/BvOBbXpgGzGmpzPbQpL/G5LmG1nV9U4t5VjVYxs40N0A9yYtAgMBAAECgYEAiW7AAM9qfgit67BZquQKPd4yoMR4Wv8mFWOmPk6pEY1OotGxt3GDdbIHih9pn087vElgP1IX BFYgFarjLrMEmV25Cm7YVc3o4BsBdMu+PSERxE0z+VSBOUgNlF/p/WF6emZ+yyHc7FvL3GcBcAFcqeZiDJnNmBTDPZoTm720AmECQQDeKgZMmuKaF0wkY6N2a7+RNqoJ3iHDDx8EcNTCMSyKev9uFk2M3hMbsi/ZGGn5qQ19XFBWAqGMFeg8faJCTVCJAkEAsy5ELs42P7DVyiTko5YR5F1lBFV8I5mXcB6gLuG2ocWpOCdiOeGIRXBhFci3+KMqTj87lixZF8unIyjk9BAXhQJBAK9lpFWEjtSzAgVUuPA8fyDJdJOX0TXr0UQzRHYQVqb6FoG2kAFgLtvV4RrXwItAIYtQf4h4SUyM2y4uzhTkb2kCQD1GeTK4enzVcIcsjrQSwhOijMUqpsVV5SAGmSjvL5GrEu+NKF467mswv0K/3Yo94l3X6fy1NdKZZpLzjTiZuDECQE4kQF/SlT7eah2BqR3OLsmtADnCCb9kE5qlv8ycCtwwAjW0InZRSdpwuYV2QgCx/l9nKEQpKwLIGXp6RjVK8Rc=".replaceAll(" ", "");
//        System.out.println("----------------sign ：");
//        						 
//        System.out.println(sign("file=备忘录.txt&param1=备忘录.txt&param2=param2&param3=param3", privat));
//        
//        System.out.println(verify("param1=<script>hello</script>&param2=<br>asdf</br>&param3=hellxsso", "HUfAwYfoNWTXhBqhBsTNy/SDRJ7rGGKx+fvuSJgVXDoMnaOc1+Bq++UBHFQpM02ieROIJo/Lup9KNZK0S3BrjqC/6uJVDAKU5me5DYEEPKBnLMvvCF0qRxbNEXqt8Pxvk28aWYNwcsiyXzwKwB6lb4hPPvihT2TJZn37jT1u0gQ=", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkK98rQRPo+iKM7hrexaDW9m646MsSPuiYU5NI"+
//				"3cINlFhWOD5dD4fhZMvgJu4kdgyCuTTIYQ7bBbnl9edOh/ArWoCMfpY8Kmt1ecHw8YHotERTGZnG"+
//				"SA0v/X4zClIoXUAHTVRZGRJ7PrQunOh/ohnbgXDxCYZV07A0Qq2RfEv7ywIDAQAB"));
  
  
    }  
    
    /**
     * json 和 url参数签名
     * @param args
     */
    public static void main11(String[] args) {
    	String privat = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJt/iSB6PqaSxteXnRWrFezvfqiSGexiI5AemaCQ2yU4OSxckVdVYOpNN4ihEajka+iHFbnILB5ymol0q2bFcNriRmxGOAmL5+s3QcSAZcSssOl4ddfl3a5wdDK/BvOBbXpgGzGmpzPbQpL/G5LmG1nV9U4t5VjVYxs40N0A9yYtAgMBAAECgYEAiW7AAM9qfgit67BZquQKPd4yoMR4Wv8mFWOmPk6pEY1OotGxt3GDdbIHih9pn087vElgP1IX BFYgFarjLrMEmV25Cm7YVc3o4BsBdMu+PSERxE0z+VSBOUgNlF/p/WF6emZ+yyHc7FvL3GcBcAFcqeZiDJnNmBTDPZoTm720AmECQQDeKgZMmuKaF0wkY6N2a7+RNqoJ3iHDDx8EcNTCMSyKev9uFk2M3hMbsi/ZGGn5qQ19XFBWAqGMFeg8faJCTVCJAkEAsy5ELs42P7DVyiTko5YR5F1lBFV8I5mXcB6gLuG2ocWpOCdiOeGIRXBhFci3+KMqTj87lixZF8unIyjk9BAXhQJBAK9lpFWEjtSzAgVUuPA8fyDJdJOX0TXr0UQzRHYQVqb6FoG2kAFgLtvV4RrXwItAIYtQf4h4SUyM2y4uzhTkb2kCQD1GeTK4enzVcIcsjrQSwhOijMUqpsVV5SAGmSjvL5GrEu+NKF467mswv0K/3Yo94l3X6fy1NdKZZpLzjTiZuDECQE4kQF/SlT7eah2BqR3OLsmtADnCCb9kE5qlv8ycCtwwAjW0InZRSdpwuYV2QgCx/l9nKEQpKwLIGXp6RjVK8Rc=".replaceAll(" ", "");
        System.out.println("----------------sign ：");
//        						 
        System.out.println();
//    
//        Stirng content = "param2=param2&[{\"address\":\"BeiJing\",\"custName\":\"Tom\",\"id\":1},{\"address\":\"ShangHai\",\"custName\":\"Bob\",\"id\":1}]";
      String sign =  sign("param2=param2&[{\"address\":\"BeiJing\",\"custName\":\"Tom\",\"id\":1},{\"address\":\"ShangHai\",\"custName\":\"Bob\",\"id\":1}]", privat);
    System.out.println(verify("param2=param2&[{\"address\":\"BeiJing\",\"custName\":\"Tom\",\"id\":1},{\"address\":\"ShangHai\",\"custName\":\"Bob\",\"id\":1}]", sign, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbf4kgej6mksbXl50VqxXs736okhnsYiOQHpmgkNslODksXJFXVWDqTTeIoRGo5GvohxW5yCwecpqJdKtmxXDa4kZsRjgJi+frN0HEgGXErLDpeHXX5d2ucHQyvwbzgW16YBsxpqcz20KS/xuS5htZ1fVOLeVY1WMbONDdAPcmLQIDAQAB"));    
	
    }
  
}  