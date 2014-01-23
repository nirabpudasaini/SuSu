package com.nirab.susu;

import java.io.File;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.os.Bundle;
import android.os.Environment;

public class MapToiletsActivity extends MapActivity {

	MapView mv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maptoilet);
		mv = (MapView) findViewById(R.id.mapView);
		mv.setClickable(true);
		mv.setBuiltInZoomControls(true);
		mv.setMapFile(new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/susu/data/kathmandu-gh/kathmandu.map"));
	}

}
