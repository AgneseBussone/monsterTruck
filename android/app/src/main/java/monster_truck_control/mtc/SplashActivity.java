package monster_truck_control.mtc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Splash screen shown the minimum amount of time needed to start the app.
 * No fixed time requested, I don't want to waste user's time.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No need to setup a view, because it comes from the theme

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
