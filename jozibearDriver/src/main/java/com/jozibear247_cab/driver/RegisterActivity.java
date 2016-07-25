package com.jozibear247_cab.driver;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.colorpicker.dialog.ColorPickerDialogFragment;
import com.jozibear247_cab.driver.base.ActionBarBaseActivitiy;
import com.jozibear247_cab.driver.fragment.LoginFragment;
import com.jozibear247_cab.driver.fragment.RegisterFragment;
import com.jozibear247_cab.driver.fragment.UberMainFragment;
import com.jozibear247_cab.driver.gcm.GCMRegisterHendler;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;

/**
 * @author Kishan H Dhamat
 * 
 */
public class RegisterActivity extends ActionBarBaseActivitiy implements ColorPickerDialogFragment.ColorPickerDialogListener{
	public ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		actionBar = getSupportActionBar();
		
		// addFragment(new UberMainFragment(), false,
		// AndyConstants.MAIN_FRAGMENT_TAG);
		if (getIntent().getBooleanExtra("isSignin", false)) {

			addFragment(new LoginFragment(), true,
					AndyConstants.LOGIN_FRAGMENT_TAG, false);
		} else {
			addFragment(new RegisterFragment(), true,
					AndyConstants.REGISTER_FRAGMENT_TAG, false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btnActionNotification:
			onBackPressed();
			break;

		default:
			break;
		}

	}

	public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
		if (mHandleMessageReceiver != null) {
			AndyUtils.showCustomProgressDialog(this, "", getResources()
					.getString(R.string.progress_loading), false);
			new GCMRegisterHendler(RegisterActivity.this,
					mHandleMessageReceiver);

		}
	}

	public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
		if (mHandleMessageReceiver != null) {

			if (mHandleMessageReceiver != null) {
				unregisterReceiver(mHandleMessageReceiver);
			}

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		Fragment signinFragment = getSupportFragmentManager()
				.findFragmentByTag(AndyConstants.LOGIN_FRAGMENT_TAG);
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				AndyConstants.REGISTER_FRAGMENT_TAG);
		if (fragment != null && fragment.isVisible()) {

			goToMainActivity();
		} else if (signinFragment != null && signinFragment.isVisible()) {
			goToMainActivity();
		} else {
			super.onBackPressed();
		}

	}

	public void showColorPicker(int color) {
		ColorPickerDialogFragment f = ColorPickerDialogFragment
				.newInstance(0, null, null, Color.BLACK, false);

		f.setStyle(DialogFragment.STYLE_NORMAL, R.style.DarkPickerDialogTheme);
		f.show(getFragmentManager(), "d");
	}

	@Override
	public void onColorSelected(int dialogId, int color) {
		RegisterFragment fragment = (RegisterFragment)getSupportFragmentManager().findFragmentByTag(
				AndyConstants.REGISTER_FRAGMENT_TAG);
		if (fragment != null && fragment.isVisible()) {
			fragment.setColor(color);
		}
	}

	@Override
	public void onDialogDismissed(int dialogId) {

	}
}
