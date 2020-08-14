package com.lifeofcoder.asynctask.core.result;

/**
 * 包含消息的结果类
 *
 * @author xbc
 * @date 2020/1/13
 */
public abstract class MessageResult extends Result {
    private String errorMsg;

    public MessageResult(boolean success, String errorMsg) {
        super(success);
        this.errorMsg = errorMsg;
    }

    public String getMessage() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean hasErrorMsg() {
        return !isSuccess() && null != errorMsg;
    }

    public static String getMessage(MessageResult messageResult) {
        return getMessage(messageResult, getDefaultMessage());
    }

    public static String getMessage(MessageResult messageResult, String defaultErrorMsg) {
        return null == messageResult ? defaultErrorMsg : messageResult.getMessage();
    }

    /**
     * 业务执行结果返回null的时候的错误提示
     */
    protected static String getDefaultMessage() {
        return "Result返回null.";
    }
}
