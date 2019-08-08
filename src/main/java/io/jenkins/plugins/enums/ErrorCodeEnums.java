package io.jenkins.plugins.enums;

/**
 * 返回错误码枚举
 */
public enum ErrorCodeEnums {

	// 系统错误码
	ERROR_200("200", "成功", ""),
	ERROR_400("400", "失败", ""),
	ERROR_401("401", "无必须传递参数", ""),
	ERROR_404("404", "找不到页面", ""),
	
	ERROR_501("501", "返回参数为空", ""),
	ERROR_502("502", "文件超过长传大小限制", ""),
	
	ERROR_997("997", "用户无访问权限", ""),
	ERROR_998("998", "用户未登陆", ""),
	ERROR_999("999", "系统异常", ""),
	;
	
	private ErrorCodeEnums(String code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 备用字段
     */
    private String description;

    /**
     * 根据code获取message
     * @param code
     * @return
     */
    public static String getMessage(String code) {
        for (ErrorCodeEnums c : ErrorCodeEnums.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c.message;
            }
        }
        return null;
    }

    /**
     * 根据message获取code
     * @param message
     * @return
     */
    public static String getCode(String message) {
        for (ErrorCodeEnums c : ErrorCodeEnums.values()) {
            if (c.getMessage().equalsIgnoreCase(message)) {
                return c.code;
            }
        }
        return null;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
