package com.meiqia.meiqiasdk.model;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/31 下午5:14
 * 描述:
 */
public class EvaluateMessage extends BaseMessage {
    public static final int EVALUATE_GOOD = 2;
    public static final int EVALUATE_MEDIUM = 1;
    public static final int EVALUATE_BAD = 0;

    private int level;

    public EvaluateMessage(int level, String content) {
        this.level = level;
        setContent(content);
        setItemViewType(TYPE_EVALUATE);
    }

    public int getLevel() {
        return level;
    }

}
