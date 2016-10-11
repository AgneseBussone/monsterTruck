package monster_truck_control.mtc.graphics;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.SeekBar;
import android.util.Log;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

import monster_truck_control.mtc.udp.UdpManager;

/**
 * This class manages the variation of the speed.
 * The logic on the board side works a little bit differently from this one because it's based on an
 * old algorithm; we wanted to change the behavior of the UI without touching the board code.
 * So this listener takes care of sending all message needed by the board that previously were sent
 * by other UI elements.
 * There is not a precise handling of the seekbar values because the car does not have a fine tuning
 * of the speed. We have three states:
 * - SLOW: the speed is increasing but is under 40%
 * - GO: the speed is increasing and go over 40%
 * - STOP: the current speed is less than the previous one
 * Every time we receive a new value, we check the status to avoid sending multiple messages to the board.
 * If the user release the seekbar, it go down automatically and put the car in park.
 */

public class SpeedChangeListener implements SeekBar.OnSeekBarChangeListener {

    private enum State {STOP, SLOW, GO}

    private static final String LOG_TAG = SpeedChangeListener.class.getSimpleName();

    private int previous_speed = 0;
    private State state = State.STOP;
    private volatile char direction = UdpManager.PARK;

    public SpeedChangeListener() {}

    public void setDirection(char direction){
        this.direction = direction;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {
        if (fromUser){
            // the user increase the speed
            if (speed > previous_speed  && speed >= 40){
                if(state != State.GO){
                    UdpManager.sendMessage(UdpManager.ACCELERATOR, UdpManager.ACC_BRK_PRESS);
                    Log.i(LOG_TAG, "accelerator pressed");
                    state = State.GO;
                }
            }
            // brake!
            else if(speed < previous_speed){
                if(state != State.STOP){
                    UdpManager.sendMessage(UdpManager.BRAKE, UdpManager.ACC_BRK_PRESS);
                    Log.i(LOG_TAG, "brake pressed");
                    state = State.STOP;
                }
            }
            // after slow down, the user increase the speed
            else if(speed > previous_speed && speed < 40){
                if(state != State.SLOW){
                    UdpManager.sendMessage(UdpManager.BRAKE, UdpManager.ACC_BRK_RELEASE);
                    Log.i(LOG_TAG, "brake released");
                    state = State.SLOW;
                }
            }
        }
        else {
            //go to 0 automatically
            if (speed == 0){
                // put in park
                UdpManager.sendMessage(UdpManager.PRNDL, UdpManager.PARK);
                Log.i(LOG_TAG, "parked");
            }
        }
        previous_speed = speed;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // the car start to move slowly
        UdpManager.sendMessage(UdpManager.PRNDL, direction);
        previous_speed = seekBar.getProgress();
        state = State.SLOW;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() == 0) {
            // put in park
            UdpManager.sendMessage(UdpManager.PRNDL, UdpManager.PARK);
            Log.i(LOG_TAG, "parked");
        } else {
            // start to go down automatically
            UdpManager.sendMessage(UdpManager.ACCELERATOR, UdpManager.ACC_BRK_RELEASE);
            Log.i(LOG_TAG, "accelerator released");

            // Animate the seekbar
            ObjectAnimator animation = ObjectAnimator.ofInt(seekBar, "progress", 0);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }
}
