package com.sina.alarm;

import java.util.Map;

import org.json.simple.JSONValue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.igexin.slavesdk.MessageManager;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity {
public static String result = "{\"result\":{\"status\":{\"code\":11,\"msg\":\"网络不给力,稍后再试\"},\"data\":[]}}";
public static String username = "";
public static String password = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityStackControlUtil.add(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MessageManager.getInstance().initialize(this.getApplicationContext());
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		if (UserModel.clean) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.clear().commit();
			UserModel.clean = false;
		}

		String username = sharedPref.getString("username", "");
		Log.d("username pref", username.toString());
		if (!username.equals("")) {
			UserModel.username = username;
			// start new activity
			Log.d("aha", "start new activity  like lists");
			this.switchToMessageList();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean isNetworkConnected() {

		// 判断网络是否连接
		ConnectivityManager mConnectivityManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	/** Called when the user clicks the Send button */
	public void authenticate(View view) {

		EditText editText = (EditText) findViewById(R.id.edit_username);
		 username = editText.getText().toString();

		editText = (EditText) findViewById(R.id.edit_password);
		 password = editText.getText().toString();
		if (username.length() < 3 || password.length() < 4) {
			Toast.makeText(this, "请输入邮箱前缀和密码", Toast.LENGTH_SHORT).show();
			return;
		}

		String serviceUrl = UserModel.baseUrl+"a=login&format=json";
		
		RequestParams params = new RequestParams();
		params.put("username", username);
		params.put("password", password);
		
		HttpPostTool.post(serviceUrl, params, new AsyncHttpResponseHandler(){
		    @Override
		    public void onSuccess(String response) {
		    	result = response;
		    	MainActivity.this.afterAuthenticate();
		        System.out.println(response);
		    }
		});
		


	}
	
	public void afterAuthenticate(){
		Map<String, Map<String, Map<String, ?>>> obj = (Map<String, Map<String, Map<String, ?>>>) JSONValue.parse(result);
		if (obj.get("result").get("status").get("code").toString().equals("0")) {
			Log.d("login success", "login success");
			this.setUsername(username);
			UserModel.updateClientId();
			this.switchToMessageList();
		} else {
			Toast.makeText(this,
					obj.get("result").get("status").get("msg").toString(),
					Toast.LENGTH_SHORT).show();
			Log.d("login failed", obj.get("result").get("status").get("msg").toString());
		}
	}

	public void setUsername(String username) {
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("username", username);
		editor.commit();
		UserModel.username = username;
	}

	public String getUsername() {
		return UserModel.username;
	}

	// todo remove later not in using
	public void setClientId(String cid) {
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("client_id", cid);
		editor.commit();
		UserModel.client_id = cid;
	}

	public String getClientId() {
		return UserModel.client_id;
	}

	public void switchToMessageList() {
		Intent intent = new Intent(this, MessageList.class);
		this.startActivity(intent);
	}

	protected void onDestroy() {
		super.onDestroy();
		// 移出管理栈
		ActivityStackControlUtil.remove(this);
	}
	@Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if (keyCode == KeyEvent.KEYCODE_BACK) {
		  ActivityStackControlUtil.finishProgram();  
		  return true;
	  }else{
		 return  super.onKeyDown(keyCode, event);
	  }
	  
	 
	}

}
