package org.flyinggecko.nb_android_remote;

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
	
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] _deltaRotationVector = new float[4];
    private float _timestamp;
    private static final double EPSILON = 0.00001;
    
    // Variablen für Neverball: _r_nb und _c_nb sollten so behandelt werden wie hier beschrieben:
    // https://github.com/jfietkau/neverball-fbiuhh/blob/master/client_java/NbNetController.java
    // und in der Aufgabenstellung
    private short _z_nb, _x_nb, _r_nb, _c_nb;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		 _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	@Override
	 public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
		      getGyroscope(event);
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
	
	private void getGyroscope(SensorEvent event)
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
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        
        // y = hoch und runter -> Neverball: x
        // x = links und rechts -> Neverball: z
        
        //TODO: Wir sollten uns überlegen, wie wir das ganze abhängig zur eigentlichen Rotation gestalten. 
        if (_deltaRotationVector[0] > 0f)
        {
        	_z_nb = (short) Math.min(_z_nb - 1000, -32000);
        }
        if (_deltaRotationVector[0] < 0f)
        {
        	_z_nb = (short) Math.max(_z_nb + 1000, 32000);
        }
        if (_deltaRotationVector[1] > 0f)
        {
        	_x_nb = (short) Math.min(_x_nb - 1000, -32000);
        }
        if (_deltaRotationVector[1] < 0f)
        {
        	_x_nb = (short) Math.max(_x_nb + 1000, 32000);
        }
        //TODO: senden der daten. das senden sollte so etwa 60/sekunde sein. eventuell auch etwas weniger.. vllt so 55
        
	}

}
