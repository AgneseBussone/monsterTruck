package monster_truck_control.mtc.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import monster_truck_control.mtc.udp.UdpManager;

/**
 * This listener manages the steering wheel feature.
 */
public class MotionManager implements SensorEventListener {

    private enum Direction {RIGHT, STRAIGHT, LEFT}

    private static final String LOG_TAG = MotionManager.class.getSimpleName();
//    private Sensor rotSensor;
    private Sensor accSensor;
    private Sensor magFieldSensor;
    private float[] values = new float[3];
    private float[] R = new float[9];
    private float[] I = new float[9];
    private float[] gravity_values = new float[3];
    private float[] geomagnetic_values = new float [3];

    private Direction direction = Direction.STRAIGHT;

    public MotionManager(SensorManager manager){
//        rotSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magFieldSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void registerMotion(SensorManager manager){
        //ask for 10ms updates
//        manager.registerListener(this, rotSensor, 10000);
        manager.registerListener(this, accSensor, 10000);
        manager.registerListener(this, magFieldSensor, 10000);
    }

    public void unregisterMotion(SensorManager manager){
//        manager.unregisterListener(this, rotSensor);
        manager.unregisterListener(this, accSensor);
        manager.unregisterListener(this, magFieldSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            gravity_values[0] = event.values[0];
            gravity_values[1] = event.values[1];
            gravity_values[2] = event.values[2];
        }
        else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            geomagnetic_values[0] = event.values[0];
            geomagnetic_values[1] = event.values[1];
            geomagnetic_values[2] = event.values[2];
        }
//        else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

        /* Get the rotation matrix R */
        SensorManager.getRotationMatrix(R, I, gravity_values, geomagnetic_values);

        /* Get the device orientation based on the rotation matrix
        * values[0]: Azimuth, angle of rotation about the -z axis.
        * values[1]: Pitch, angle of rotation about the x axis.
        * values[2]: Roll, angle of rotation about the y axis.*/
        SensorManager.getOrientation(R, values);

        //right now, it doesn't recognize if the device is flat or not. Seems that the coord sys doesn't follow the rotation,
        //so you're still using the rotation around x....
        Direction current_dir;
        if(values[1] > 0.4)
            current_dir = Direction.LEFT;
        else if(values[1] < -0.4)
            current_dir = Direction.RIGHT;
        else
            current_dir = Direction.STRAIGHT;
        if(current_dir != direction){
            direction = current_dir;
            switch(direction){
                case STRAIGHT:{
                    UdpManager.sendMessage(UdpManager.DIRECTION, UdpManager.STRAIGHT);
                    Log.i(LOG_TAG, "STRAIGHT");
                }break;
                case RIGHT: {
                    UdpManager.sendMessage(UdpManager.DIRECTION, UdpManager.RIGHT);
                    Log.i(LOG_TAG, "RIGHT");
                }break;
                case LEFT: {
                    UdpManager.sendMessage(UdpManager.DIRECTION, UdpManager.LEFT);
                    Log.i(LOG_TAG, "LEFT");
                }break;
            }
        }
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
