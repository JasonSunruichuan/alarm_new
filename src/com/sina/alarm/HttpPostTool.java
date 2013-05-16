package com.sina.alarm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.loopj.android.http.*;

public class HttpPostTool {
    public static final String SERVICEURL = "http://sdk.open.api.igexin.com/service";
    public static final int CONNECTION_TIMEOUT_INT = 8000;
    public static final int READ_TIMEOUT_INT = 5000;

    private static final String BASE_URL = "";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(CONNECTION_TIMEOUT_INT);
        client.addHeader("Referer", "http://sina.com.cn");
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(CONNECTION_TIMEOUT_INT);
        client.addHeader("Referer", "http://sina.com.cn");
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


    public static String httpPost(Map<String, Object> map, String serviceUrl) {
        String result = ""; // 获取服务器返回数据
        String postStr = "";
        Set<Map.Entry<String, Object>> set = map.entrySet();
        for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
            String key = entry.getKey();
            String value = entry.getValue().toString();
            postStr += "&" + key + "=" + value;
        }
        //String param = JSONObject.toJSONString(map);
        Log.d("post String", postStr);
        if (postStr != null) {

            URL url = null;

            try {
                url = new URL(serviceUrl);
                Log.d("http post", serviceUrl);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true); // 设置输入流采用字节流
                urlConn.setDoOutput(true); // 设置输出流采用字节流
                urlConn.setRequestMethod("POST");
                urlConn.setUseCaches(false); // 设置缓存
                urlConn.setRequestProperty("Charset", "utf-8");
                urlConn.setRequestProperty("Referer", "http://sina.com.cn");
                urlConn.setConnectTimeout(CONNECTION_TIMEOUT_INT);
                urlConn.setReadTimeout(READ_TIMEOUT_INT);

                urlConn.connect(); // 连接既往服务端发送消息

                DataOutputStream dop = new DataOutputStream(urlConn.getOutputStream());
                dop.write(postStr.getBytes("utf-8")); // 发送参数
                dop.flush(); // 发送，清空缓存
                dop.close(); // 关闭

                // 下面开始做接收工作
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                String readLine = null;
                while ((readLine = bufferReader.readLine()) != null) {
                    result += readLine;
                }
                bufferReader.close();
                urlConn.disconnect();

                System.out.println("result： " + result);
                if (result.equals("")) {
                    result = "{\"result\":{\"status\":{\"code\":11,\"msg\":\"网络不给力,稍后再试 result为空\"},\"data\":[]}}";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                result = "{\"result\":{\"status\":{\"code\":11,\"msg\":\"网络不给力,稍后再试\"},\"data\":[]}}";

            }
        }
        return result;
    }
}