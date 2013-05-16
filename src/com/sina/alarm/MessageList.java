package com.sina.alarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.sina.alarm.MyListView;
import com.sina.alarm.MyListView.OnRefreshListener;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public  class MessageList extends Activity {


	public static LinkedList<Map<String,String>> data;
	public static BaseAdapter adapter;
	public static  MyListView listView = null;

	public void onCreate(Bundle savedInstanceState) {
		ActivityStackControlUtil.add(this); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		data = new LinkedList<Map<String,String>>();
	

		listView = (MyListView) findViewById(R.id.listView);
		adapter = new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) {
				if( data.get(position).get("level").equals("1")){
					convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_level2, null);
				}else if(data.get(position).get("level").equals("2")){
					convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, null);
				}else if(data.get(position).get("level").equals("3")){
					convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_level3, null);
				}
				
				
				
				TextView textView = (TextView) convertView.findViewById(R.id.textView_item);
				String msg = data.get(position).get("message");
				if(!data.get(position).get("link").equals("")){
					msg += "(link)";	
				}
				textView.setText(msg);
			
				textView = (TextView) convertView.findViewById(R.id.textView_time);
				SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' HH:mm:ss");
				Date dNow = new Date(Long.parseLong(data.get(position).get("time"))*1000 );
				textView.setText(ft.format(dNow));
				//textView = (TextView) convertView.findViewById(R.id.hidden_url);
				//textView.setText(data.get(position).get("link"));
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
		listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				MessageList.loadDataR();
			}
		});
		MessageList.loadData();
		
	}
	
	public static void resetListView(){
		data.removeAll(data);
		UserModel.msg_first_id = 0;
		adapter.notifyDataSetChanged();
		MessageList.loadData();
		
	}
	
	public static void loadData(){
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {				
				//获取数据
				String serviceUrl = UserModel.baseUrl+"a=lists&username="+UserModel.username+"&format=json&first_id="+UserModel.getMsgFirstId();
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
					UserModel.setMsgFirstId(Integer.parseInt(m.get("id")));
				}
		
				}else{
					Log.d("fetch new lists failed", obj.get("result").get("status").get("msg").toString());
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				adapter.notifyDataSetChanged();
				listView.onRefreshComplete();
				listView.setSelection(adapter.getCount()-1);
			}

		}.execute();
	}
	
	public static void loadDataR(){
		new AsyncTask<Object, Object, Object>() {
			protected Object doInBackground(Object... obj2) {				
				//获取数据
				String serviceUrl = UserModel.baseUrl+"a=lists&username="+UserModel.username+"&format=json&first_id="+UserModel.getMsgFirstId();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("username", UserModel.username);
				String result = HttpPostTool.httpPost(map, serviceUrl);
				Map<String, Map<String, Map<String, ?>>> obj=(Map<String, Map<String, Map<String, ?>>>) JSONValue.parse(result);
				LinkedList<Map<String,String>> data2 = new LinkedList<Map<String,String>>();
				if( obj.get("result").get("status").get("code").toString().equals("0") ){
				JSONArray	 jArray =  (JSONArray) obj.get("result").get("data");
				Map<String,String> m;
				for(Iterator<Map<String, String>> it = jArray.iterator(); it.hasNext();){
					m = it.next();
					data2.addFirst(m);
					
				}
				for(Map<String, String> m2 : data2){
					data.addFirst(m2);
					UserModel.setMsgFirstId(Integer.parseInt(m2.get("id"))); 
				}
			
					
		
				}else{
					Log.d("fetch new lists failed", obj.get("result").get("status").get("msg").toString());
				}
				return data2;
			}

			@Override
			protected void onPostExecute(Object result) {
				List l = (List)result;
				adapter.notifyDataSetChanged();
				listView.onRefreshComplete();
				listView.setSelection(l.size()-1);
			}

		}.execute();
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
	@Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if (keyCode == KeyEvent.KEYCODE_BACK) {
		  ActivityStackControlUtil.finishProgram();  
		  return true;
	  }else{
		 return  super.onKeyDown(keyCode, event);
	  }
	  
	 
	}
	
	protected void onDestroy() {
		super.onDestroy();
		// 移出管理栈
		ActivityStackControlUtil.remove(this);
	}

}


