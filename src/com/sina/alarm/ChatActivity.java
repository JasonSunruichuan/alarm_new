package com.sina.alarm;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sina.alarm.ChatListView.OnRefreshListener;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ChatActivity extends Activity {
	public static LinkedList<Map<String,String>> data =null;
	public static ChatListView listView ;
	public static BaseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityStackControlUtil.add(this); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		data = new LinkedList<Map<String,String>>();
		listView = (ChatListView) findViewById(R.id.listView_chat_session);

		adapter = new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView tv = new TextView(ChatActivity.this);
				if(data.get(position).get("send_user").equals(UserModel.username)){
					 convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_reply, null);
				}else{
					 convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, null);
				}
		

				TextView textView = (TextView) convertView.findViewById(R.id.textView_item);
				String msg = data.get(position).get("message");
				String send_user = data.get(position).get("send_user");
				if(!send_user.equals("system")){
					msg = send_user+":"+msg;
				}
				textView.setText(msg);
			
				textView = (TextView) convertView.findViewById(R.id.textView_time);
				SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' HH:mm:ss");
				Date dNow = new Date(Long.parseLong(data.get(position).get("time"))*1000 );
				textView.setText(ft.format(dNow));
				
				
				return convertView;
			}
		

			public long getItemId(int position) {
				return position;
			}

			public Object getItem(int position) {
				return data.get(position);
			}

			public int getCount() {
				return data.size();
			}
		};
		
		listView.setAdapter(adapter);
		listView.setonRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				ChatActivity.loadDataR();
				
			}
			
		});
		
		loadData();


		
	}
	public static void loadDataR(){
		 new AsyncTask<Void, Void, Void>() {
				@Override
				protected void onPostExecute(Void result) {
					adapter.notifyDataSetChanged();
					listView.onRefreshComplete();
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					
					String serviceUrl = UserModel.baseUrl+"a=sessionList&session="+UserModel.session+"&format=json&first_id="+UserModel.getSessionMsgFirstId();
					Log.d("CharActivity:serviceUrl", serviceUrl);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("username", UserModel.username);
					String result = HttpPostTool.httpPost(map, serviceUrl);
					Map<String, Map<String, Map<String, ?>>> obj=(Map<String, Map<String, Map<String, ?>>>) JSONValue.parse(result);
					if( obj.get("result").get("status").get("code").toString().equals("0") ){
					JSONArray	 jArray =  (JSONArray) obj.get("result").get("data");
					Map<String,String> m;
					LinkedList<Map<String,String>> data2 = new LinkedList<Map<String,String>>();
					for(Iterator<Map<String, String>> it = jArray.iterator(); it.hasNext();){
						m = it.next();
						data2.addFirst(m);
						
					}
					for(Map<String, String> m2 : data2){
						data.addFirst(m2);
						UserModel.setSessionMsgFirstId(Integer.parseInt(m2.get("id"))); 
					}
				
					}else{
						Log.d("fetch new lists failed", obj.get("result").get("status").get("msg").toString());
					}

					
					
					return null;
				}

				
			}.execute();
	}
	public static void loadData(){
		 new AsyncTask<Void, Void, Void>() {
				@Override
				protected void onPostExecute(Void result) {
					adapter.notifyDataSetChanged();
					listView.onRefreshComplete();
					listView.setSelection(adapter.getCount()-1);
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					
					String serviceUrl = UserModel.baseUrl+"a=sessionList&session="+UserModel.session+"&format=json&first_id=";
					Log.d("CharActivity:serviceUrl", serviceUrl);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("username", UserModel.username);
					String result = HttpPostTool.httpPost(map, serviceUrl);
					Map<String, Map<String, Map<String, ?>>> obj=(Map<String, Map<String, Map<String, ?>>>) JSONValue.parse(result);
					if( obj.get("result").get("status").get("code").toString().equals("0") ){
					JSONArray	 jArray =  (JSONArray) obj.get("result").get("data");
					Map<String,String> m;
					for(Iterator<Map<String, String>> it = jArray.iterator(); it.hasNext();){
						m = it.next();
						data.addLast(m);
						UserModel.setSessionMsgFirstId(Integer.parseInt(m.get("id"))); 
					}
				
					}else{
						Log.d("fetch new lists failed", obj.get("result").get("status").get("msg").toString());
					}

					
					
					return null;
				}

				
			}.execute();
	}
	
	public void postMsg(View view){
		final EditText editText =	(EditText)findViewById(R.id.editText_input);
		String content = editText.getText().toString();
		if(content.equals("")){
			Toast.makeText(this, "再多敲点内容吧", Toast.LENGTH_SHORT).show();
		}else{
			//post msg
			String serviceUrl = UserModel.baseUrl+"a=message&format=json";
			Log.d("CharActivity:postMsg", serviceUrl);
			RequestParams params = new RequestParams();
			params.put("session", UserModel.session);
			params.put("from", UserModel.username);
			params.put("content", content);
			HttpPostTool.get(serviceUrl, params, new AsyncHttpResponseHandler(){
			    @Override
			    public void onSuccess(String response) {
			     	Log.d("response", response);
					resetListView();
					editText.setText("");
					InputMethodManager imm = (InputMethodManager)getSystemService(ChatActivity.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

			    }
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.logout:
	        	Toast.makeText(this, "奋力注销ing", Toast.LENGTH_SHORT).show();
	        	
	    		UserModel.clean();
	        	Intent intent = new Intent(this, MainActivity.class);
	    		this.startActivity(intent);
	            return true;
	        case R.id.do_not_disturb:
	            
	        	Toast.makeText(this, "宁静!", Toast.LENGTH_SHORT).show();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public static void resetListView(){
		data.removeAll(data);
		UserModel.session_msg_first_id = 0;
		adapter.notifyDataSetChanged();
		ChatActivity.loadData();
		
	}
	protected void onDestroy() {
		super.onDestroy();
		// 移出管理栈
		ActivityStackControlUtil.remove(this);
	}

}
