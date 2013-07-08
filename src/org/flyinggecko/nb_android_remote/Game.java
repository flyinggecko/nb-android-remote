package org.flyinggecko.nb_android_remote;

import org.flyinggecko.nb_android_remote.helper.*;

import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class Game extends Activity implements SensorEventListener
{
	private SensorManager _sensorManager;
	// Variablen für Neverball: _r_nb und _c_nb sollten so behandelt werden wie
	// hier beschrieben:
	// https://github.com/jfietkau/neverball-fbiuhh/blob/master/client_java/NbNetController.java
	// und in der Aufgabenstellung
	private short _z_nb, _x_nb, _r_nb, _c_nb;

	private Timer _timer;

	private TextView _sendOutput;

	private float[] _Gravs;
	private float[] _GeoMags;
	private boolean _GravB;
	private boolean _GeoB;
	float[] _RotationM;
	float[] _radian;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);
		_sendOutput = (TextView) findViewById(R.id.sendOutput);
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_Gravs = new float[3];
		_GeoMags = new float[3];
		_GravB = false;
		_GeoB = false;
		_RotationM = new float[9];
		_radian = new float[3];

		_sensorManager.registerListener(this,
				_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		_sensorManager.registerListener(this,
				_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
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
				ConnectionHolder.write(_x_nb);
				ConnectionHolder.write(_z_nb);
				ConnectionHolder.write(_r_nb);
				ConnectionHolder.write(_c_nb);

				ConnectionHolder.flush();
			}
		}, 0, 55);

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
				System.arraycopy(event.values.clone(), 0, _Gravs, 0, 3);
				_GravB = true;
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values.clone(), 0, _GeoMags, 0, 3);
				_GeoB = true;
				break;
			default:
				return;
		}

		if (_GravB && _GeoB)
		{
			boolean success = SensorManager.getRotationMatrix(_RotationM, null,
					_Gravs, _GeoMags);
			if (success)
			{
				SensorManager.getRotationMatrix(_RotationM, null,
						_Gravs, _GeoMags);
				SensorManager.getOrientation(_RotationM, _radian);
				if (_radian[1] < 0.0f)
				{
					_x_nb = (short) (-1 * (1 / 1 + Math.exp(-Math
							.abs(_radian[1]) * 32000)));
				}
				else if (_radian[1] >= 0.0f)
				{
					_x_nb = (short) (1 / (1 + Math.exp(-_radian[1])) * 32000);
				}
				if (_radian[0] < 0.0f)
				{
					_z_nb = (short) (-1 * (1 / 1 + Math.exp(-Math
							.abs(_radian[0]) * 32000)));
				}
				else if (_radian[0] >= 0.0f)
				{
					_z_nb = (short) (1 / (1 + Math.exp(-_radian[0])) * 32000);
				}

				if (_sendOutput.length() < 50)
				{
					_sendOutput.setText(_sendOutput.getText() + "x = " + _x_nb
							+ " ; z = " + _z_nb + "\n");
				}
				else
				{
					_sendOutput.setText("x = " + _x_nb + " ; z = " + _z_nb
							+ "\n");
				}
			}
		}
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
				_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_GAME);
	}
}
