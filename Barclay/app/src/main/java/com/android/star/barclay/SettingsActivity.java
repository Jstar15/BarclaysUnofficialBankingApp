package com.android.star.barclay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import database.Credentials;
import database.MySQLiteHelper;

public class SettingsActivity extends Activity {
	EditText surnameText, accountText, sortText, passText,secretText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		//initiate text fields
		surnameText = (EditText)findViewById(R.id.surnameText);
		sortText = (EditText)findViewById(R.id.sortText);
		accountText = (EditText)findViewById(R.id.accountText);
		passText = (EditText)findViewById(R.id.passText);
		secretText = (EditText)findViewById(R.id.secretText);

		Button resetBtn = (Button)findViewById(R.id.resetButton);
		resetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ResetTextBox();
			}
		});

		Button saveBtn = (Button)findViewById(R.id.saveButton);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveTextBoxToProp();
			}
		});
	}

	private void ResetTextBox(){
		surnameText.setText("");
		sortText.setText("");
		accountText.setText("");
		passText.setText("");
		secretText.setText("");
		Toast.makeText(SettingsActivity.this, "Reset Successfully", Toast.LENGTH_SHORT).show();
	}

	private void SaveTextBoxToProp(){
		//validate and save to prop
		Credentials c = new Credentials(surnameText.getText().toString(), sortText.getText().toString(), accountText.getText().toString(), passText.getText().toString(), secretText.getText().toString());
		MySQLiteHelper db = new MySQLiteHelper(getBaseContext());
		db.addCred(c);
		Toast.makeText(SettingsActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		Thread LoginThread = new Thread() {
			@Override
			public void run() {
				Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				SettingsActivity.this.finish();
			}
		};
		LoginThread.start();
	}
}
