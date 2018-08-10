package com.biz.primus.base.session.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
 
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
 
import org.apache.commons.codec.binary.Base64;
 
/**
 * HMAC_SHA1 Sign生成器.
 * 
 * 需要apache.commons.codec包
 * 
 */
public class HMAC_SHA1 {
 
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
 
	/**
	 * 使用 HMAC-SHA1 签名方法对data进行签名
	 * 
	 * @param data
	 *            被签名的字符串
	 * @param key
	 *            密钥     
	 * @return 
                      加密后的字符串
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static String genHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] result = null;
			//根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称  
			SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			//生成一个指定 Mac 算法 的 Mac 对象  
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			//用给定密钥初始化 Mac 对象  
			mac.init(signinKey);
			//完成 Mac 操作   
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = Base64.encodeBase64(rawHmac);
 
		if (null != result) {
			return new String(result);
		} else {
			return null;
		}
	}
	
	
	
	
	/**
	 * 测试
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException {
		String genHMAC = genHMAC("111", "2222");
		System.out.println(genHMAC.length()); //28
		System.out.println(genHMAC);  // O5fviq3DGCB5NrHcl/JP6+xxF6s=
	}
}