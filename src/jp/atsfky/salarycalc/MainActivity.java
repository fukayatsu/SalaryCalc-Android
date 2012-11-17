package jp.atsfky.salarycalc;



import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import jp.atsfky.salarycalc.R;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
    static final String TAG = "MainActivity";

	TextView textView02, textView04, textView06, textView08, textView10, textView11;
	Button button01, button02;

	SharedPreferences sp;
	Handler handler;
	Runnable runnable;
	Timer timer;
	TimerTask task;


	//定数

    /*
     * アクティビティのライフサイクル
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Configuration config = getResources().getConfiguration();
    	if(config.orientation==Configuration.ORIENTATION_LANDSCAPE){
    		//横画面の時、前画面表示
    		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	}

    	if(sp.getBoolean("keep_screen_on", false)){
    		//screen ON
    		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}else{
    		//screen ON / OFF
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}

    	setContentView(R.layout.main);

        textView02 = (TextView) findViewById(R.id.textView2);
    	textView04 = (TextView) findViewById(R.id.textView4);
    	textView06 = (TextView) findViewById(R.id.textView6);
    	textView08 = (TextView) findViewById(R.id.textView8);
    	textView10 = (TextView) findViewById(R.id.textView10);
    	textView11 = (TextView) findViewById(R.id.textView11);//10と同じ内容

    	button01 = (Button) findViewById(R.id.button1);
    	button02 = (Button) findViewById(R.id.button2);

    	button01.setOnClickListener(this);
    	button02.setOnClickListener(this);



    	if(sp.getString("basic_houry_rate","null").equals("null")){
    		Intent intent = new Intent(this,MyPrefActivity.class);
    		startActivity(intent);
    	}
    }

    @Override
    public void onResume(){

    	super.onResume();
    	Log.d(TAG, "onResume()");
    	//timer
    	handler = new Handler();
    	runnable = new Runnable(){
			@Override
			public void run() {
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				setViewText(calendar.getTime());
			}
    	};
    	task = new TimerTask(){
			@Override
			public void run() {
				handler.post(runnable);

			}
    	};
    	timer = new Timer();
    	timer.schedule(task, 0,1000);
    }

    @Override
    protected void onPause(){
    	super.onPause();
    	Log.d(TAG, "onPause()");

    	timer.cancel();
    	timer=null;
    	task.cancel();
    	task=null;
    }

    /*
     * メニューボタンの処理
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem item0 = menu.add(Menu.NONE,Menu.FIRST+1,Menu.NONE,"設定");
		item0.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent=new Intent(this,MyPrefActivity.class);
		startActivity(intent);
		return true;
	}

	/*
     * ボタンの処理
     */
	@Override
	public void onClick(View v) {
		final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		//クリック処理
		if(v == button01){
			new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar c = calendar;
					c.set(Calendar.HOUR_OF_DAY, hourOfDay);
					c.set(Calendar.MINUTE, minute);
					sp.edit().putLong("m_start_time", c.getTimeInMillis()).commit();

				}
			}, hour ,minute ,true).show();

		}else if(v==button02){
			//Toast.makeText(this, "お疲れ様でした", Toast.LENGTH_SHORT).show();
			new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar c = calendar;
					c.set(Calendar.HOUR_OF_DAY, hourOfDay);
					c.set(Calendar.MINUTE, minute);
					sp.edit().putLong("m_end_time", c.getTimeInMillis()).commit();

				}
			}, hour ,minute ,true).show();
			Toast.makeText(this, "お疲れー", Toast.LENGTH_SHORT).show();
		}
	}

	/*
     * 1秒ごとの処理
     */
	private void setViewText(Date now){
		//TODO 1秒ごとの処理
		long mStartTime = sp.getLong("m_start_time", -1);
		if(mStartTime == -1){
			textView02.setText("出勤ボタンを押して下さい");
		}else{

			Date d = new Date(mStartTime);
			textView02.setText(String.format("%02d/%02d %02d:%02d", d.getMonth(), d.getDate(), d.getHours(), d.getMinutes()));
			long passed = now.getTime() - mStartTime;
			textView06.setText(Sec2String(passed/1000));

			double pay = getDailyPay(new Date(mStartTime), now);
			textView10.setText(pay2String(pay));
			textView11.setText(pay2String(pay));

		}
		textView08.setText(String.valueOf(getHouryRate()));

		textView04.setText(String.format("%02d/%02d %02d:%02d", now.getMonth(), now.getDate(), now.getHours(), now.getMinutes()));
	}

	/**
	 * 金額を文字列に
	 */
	private String pay2String(double pay){
		int a = (int) Math.floor(pay);
		double b = (pay - a)*10;
		if(a>999){
			return String.format("%d,%03d.%1.0f", a/1000,a%1000,b);
		}else{
			return String.format("%d.%1.0f", a,b);
		}
	}

	/**
	 * 秒を時間、分、秒に
	 */
	private String Sec2String(long sec){
		return String.format("%02d:%02d:%02d", sec/3600,(sec/60)%60,sec%60);
	}

	/**
	 * 一日の報酬を計算する関数
	 * @return
	 */
	private double getDailyPay(Date d1, Date d2){
		int basic = getETInt("basic_houry_rate");
		int ext1 = getETInt("ext1_houry_rate");//25
		int ext2 = getETInt("ext2_houry_rate");//50

		int morning = getETInt("early_morning");//5
		int start = getETInt("start_time");//9
		int end = getETInt("end_time");//18
		int evening = getETInt("late_evening");//22


		double pay=0;

		double d1m = d1.getMinutes() + d1.getSeconds()/60.0;
		int d1h = d1.getHours();

		double d2m = d2.getMinutes() + d2.getSeconds()/60.0;
		int d2h = d2.getHours();

		if(d1h==d2h){
			if(morning<= d1h && d1h < start){
				pay += (d2m-d1m)/60.0*basic*(100+ext1)/100.0;
			}else if(start <= d1h && d1h < end){
				pay += (d2m-d1m)/60.0*basic*(100+0)/100.0;
			}else if(end <= d1h && d1h < evening){
				pay += (d2m-d1m)/60.0*basic*(100+ext1)/100.0;
			}else{
				pay += (d2m-d1m)/60.0*basic*(100+ext2)/100.0;
			}
			return pay;
		}else{

			//最初の端数を計算

			if(morning<= d1h && d1h < start){
				pay += (60-d1m)/60.0*basic*(100+ext1)/100.0;
			}else if(start <= d1h && d1h < end){
				pay += (60-d1m)/60.0*basic*(100+0)/100.0;
			}else if(end <= d1h && d1h < evening){
				pay += (60-d1m)/60.0*basic*(100+ext1)/100.0;
			}else{
				pay += (60-d1m)/60.0*basic*(100+ext2)/100.0;
			}

			//途中の部分を計算
			for(int i = d1h+1; i<d2h; i++){
				if(morning<= i && i < start){
					pay += basic*(100+ext1)/100.0;
				}else if(start <= i && i < end){
					pay += basic*(100+0)/100.0;
				}else if(end <= i && i < evening){
					pay += basic*(100+ext1)/100.0;
				}else{
					pay += basic*(100+ext2)/100.0;
				}
			}


			//最後の端数を計算

			if(morning<= d2h && d2h < start){
				pay += d2m/60.0*basic*(100+ext1)/100.0;
			}else if(start <= d2h && d2h < end){
				pay += d2m/60.0*basic*(100+0)/100.0;
			}else if(end <= d2h && d2h < evening){
				pay += d2m/60.0*basic*(100+ext1)/100.0;
			}else{
				pay += d2m/60.0*basic*(100+ext2)/100.0;
			}

			//休憩時間を計算
			Long tmp = d2.getTime() - d1.getTime();
			if(sp.getBoolean("is_check_breaktime", false)){
				if(tmp > 8*60*60*1000){
					pay -= basic;
				}else if(tmp > 6*45*60*1000){
					pay -= basic*45/60.0;
				}
			}
		}
		return pay;
	}


	/**
	 * 現在の時給を計算する関数
	 * @return int
	 */
	private int getHouryRate(){
		int basic = getETInt("basic_houry_rate");
		int ext1 = getETInt("ext1_houry_rate");//25
		int ext2 = getETInt("ext2_houry_rate");//50

		int morning = getETInt("early_morning");//5
		int start = getETInt("start_time");//9
		int end = getETInt("end_time");//18
		int evening = getETInt("late_evening");//22


		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int rate = basic;
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		if(morning<= hour && hour < start){
			rate += rate*ext1/100;
		}else if(start <= hour && hour < end){

		}else if(end <= hour && hour < evening){
			rate += rate*ext1/100;
		}else{
			rate += rate*ext2/100;
		}
		return rate;
	}

	/**
	 * SharedPreferencesのEditTextのIntの値を返す関数
	 * @param String key
	 * @return int
	 */
	private int getETInt(String key){
		return Integer.parseInt(sp.getString(key, "-1"));
	}

}