package jp.atsfky.salarycalc;


import jp.atsfky.salarycalc.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;


public class MyPrefActivity extends PreferenceActivity implements OnPreferenceChangeListener{
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}


}
