package com.meiqia.meiqiasdk.model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClueCardMessage extends BaseMessage {

    private JSONObject attrs;
    private final Map<String,Boolean> enableStateMap;

    public ClueCardMessage() {
        attrs = new JSONObject();
        enableStateMap = new HashMap<>();
        setItemViewType(TYPE_CLUE_CARD);
    }

    public JSONObject getAttrs() {
        return attrs;
    }

    public void setAttrs(JSONObject attrs) {
        this.attrs = attrs;
    }

    public void setEnable(String name, boolean isEnable) {
        enableStateMap.put(name, isEnable);
    }

    public boolean isEnable(String name) {
        Boolean aBoolean = enableStateMap.get(name);
        return aBoolean != null && aBoolean;
    }

    public boolean isAllEnable() {
        for (boolean isItemEnable : enableStateMap.values()) {
            if (isItemEnable) {
                return true;
            }
        }
        return false;
    }

}
