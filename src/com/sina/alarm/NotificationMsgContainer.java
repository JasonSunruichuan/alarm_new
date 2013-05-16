package com.sina.alarm;

import java.util.HashMap;
import java.util.Map;

public class NotificationMsgContainer {

    private static Map<String, Map<String, String>> msgStack = new HashMap<String, Map<String, String>>();

    public static Map<String, String> getMsg(String content, int position) {
        String key = Tool.md5(content);
        Map<String, String> mm = new HashMap<String, String>();
        if (msgStack.containsKey(key)) {//if this msg already exists
            Map<String, String> m = msgStack.get(key);
            int count = Integer.parseInt(m.get("count")) + 1;
            mm.put("count", count + "");
            mm.put("content", content);
            mm.put("position", m.get("position"));
            msgStack.put(key, mm);
        } else { // msg does not exist
            mm.put("count", "1");
            mm.put("content", content);
            mm.put("position", position + "");
            msgStack.put(Tool.md5(content), mm);
        }
        return mm;
    }

}
