package com.android.star.barclay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

import database.Credentials;
import database.MySQLiteHelper;


public class LoginActivity extends Activity {

	ArrayList<ImageView> keypadImageArray = new ArrayList<ImageView>();
	EditText editText;
	Button keyboardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//ensure keyboard is showing
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		//add imageview objects for keypad gui to array list
		keypadImageArray.add((ImageView) findViewById(R.id.imageView1));
		keypadImageArray.add((ImageView) findViewById(R.id.imageView2));
		keypadImageArray.add((ImageView) findViewById(R.id.imageView3));
		keypadImageArray.add((ImageView) findViewById(R.id.imageView4));
		keypadImageArray.add((ImageView) findViewById(R.id.imageView5));

		ImageButton settingsBtn = (ImageButton)findViewById(R.id.imageButton);
		settingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread settingsThread = new Thread() {
					@Override
					public void run() {
						Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(intent);
						LoginActivity.this.finish();
					}
				};
				settingsThread.start();
			}
		});


		editText = (EditText)findViewById(R.id.editText1);
		editText.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				int strLength=s.length();
				for(int x = 0; x <= 4; x++){
					if(x+1 > strLength){
						keypadImageArray.get(x).setImageResource(R.drawable.off);
					}else{
						keypadImageArray.get(x).setImageResource(R.drawable.on);
					}
				}

				//check if passcode matches
				if(strLength == 5){
					//login attempt
					MySQLiteHelper db = new MySQLiteHelper(getBaseContext());
					Credentials c = db.getCred(1);

					//check passcode matches and login if true else display an error message
					if(c.getPass().equals("")){ //if account not setup yet
						keyboardView.setText("Must setup account before logging in!");
						editText.setText("");
					}else if(s.toString().equals(c.getPass())){//if password correct change view and login
						Thread mainThread = new Thread() {
							@Override
							public void run() {
								Intent intent = new Intent(LoginActivity.this, WebActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
								startActivity(intent);
								LoginActivity.this.finish();
							}
						};
						mainThread.start();
					}else{//if password incorrect
						editText.setText("");
						keyboardView.setText("Incorrect passcode please try again!");
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});

		keyboardView = (Button)findViewById(R.id.keyboardView);
		keyboardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	@Override
	public void onBackPressed() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}
}
