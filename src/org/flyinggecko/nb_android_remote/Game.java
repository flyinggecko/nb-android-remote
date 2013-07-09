package org.flyinggecko.nb_android_remote;

import org.flyinggecko.nb_android_remote.helper.*;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Game extends Activity implements SensorEventListener
{
	private SensorManager _sensorManager;
	// Variablen für Neverball: _r_nb und _c_nb sollten so behandelt werden wie
	// hier beschrieben:
	// https://github.com/jfietkau/neverball-fbiuhh/blob/master/client_java/NbNetController.java
	// und in der Aufgabenstellung
	private short _z_nb, _x_nb, _r_nb, _c_nb;

	private Timer _timer;


	private float[] _Gravs;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_Gravs = new float[3];

		_sensorManager.registerListener(this,
				_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		run();
	}

	private void run()
	{
		_timer = new Timer();
		_timer.schedule(new TimerTask()
		{
			public void run()
			{
				Log.w("X", String.valueOf(_x_nb));
				Log.w("Z", String.valueOf(_z_nb));
				ConnectionHolder.write(_x_nb);
				ConnectionHolder.write(_z_nb);
				ConnectionHolder.write(_r_nb);
				ConnectionHolder.write(_c_nb);

				ConnectionHolder.flush();
			}
		}, 0, 60);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		
		
		
		switch (event.sensor.getType())
		{
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, _Gravs, 0, 3);
				break;
			default:
				return;
		}
		
		for (int i = 0 ; i < 3 ; i++)
		{
			if (_Gravs[i] > 9.81f)
			{
				_Gravs[i] = 9.81f;
			}
			
		}
		
		_z_nb = (short)  (- 3300 * _Gravs[0]);
		_x_nb = (short) (- 3300 * _Gravs[1]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		_sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		_sensorManager.registerListener(this,
				_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
	}
}
