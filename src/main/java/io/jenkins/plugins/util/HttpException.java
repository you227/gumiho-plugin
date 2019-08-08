package io.jenkins.plugins.util;

public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 3955685975302826747L;
    
    /**
     * @description 错误编码
     */
    private String errorCode;

    /**
     * @description 消息是否为属性文件中的Key
     */
    private boolean propertiesKey = true;

    /**
     * @description 构造一个基本异常.
     * @param message 信息描述
     */
    public HttpException(String message) {
        super(message);
    }

    /**
     * @description 构造一个基本异常.
     * @param errorCode 错误编码
     * @param message 信息描述
     */
    public HttpException(String errorCode, String message) {
        this(errorCode, message, true);
    }

    /**
     * @description 构造一个基本异常.
     * @param errorCode 错误编码
     * @param message 信息描述
     */
    public HttpException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, true);
    }

    /**
     * @description 构造一个基本异常.
     * @param errorCode 错误编码
     * @param message 信息描述
     * @param propertiesKey 消息是否为属性文件中的Key
     */
    public HttpException(String errorCode, String message, boolean propertiesKey) {
        super(message);
        this.setErrorCode(errorCode);
        this.setPropertiesKey(propertiesKey);
    }

    /**
     * @description 构造一个基本异常.
     * @param errorCode 错误编码
     * @param message 信息描述
     */
    public HttpException(String errorCode, String message, Throwable cause, boolean propertiesKey) {
        super(message, cause);
        this.setErrorCode(errorCode);
        this.setPropertiesKey(propertiesKey);
    }

    /**
     * @description 构造一个基本异常.
     * @param message 信息描述
     * @param cause 根异常类（可以存入任何异常）
     */
    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isPropertiesKey() {
        return propertiesKey;
    }

    public void setPropertiesKey(boolean propertiesKey) {
        this.propertiesKey = propertiesKey;
    }

}
