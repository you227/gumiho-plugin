package io.jenkins.plugins;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

import io.jenkins.plugins.enums.ErrorCodeEnums;
import net.sf.json.JSONObject;

@SuppressWarnings("all")
public class BaseResult<T> {

	/**
	 * 响应码
	 */
	public String code;

	/**
	 * 响应信息
	 */
	public String msg;

	/**
	 * 业务数据
	 */
	public T result;

	/********子状态码定义,按需要获取、默认返回为1*********/
	/**
	 * 子状态码1
	 */
	public static final String STATUS_1 = "1";
	/**
	 * 子状态码2
	 */
	public static final String STATUS_2 = "2";
	/**
	 * 子状态码3
	 */
	public static final String STATUS_3 = "3";

	/***
	 * 构建成功返回的resp
	 * 
	 * @return BaseResult
	 */
	public static BaseResult buildSucBaseResult() {
		BaseResult br = new BaseResult();
		br.setCode(ErrorCodeEnums.ERROR_200.getCode());
		br.setMsg(ErrorCodeEnums.ERROR_200.getMessage());
		return br;
	}
	
	/***
	 * 构建成功返回的resp
	 * 
	 * @param msg 响应信息
	 * @return BaseResult
	 */
	public static BaseResult buildSucResult(String msg) {
		if(StringUtils.isEmpty(msg)){
			msg = ErrorCodeEnums.ERROR_200.getMessage();
		}
		BaseResult br = new BaseResult();
		br.setCode(ErrorCodeEnums.ERROR_200.getCode());
		br.setMsg(msg);
		return br;
	}

	/**
	 * 构建成功返回的resp
	 * @param result
	 * @param <T>
	 * @return
	 */
	public static <T> BaseResult buildSucBaseResult(T result) {
		BaseResult br = new BaseResult();
		br.setCode(ErrorCodeEnums.ERROR_200.getCode());
		br.setMsg(ErrorCodeEnums.ERROR_200.getMessage());
		br.setResult(result);
		return br;
	}

	/***
	 * 构建失败返回的resp
	 * 
	 * @return BaseResult
	 */
	public static BaseResult buildFailBaseResult() {
		BaseResult br = new BaseResult();
		br.setCode(ErrorCodeEnums.ERROR_999.getCode());
		br.setMsg(ErrorCodeEnums.ERROR_999.getMessage());
		return br;
	}

	/**
	 * 构建失败返回的resp
	 * @param code 响应码
	 * @return
	 */
	public static BaseResult buildFailBaseResult(String code) {
		BaseResult br = new BaseResult();
		br.setCode(code);
		br.setMsg(ErrorCodeEnums.getMessage(code));
		return br;
	}

	/**
	 * 构建失败返回的resp
	 * @param code 响应码
	 * @param msg 响应信息
	 * @return
	 */
	public static BaseResult buildFailBaseResult(String code, String msg) {
		BaseResult br = new BaseResult();
		br.setCode(code);
		br.setMsg(msg);
		return br;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
	
	public String toString(){
        return JSON.toJSONString(this);
    }

}