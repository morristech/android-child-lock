package com.alimuzaffar.android.childlock.activities;

import java.io.FileInputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alimuzaffar.android.childlock.Constants;
import com.alimuzaffar.android.childlock.R;
import com.alimuzaffar.android.childlock.services.HeartBeat;
import com.alimuzaffar.android.childlock.utils.Utils;

public class LockScreenActivity extends FragmentActivity {
	private static final String TAG = LockScreenActivity.class.getSimpleName();
	EditText pin;
	TextView pinMsg;
	String firstPin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_screen);

		getBlockedAppInfo();

		pin = (EditText) findViewById(R.id.pin);
		pinMsg = (TextView) findViewById(R.id.pinMsg);
		pin.requestFocus();

		pin.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(pin, 0);
			}
		}, 200);
		
		pin.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 4) {
					boolean setPin = getIntent().getBooleanExtra("setPin", false);
					if (setPin) {
						if (firstPin == null) {
							firstPin = s.toString();
							pinMsg.setText("Re-enter your 4-digit PIN");
							pin.setText("");
						} else if (firstPin.equals(s.toString())) {
							// confirm pin
							Intent data = new Intent();
							data.putExtra("pin", firstPin);
							if (getParent() == null) {
								setResult(Activity.RESULT_OK, data);
							} else {
								getParent().setResult(Activity.RESULT_OK, data);
							}
							LockScreenActivity.this.finish();
						} else {
							firstPin = null;
							pin.setText(null);
							pinMsg.setText("PINs did not match. Try again, enter a 4-digit PIN.");
						}
					} else {
						checkPin(s);
					}
				} 
			}
		});
	}

	private void checkPin(Editable s) {
		String savedPin = "";
		try {
			FileInputStream istream = openFileInput(Constants.LOCKED_APP_FILE);
			String jsonStr = Utils.getStringFromInputStream(istream);
			JSONObject jo = new JSONObject(jsonStr);
			savedPin = jo.getString("pin");
		} catch (Exception e) {
			return;
		}
		
		if (s.toString().equals(savedPin)) {
			Intent i2 = new Intent();
			i2.setAction(Constants.ACTION_GRANT_ACCESS);
			i2.putExtra("packageName", getIntent().getStringExtra("packageName"));
			sendBroadcast(i2);

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					LockScreenActivity.this.finish();
				}
			}, 250);
		} else {
			pin.setText(null);
			pinMsg.setText("Incorrect PIN, Please try again.");
		}
	}

	private void getBlockedAppInfo() {
		try {
			ApplicationInfo app = getPackageManager().getApplicationInfo(getIntent().getStringExtra("packageName"), 0);
			Drawable icon = getPackageManager().getApplicationIcon(app);
			CharSequence name = getPackageManager().getApplicationLabel(app);

			((ImageView) findViewById(R.id.appIcon)).setImageDrawable(icon);
			((TextView) findViewById(R.id.appName)).setText(name);
			((TextView) findViewById(R.id.appName)).append(" is blocked.");

		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onBackPressed() {
		// Display confirmation here, finish() activity.
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);

		Intent i = new Intent(LockScreenActivity.this, HeartBeat.class);
		startService(i);

		finish();
		super.onBackPressed();
	}

}
