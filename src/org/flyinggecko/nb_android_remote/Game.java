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
	
	//Variablen für die Gyroscopeberechnungen
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] _deltaRotationVector = new float[4];
    private float _timestamp;
    private static final double EPSILON = 0.00001;
    
    // Variablen für Neverball: _r_nb und _c_nb sollten so behandelt werden wie hier beschrieben:
    // https://github.com/jfietkau/neverball-fbiuhh/blob/master/client_java/NbNetController.java
    // und in der Aufgabenstellung
    private short _z_nb, _x_nb, _r_nb, _c_nb;
	
    private Timer _timer;
    
    
    private TextView _sendOutput;

	private float[] _Gravs;
	private float[] _GeoMags;
	
	private boolean _GravB;
	private boolean _GeoB;
	
    float[] _InclinationM;
    float[] _RotationM;
    
    float[] _radian;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_game);
		_sendOutput = (TextView) findViewById(R.id.sendOutput);
		 _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//		 _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
		 
		 _Gravs = new float[3];
		 _GeoMags = new float[3];
		 _GravB = false;
		 _GeoB = false;
		 _InclinationM = new float[9];
		 _RotationM = new float[9];
		 _radian = new float[3];
		 
//		 _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
//		 _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
		 
		 _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		 _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
		 
//		 run();
		 _sendOutput.setText("test");
		 
	}

	private void run() {
		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			public void run() {
				
				
				_sendOutput.setText(_sendOutput.getText() + "x = " + _x_nb + " ; z = " + _z_nb + "\n");
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
		// synchronized (this)
		// {
		// _sendOutput.setText("beforeSensor");
		// // if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
		// // getOrientation(event);
		// // }
		// switch (event.sensor.getType())
		// {
		// case Sensor.TYPE_GYROSCOPE:
		// _sendOutput.setText("testSensor");
		// getOrientation(event);
		// break;
		//
		// default:
		// break;
		// }
		//
		// }

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
			boolean success = SensorManager.getRotationMatrix(_RotationM, _InclinationM, _Gravs, _GeoMags);
			if (success)
			{
				SensorManager.getRotationMatrix(_RotationM, _InclinationM,
						_Gravs, _GeoMags);
//				SensorManager.remapCoordinateSystem(_RotationM, SensorManager.AXIS_X, SensorManager.AXIS_Y, _RotationM);
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
				} else {
					_sendOutput.setText("x = " + _x_nb
							+ " ; z = " + _z_nb + "\n");
					
				}
				ConnectionHolder.write(_x_nb);
				ConnectionHolder.write(_z_nb);
				ConnectionHolder.write(_r_nb);
				ConnectionHolder.write(_c_nb);

				ConnectionHolder.flush();
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
	
	private void getOrientation(SensorEvent event)
	{
		
		if (_timestamp != 0) {
            final float dT = (event.timestamp - _timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            _deltaRotationVector[0] = sinThetaOverTwo * axisX;
            _deltaRotationVector[1] = sinThetaOverTwo * axisY;
            _deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            _deltaRotationVector[3] = cosThetaOverTwo;
        }
        _timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, _deltaRotationVector);
        float[] radians = new float[3];
        SensorManager.getOrientation(deltaRotationMatrix, radians);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        
        // z = hoch und runter -> Neverball: z
        // x = links und rechts -> Neverball: x
        
        if (radians[1] < 0.0f)
        {
        	_x_nb = (short) (-1 * (1 / 1+ Math.exp( - Math.abs(radians[1]) * 32000)));
        } else if (radians[1] >= 0.0f)
        {
        	_x_nb = (short) (1 / ( 1 + Math.exp( - radians[1])) * 32000);
        }
        if (radians[0] < 0.0f)
        {
        	_z_nb = (short) (-1 * (1 / 1+ Math.exp( - Math.abs(radians[0]) * 32000)));
        }
        else if (radians[0] >= 0.0f)
        {
        	_z_nb = (short) (1 / ( 1 + Math.exp( - radians[0])) * 32000);
        }
        
        _sendOutput.setText(_sendOutput.getText() + "x = " + _x_nb + " ; z = " + _z_nb + "\n");
		ConnectionHolder.write(_x_nb);
		ConnectionHolder.write(_z_nb);
		ConnectionHolder.write(_r_nb);
		ConnectionHolder.write(_c_nb);
		
		ConnectionHolder.flush();
        
	}

}
