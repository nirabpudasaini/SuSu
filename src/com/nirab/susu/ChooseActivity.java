package com.nirab.susu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseActivity extends Activity {
	
	Button mapBtn, viewBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);
		mapBtn = (Button) findViewById(R.id.btn_maptoilets);
		viewBtn = (Button) findViewById(R.id.btn_viewtoilets);
		
		mapBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openMap = new Intent(ChooseActivity.this,
						MapToiletsActivity.class);
				startActivity(openMap);
				
			}
			
		});
		
		viewBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openList = new Intent(ChooseActivity.this,
						ListToiletsActivity.class);
				startActivity(openList);
				
			}
		});
		
	}
	
	

}
