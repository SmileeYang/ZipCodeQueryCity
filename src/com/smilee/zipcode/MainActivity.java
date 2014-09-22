package com.smilee.zipcode;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import com.smilee.zipcode.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity {

	private final String TAG = "zipcode";

	private String mZipCode, mState, mCity, mTimezone, mDaylightSavingTime, mCurrentTyping = null;
	private long mLastTimeStamp, mCurrTimeStamp, mTimeGap, mFourHours = 14400000;
	private boolean isRecorded = false, isSearchSuccess = false;
	ZipCodeDBHelper mZipCodeDBHelper = null;

	private EditText mTypingZip;
	private Button search;
	private TextView zipcodeInfoText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mZipCodeDBHelper = new ZipCodeDBHelper(this);

		mTypingZip = (EditText) findViewById(R.id.zip);
		search = (Button) findViewById(R.id.search);
		search.setOnClickListener(searchOnClickListener);
		zipcodeInfoText = (TextView) findViewById(R.id.zipcodeinfo);
	}

	@Override
	public void onResume() {
		mTypingZip.setText("");
		isSearchSuccess = false;
		Log.d(TAG, "onResume, isRecorded=" + isRecorded + ", isSearchSuccess=" + isSearchSuccess);
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	Button.OnClickListener searchOnClickListener = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mZipCode = mTypingZip.getText().toString();
			Log.d(TAG, "mZipCode=" + mZipCode + ", mCurrentTyping=" + mCurrentTyping);
			while (mZipCode.length() > 2 && mZipCode.length() < 5) {
				String isThree = "00";
				String isFour = "0";
				if (mZipCode.length() == 3) {
					mZipCode = isThree + mZipCode;
					Log.d(TAG, "Now mZipCode=" + mZipCode);
				} else if (mZipCode.length() == 4) {
					mZipCode = isFour + mZipCode;
					Log.d(TAG, "Now mZipCode=" + mZipCode);
				}
			}
			if (mZipCode.equals("")) {
				Toast.makeText(getBaseContext(), "Please enter zip code!", Toast.LENGTH_SHORT).show();
			} else if (mZipCode.length() < 3) {
				Toast.makeText(getBaseContext(), "Your zip code is too short!", Toast.LENGTH_SHORT).show();
			} else {
				ArrayList<ZipCodeTimeZone> locationList = mZipCodeDBHelper.getZipData(MainActivity.this, mZipCode);
				Log.d(TAG, "getZipCodeData()");
				if (locationList != null && !locationList.isEmpty()) {
					mCurrTimeStamp = System.currentTimeMillis();
					isSearchSuccess = true;
					mTimeGap = mCurrTimeStamp - mLastTimeStamp;
					Log.d(TAG, ">>>>>mTimeGap=" + mTimeGap);
					if (!isRecorded || !mCurrentTyping.equalsIgnoreCase(mZipCode)) {
						mCurrentTyping = mZipCode;
						isRecorded = true;
						mLastTimeStamp = mCurrTimeStamp;
						Log.d(TAG, "---mCurrentType=" + mCurrentTyping + ", mLastTimeStamp=" + mLastTimeStamp);
						for (ZipCodeTimeZone zip : locationList) {
							mZipCode = zip.getZipCode();
							mState = zip.getState();
							mCity = zip.getCity();
							mTimezone = zip.getTimeZone();
							mDaylightSavingTime = zip.getDst();
							Log.d(TAG, "Zip Code: " + zip.getZipCode()
									+ "\nState: " + zip.getState()
									+ "\nCity: " + zip.getCity()
									+ "\nTime Zone: " + zip.getTimeZone()
									+ "\nDaylight Saving Time: " + zip.getDst());
						}
						String showTimezone = mTimezone.substring(1);
						if (showTimezone.length() == 1) {
							mTimezone = mTimezone.substring(0, 1) + "0" + showTimezone + ":00";
						} else if (showTimezone.length() == 2) {
							mTimezone = mTimezone.substring(0, 1) + showTimezone + ":00";
						}
						zipcodeInfoText.setText("City: " + mCity + "\nState: " + mState + "\nTime Zone: " + mTimezone);
					} else if (isRecorded && mCurrentTyping.equalsIgnoreCase(mZipCode) && (mTimeGap <= mFourHours)) {
						Log.d(TAG, "---equals---mTimeGap=" + mTimeGap);
						Log.d(TAG, "City: " + mCity + "\nState: " + mState + "\nTime Zone: " + mTimezone);
					}
				} else {
					Toast.makeText(MainActivity.this, "No such zip code, please try again!", Toast.LENGTH_SHORT).show();
					mTypingZip.setText("");
				}
			}
		}
	};
}
