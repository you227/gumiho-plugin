package io.jenkins.plugins.util;

import java.io.UnsupportedEncodingException;

public class StringUtil {
	
	/**
	 * @param param
	 * @return 将字符串转为设置编码
	 * @throws UnsupportedEncodingException
	 */
	public static String encodingStr(String param) throws UnsupportedEncodingException {
		String sysEncode = System.getProperty("file.encoding");
		return new String(param.getBytes(sysEncode), Constant.CHARSET);
	}

}
