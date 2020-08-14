package com.lifeofcoder.asynctask.core.result;

/**
 * 异常错误类结果
 *
 * @author xbc
 * @date 2020/1/16
 */
public abstract class ExceptionMessageResult extends MessageResult {
    private Exception exception;

    public ExceptionMessageResult(boolean success, String errorMsg, Exception exception) {
        super(success, errorMsg);
        this.exception = exception;
    }

    public ExceptionMessageResult(boolean success, Exception exception) {
        super(success, "");
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public static String getMessage(ExceptionMessageResult exceptionMsgResult) {
        if (null == exceptionMsgResult) {
            return getDefaultMessage();
        }

        String message = MessageResult.getMessage(exceptionMsgResult);
        if (null == message) {
            message = "No Message.";
        }

        Exception e  = exceptionMsgResult.getException();
        if (null == e) {
            return message;
        }

        String exceptionMsg = e.getMessage();
        if (null == e) {
            return message;
        }

        return message + ", Exception:" + exceptionMsg;
    }
}
