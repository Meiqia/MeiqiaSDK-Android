package com.meiqia.meiqiasdk.model;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/5 下午3:43
 * 描述:
 */
public class RobotMessage extends BaseMessage {
    public static final int EVALUATE_USEFUL = 1;
    public static final int EVALUATE_USELESS = 0;

    public static final String SUB_TYPE_EVALUATE = "evaluate";
    public static final String SUB_TYPE_REDIRECT = "redirect";
    public static final String SUB_TYPE_REPLY = "reply";
    public static final String SUB_TYPE_MESSAGE = "message";
    public static final String SUB_TYPE_MENU = "menu";
    public static final String SUB_TYPE_QUEUEING = "queueing";
    public static final String SUB_TYPE_MANUAL_REDIRECT = "manual_redirect";
    public static final String SUB_TYPE_UNKNOWN = "unknown";

    private String subType;
    private String contentRobot;
    private String extra;
    private long questionId;
    private boolean isAlreadyFeedback;

    public RobotMessage() {
        setItemViewType(TYPE_ROBOT);
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getContentRobot() {
        return contentRobot;
    }

    public void setContentRobot(String contentRobot) {
        this.contentRobot = contentRobot;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public boolean isAlreadyFeedback() {
        return isAlreadyFeedback;
    }

    public void setAlreadyFeedback(boolean alreadyFeedback) {
        isAlreadyFeedback = alreadyFeedback;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
