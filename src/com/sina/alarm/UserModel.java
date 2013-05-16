package com.sina.alarm;

import java.util.HashMap;
import java.util.Map;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.util.Log;

public class UserModel {
    public static String username = "";
    public static String client_id = "";
    public static boolean updated = false;
    public static int msg_first_id = 0;
    public static int session_msg_first_id = 0;
    public static String session = "";
    public static boolean clean = false;
    //public static String baseUrl = "http://wangxin3.alarm.mix.sina.com.cn/?p=report&s=client&";
    public static String baseUrl = "http://alarm.mix.sina.com.cn/?p=report&s=client&";

    public static void clean() {
        username = "";
        client_id = "";
        updated = false;
        msg_first_id = 0;
        session_msg_first_id = 0;
        session = "";
        clean = true;
    }

    public static void updateClientId() {
        if (updated == false && false == username.equals("")) {
            String serviceUrl = baseUrl + "a=updateClientId&format=json";

            RequestParams params = new RequestParams();
            params.put("username", username);
            params.put("client_id", client_id);
            HttpPostTool.post(serviceUrl, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    Log.d("updateClientId", response);
                    updated = true;
                }
            });


        } else {
            Log.d("updateClientId failed ", "failed");
        }

    }

    public static void setMsgFirstId(int id) {

        if (msg_first_id == 0) {
            msg_first_id = id;
        } else {
            if (msg_first_id > id) {
                msg_first_id = id;
            }
        }
    }

    public static void setSessionMsgFirstId(int id) {

        if (session_msg_first_id == 0) {
            session_msg_first_id = id;
        } else {
            if (session_msg_first_id > id) {
                session_msg_first_id = id;
            }
        }
    }

    public static String getSessionMsgFirstId() {
        if (session_msg_first_id == 0) return "";
        return session_msg_first_id + "";
    }

    public static String getMsgFirstId() {
        if (msg_first_id == 0) return "";
        return msg_first_id + "";
    }
}
