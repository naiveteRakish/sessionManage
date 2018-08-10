package com.biz.primus.base.session.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.InputBuffer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.biz.primus.common.exception.BizSilentException;

public class SessionUtil {

	/**
	 * session参数是否更新校验key
	 */
	public final static String PARAMS_MD5 = "paramsMd5";
	/**
	 * session创建时间key
	 */
	public final static String CREATION_TIME = "creationTime";
	/**
	 * session最后一次访问时间key
	 */
	public final static String LAST_ACCESSED_TIME = "lastAccessedTime";
	/**
	 * session生命时长key
	 */
	public final static String MAX_INACTIVE_INTERVAL = "maxInactiveInterval";
	/**
	 * SessionUtil.UPDATE_LAST_ACCESSED_TYPE <=0:无间隔，每次访问都更新一次session
	 * 其它数值都代表间隔x秒 之后用户第一次访问更新
	 */
	public final static String UPDATE_LAST_ACCESSED_TYPE = "updateLastAccessedType";
	/**
	 * 令牌参数名
	 */
	public final static String CSRF_TOKEN = "csrf_token";
	/**
	 * token中时间戳的分隔符
	 */
	public final static String CSRF_TOKEN_SEPARATOR = "=";
	/**
	 * 会话标识
	 */
	public final static String SESSION_ID = "Authorization";
	/**
	 * 未登录用户默认标识
	 */
	public final static String DEFAULT_SESSIONID_VALUE = "a14372fd-206b-46c5-8055-18afc8d3594b";
	/**
	 * 申请token标识
	 */
	public final static String WANT_TOKEN = "WANT_TOKEN";
	/**
	 * 公钥
	 */
	public final static String PUBLIC_KEY = "PUBLIC_KEY";
	/**
	 * 私钥
	 */
	public final static String PRIVATE_KEY = "PRIVATE_KEY";
	/**
	 * 签名
	 */
	public final static String SIGN = "SIGN";
	/**
	 * 字符集
	 */
	public static String CHARSET_NAME = "UTF-8";

	// code相关
	/**
	 * TODO 用户未认证code ???
	 */
	public static int UNVERIFIED = 3332333;
	/**
	 * TODO 验签失败code ???
	 */
	public static int VERIFY_FAIL = 1111111;
	/**
	 * TODO 无效的CSRF令牌
	 */
	public static int INVALID_CSRF = 2222222;
	/**
	 * 缺失必要字段
	 */
	public static int MISSING_FIELD = 444444444;
	/**
	 * 缺失必要字段
	 */
	public static int NOT_SUPPORT_OPERATE = 555555;
	
	/**
	 * 私钥长度
	 */
	public static  int KEY_SIZE = 571;

	/**
	 * 摘要算法
	 */
	public  static String ALGORITHM = "SHA512withECDSA";

	/**
	 * 密钥对生成加解密算法 RSA 、 DSA 、 EC
	 * RSA 摘要算法:  SHA1withRSA SHA224withRSA SHA256withRSA
	 * DSA 摘要算法:  SHA1withDSA  SHA224withDSA   SHA256withDSA 
	 * EC  摘要算法:  SHA1withECDSA SHA224withECDSA SHA512withECDSA
	 */
	public  static String KEY_PAIR_GENERATOR_ALGORITHM = "EC";

	/**
	 * 指定签名算法
	 * 
	 * 0:常规签名（包含加解密和摘要计算） 1:使用HMAC_SHA1 摘要签名 2:使用SHA256 摘要签名
	 * 如果选择0则  前台必须按照 KEY_PAIR_GENERATOR_ALGORITHM 和  ALGORITHM 这两个字段 确定使用的加密算法和摘要算法
	 * 如果选择 1 或者 2 则 KEY_SIZE  ALGORITHM KEY_PAIR_GENERATOR_ALGORITHM 这三者属性是无效的
	 * 如果选择1  前台需要将private_key 进行HMAC_SHA1摘要 再进行base64编码 
	 * 如果选择2  前台就只需将参数进行SHA256摘要 再进行 base64编码
	 */
	public static int ALGORITHM_SWITCH;
	/**
	 * 二级域名cookie共享 cookie domain_pattern
	 */
	public static String DOMAIN_PATTERN = null;
	/**
	 * cookie maxage 单位秒
	 */
	public static Integer COOKIE_MAXAGE = 1800;
	/**
	 * 是否开启文件验签 默认false
	 */
	public static boolean FILE_VERIFY_SWITCH = false;
	/**
	 * 
	 * 是否开启app 参数替换策略
	 */
	public static boolean APP_REPLACE_SWITCH = false;

	/**
	 * 对字符串md5加密(大写+数字)
	 *
	 * @param str
	 *            传入要加密的字符串
	 * @return MD5加密后的字符串
	 */

	public static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String XSSHtmlFilt(String msg) {
		StringBuffer buffer = new StringBuffer(msg.length());
		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			switch (c) {
			case '\b':
				buffer.append("\\b");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			case '\n':
				buffer.append("<br />");
				break;
			case '\r':
				// ignore
				break;
			case '\t':
				buffer.append("\\t");
				break;
			case '\'':
				buffer.append("\\'");
				break;
			case '\"':
				buffer.append("\\\"");
				break;
			case '\\':
				buffer.append("\\\\");
				break;
			case '<':
				buffer.append("&lt;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '&':
				buffer.append("&amp;");
				break;
			default:
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * 获取请求体内容， 之所以使用反射加截取的方式 是因为使用流读取之后，后续的流将无法进行读取了。
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String getRequestBodyParam(HttpServletRequest request) throws IOException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		InputStream is = request.getInputStream();
		Field iBufferField = is.getClass().getDeclaredField("ib");
		iBufferField.setAccessible(true);
		org.apache.catalina.connector.InputBuffer iBuffer = (InputBuffer) iBufferField.get(is);
		iBufferField.setAccessible(false);
		Field bufferField = org.apache.catalina.connector.InputBuffer.class.getDeclaredField("bb");
		bufferField.setAccessible(true);
		ByteBuffer buffer = (ByteBuffer) bufferField.get(iBuffer);
		String httpMessage = new String(buffer.array());
		String[] strings = httpMessage.split("(?m)^\\s*$" + System.lineSeparator());

		bufferField.setAccessible(false);
		if (strings.length > 1) {
			if (request.getHeader("Content-Type").contains("application/json")
					|| request.getHeader("Content-Type").contains("application/json;charset=CHARSET_NAME")) {
				// 处理json
				return parseJson(strings[1].trim());
			} else {
				// return parseJson(strings[1].trim());
				throw new BizSilentException(SessionUtil.NOT_SUPPORT_OPERATE,
						"不支持的Content-Type >>>" + request.getHeader("Content-Type"));
			}
		}
		return null;
	}

	/**
	 * 获取请求体内容， 之所以使用反射加截取的方式 是因为使用流读取之后，后续的流将无法进行读取了。
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String getRequestBodyParamAndXSS(HttpServletRequest request) throws IOException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {

		InputStream is = request.getInputStream();
		Field iBufferField = is.getClass().getDeclaredField("ib");
		iBufferField.setAccessible(true);
		org.apache.catalina.connector.InputBuffer iBuffer = (InputBuffer) iBufferField.get(is);
		iBufferField.setAccessible(false);
		Field bufferField = org.apache.catalina.connector.InputBuffer.class.getDeclaredField("bb");
		bufferField.setAccessible(true);
		ByteBuffer buffer = (ByteBuffer) bufferField.get(iBuffer);
		bufferField.setAccessible(false);

		String httpMessage = new String(buffer.array(), CHARSET_NAME);
		String[] strings = httpMessage.split("(?m)^\\s*$" + System.lineSeparator());
		if (strings.length > 1) {
			if (request.getHeader("Content-Type").equals("application/json")) {
				// 处理json
				return parseJson(strings[1].trim());
			} else {
				return parseJson(strings[1].trim());
				// throw new BizSilentException(SessionUtil.NOT_SUPPORT_OPERATE,
				// "不支持的Content-Type >>>"+request.getHeader("Content-Type"));
			}
		}
		return null;
	}

	private static String parseJson(String trim) {
		char[] cs = trim.toCharArray();
		if (cs[0] != '[' && cs[0] != '{') {
			return trim;
		}
		Queue<Character> queue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
		char c;
		for (int i = 0; i < cs.length - 1; i++) {
			c = cs[i];
			if (c == '[' || c == '{') {
				queue.add(c);
			} else if (c == ']' || c == '}') {
				queue.poll();
			}
			if (queue.isEmpty()) {
				return new String(cs, 0, i + 1);
			}
		}
		return trim;
	}

	public static Map<String, String> getRequestFileNames(HttpServletRequest request) {

		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		Map<String, String> result = null;
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			Map<String, MultipartFile> mults = multiRequest.getFileMap();
			result = new HashMap<>(mults.size() + 1, 1);
			for (Map.Entry<String, MultipartFile> item : mults.entrySet()) {
				result.put(item.getKey(), item.getValue().getOriginalFilename());
			}
		}
		return result;
	}

	public static Map<String, String> getSessionMap(HttpSession session)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> standardSessionFacade = session.getClass();
		Field sessionfield = standardSessionFacade.getDeclaredField("session");
		sessionfield.setAccessible(true);
		HttpSession httpSession = (HttpSession) sessionfield.get(session);
		sessionfield.setAccessible(false);
		Class<?> standardSession = httpSession.getClass();
		Field attributesField = standardSession.getDeclaredField("attributes");
		attributesField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) attributesField.get(httpSession);
		attributesField.setAccessible(false);
		return map;

	}

	public static String getUrlSessionId(String url) {
		int i = url.indexOf("?");
		if (i != -1) {
			url = url.substring(i + 1, url.length());
			String[] strs = url.split("&");
			for (String string : strs) {
				String[] ss = string.split("=");
				if (ss[0].equals(SESSION_ID)) {
					return ss[1];
				}
			}
		}
		return null;
	}

	public static void main1(String[] args) {
		System.out.println(MD5("com.biz.primus.base.session"));

	}

	public static boolean verify(String content, String sign, HttpServletRequest request)
			throws InvalidKeyException, NoSuchAlgorithmException {
		switch (ALGORITHM_SWITCH) {
		case 1:
			return Objects.equals(SHA256Util.getSHA256Str(content), sign);
		case 2:
			return HMAC_SHA1.genHMAC(content, sign).equals(request.getSession(false).getAttribute(SessionUtil.PRIVATE_KEY).toString());
		}
		return CipherHelper.verify(content, sign, request.getSession(false).getAttribute(SessionUtil.PUBLIC_KEY).toString());
	}
}
