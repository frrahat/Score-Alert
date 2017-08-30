package com.frrahat.scorealert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView scoreTextView;
	private EditText editText;
	private TextView indicatorTextView;
	private Button btnPrev;
	private Button btnNext;
	private final String cricbuzzURL="http://synd.cricbuzz.com/j2me/1.0/livematches.xml";
	
	private int refreshDelayInSec=30;
	private ArrayList<Pair<Integer, Integer>> LineIndices;
	private int selectedLineIndex=0;
	private int lockedLineIndex=-1;
	
	private String attributeString;
	private boolean isScreenAlwaysOn;
	
	
    Timer timer;
    TimerTask doAsynchronousTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		scoreTextView=(TextView) findViewById(R.id.textViewDelay);
		editText=(EditText) findViewById(R.id.editText1);
		indicatorTextView=(TextView) findViewById(R.id.textViewIndicator);
		btnPrev=(Button) findViewById(R.id.buttonPrev);
		btnNext=(Button) findViewById(R.id.buttonNext);
		
		attributeString="r=+4,wkts=+1,ovrs";
		
		btnPrev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selectedLineIndex>0){
					selectedLineIndex--;
					updateSelection();
				}
			}
		});
		
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selectedLineIndex<LineIndices.size()-1){
					selectedLineIndex++;
					updateSelection();
				}
			}
		});
		
		scoreTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				StringPickerDialog stringPickerDialog=new StringPickerDialog();
				stringPickerDialog.setPrevText(attributeString);
				stringPickerDialog.setInputListener(new TextInputListener() {
					
					@Override
					public void inputGiven(String text) {
						if(text!=null) attributeString=text;
					}
				});
				stringPickerDialog.show(getFragmentManager(), "attribute picker");
			}
		});

		scheduleRefresh();
	}
	
	
	/*private boolean isPeriod(char s){
		if(s<'A' || s>'z') return true;
		if(s>'Z' && s<'a') return true;
		return false;
	}*/
	
	private void Refresh(){
		indicatorTextView.setText("Refreshing ...");
		new RetrieveXmlTask().execute(cricbuzzURL);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		if(isScreenAlwaysOn){
			menu.findItem(R.id.action_toggle_screen_state)//.setIcon(R.drawable.ic_screen_off)
			.setTitle(R.string.action_toggle_screen_to_off);
		}else{
			menu.findItem(R.id.action_toggle_screen_state)//.setIcon(R.drawable.ic_screen_on)
			.setTitle(R.string.action_toggle_screen_to_on);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();		
		if (id == R.id.action_refresh) {
			Refresh();
			return true;
		}
		if (id == R.id.action_lock_line) {
			lockLine();
			return true;
		}
		if (id == R.id.action_set_refresh_delay) {
			setRefreshDelay();
			return true;
		}
		if (id == R.id.action_toggle_screen_state) {
			toggleScreenState();
			return true;
		}
		if (id == R.id.action_help) {
			Intent intent=new Intent(MainActivity.this, ScoreAlertHelpActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void toggleScreenState(){
		isScreenAlwaysOn=!isScreenAlwaysOn;
		if(isScreenAlwaysOn){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}else{
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		invalidateOptionsMenu();
	}
	
	private void lockLine(){
		lockedLineIndex=selectedLineIndex;
		Toast.makeText(this, "locked line index : "+Integer.toString(lockedLineIndex), Toast.LENGTH_SHORT).show();
		updateScoreText();
	}
	
	private void setRefreshDelay(){
		StringPickerDialog stringPickerDialog=new StringPickerDialog();
		stringPickerDialog.setPrevText(Integer.toString(refreshDelayInSec));
		stringPickerDialog.setInputListener(new TextInputListener() {
			
			@Override
			public void inputGiven(String text) {
				if(text!=null){
					try{
						int sec=Integer.parseInt(text.trim());
						if(sec>4){
							refreshDelayInSec=sec;
							timer.cancel();
							scheduleRefresh();
						}
						else{
							timer.cancel();
							indicatorTextView.setText("Refresh Off");
						}
					}catch(NumberFormatException ne){
						
					}
				}
			}
		});
		stringPickerDialog.show(getFragmentManager(), "attribute picker");
	}
	
	private void updateScoreText(){
		if(lockedLineIndex!=-1 && lockedLineIndex<LineIndices.size()){
			String srcText=editText.getText().toString();
			srcText=srcText.substring(LineIndices.get(lockedLineIndex).first, LineIndices.get(lockedLineIndex).second).trim();
			if(attributeString==null || attributeString.length()==0){				
				scoreTextView.setText(srcText);
			}else{
				String[] attrbts=attributeString.split(",");
				String text="";
				String prevScoreTextString=scoreTextView.getText().toString();
				for(int i=0;i<attrbts.length;i++){
					String[] parts=attrbts[i].split("=");
					String attribute=parts[0].trim();
					
					String alertValue="";
					if(parts.length>1) alertValue=parts[1].trim();
					
					String newValueString=parseValue(srcText, attribute);
					
					
					text+=attribute+"=\""+newValueString+"\" ";
						
					//make alert
					if(alertValue!=""){
						boolean checkDiff;
						if(alertValue.charAt(0)=='+'){
							checkDiff=true;
						}else{
							checkDiff=false;
						}
						
						try{							
							int newValue=Integer.parseInt(newValueString);
							if(checkDiff){
								int diff=Integer.parseInt(alertValue.substring(1));
								int preValue=Integer.parseInt(parseValue(prevScoreTextString, attribute));
								if((newValue-preValue)>=diff){
									playBeep(i);
								}
							}else{
								int check=Integer.parseInt(alertValue);
								if(newValue>=check){
									playBeep(i);
								}
							}
						}catch(NumberFormatException ne){
							if(newValueString==alertValue){
								playBeep(i);
							}
						}
					}
					//handling alert done
				}
				scoreTextView.setText(text);
			}
			
		}
	}
	
	private String parseValue(String srcText, String attribute){
		String value="";
		int k=srcText.indexOf(attribute+"=");
		if(k!=-1){
			k=k+attribute.length()+2;
			int p=k;
			while(srcText.charAt(p)!='"' && srcText.charAt(p)!='\'') p++;
			value=srcText.substring(k,p);
		}
		return value;
	}
	
	private void updateLines(){
		String[] lines=editText.getText().toString().split("\n");
		int n=lines.length;
		LineIndices=new ArrayList<Pair<Integer,Integer>>();
		int startIndex=0;
		int endIndex=0;
		for(int i=0;i<n;i++){
			endIndex=startIndex+lines[i].length()+1;
			LineIndices.add(new Pair<Integer, Integer>(startIndex, endIndex));
			startIndex=endIndex;
		}
		
		if(selectedLineIndex<n){
			updateSelection();
		}
	}
	
	private void updateSelection(){
		int startIndex=LineIndices.get(selectedLineIndex).first;
		int endIndex=LineIndices.get(selectedLineIndex).second;
		
		editText.setSelection(startIndex, endIndex);
		//scoreTextView.setText(Integer.toString(startIndex)+":"+Integer.toString(endIndex));
	}
	
	private void playBeep(int type){
		ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);             
		toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_S_X4,1000+type*5000);
		//toneGen1.release();
	}
	
	class RetrieveXmlTask extends AsyncTask<String, Void, String> {

	    protected String doInBackground(String... urls) {
	    	URL cricbuzz = null;
	    	String text="";
	    	//HttpURLConnection httpConn;
			try {
				cricbuzz = new URL(urls[0]);
				/*httpConn=(HttpURLConnection) cricbuzz.openConnection();	
				httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/602.4.8 (KHTML, like Gecko) Version/10.0.3 Safari/602.4.8");
				httpConn.setRequestMethod("GET");*/
			} catch (IOException e) {
				text=e.toString();
				return text+"\n";
			}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(cricbuzz.openStream()));//httpConn.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				    text+=inputLine+"\n";
			} catch (IOException e) {
				text=e.toString();
				return text+"\n";
			}finally{
				try {
					if(in!=null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return text;
	    }

	    protected void onPostExecute(String text) {
	    	editText.setText(text);
	    	indicatorTextView.setText("");
	    	
	    	updateLines();
	    	updateScoreText();
	    }
	}
	
	
	private void scheduleRefresh() {
		final Handler handler = new Handler();
		doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                        Refresh();
	                    } catch (Exception e) {
	                    
	                    }
	                }
	            });
	        }
	    };
	    
		timer=new Timer();
		timer.schedule(doAsynchronousTask, 0, refreshDelayInSec*1000);
	}
}
