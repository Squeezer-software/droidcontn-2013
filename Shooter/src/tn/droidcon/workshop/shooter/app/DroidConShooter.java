package tn.droidcon.workshop.shooter.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DroidConShooter extends Activity implements View.OnClickListener,
        OnTouchListener {

    /** A handle to the thread that's actually running the animation. */
    private GameThread mGameThread;

    /** A handle to the View in which the game is running. */
    private GameView mGameView;

    // Button to start game and submit score in the end
    private Button mStartButton;

    // the window for instructions and such
    private TextView mHelpTextView;

    // game window timer
    private TextView mTimerView;

    private RelativeLayout mControlLayout;
    private Button mControlButtonLeft;
    private Button mControlButtonRight;
    private Button mControlButtonFire;

    /**
     * Invoked when the Activity is created.
     * @param savedInstanceState
     *            a Bundle containing state saved from a previous execution, or
     *            null if this is a new execution
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // tell system to use the layout defined in our XML file
        setContentView(R.layout.main);

        // get handles to the LunarView from XML, and its LunarThread
        mGameView = (GameView) findViewById(R.id.game);
        mGameThread = mGameView.getThread();

        // look up the happy shiny button
        mStartButton = (Button) findViewById(R.id.startButton);
        mStartButton.setOnClickListener(this);

        // set up handles for instruction text and game timer text
        mHelpTextView = (TextView) findViewById(R.id.helpText);
        mHelpTextView.setText(R.string.helpText);
        mTimerView = (TextView) findViewById(R.id.timer);
        mControlLayout = (RelativeLayout) findViewById(R.id.control_layout);
        mControlButtonLeft = (Button) findViewById(R.id.left_direction);
        mControlButtonRight = (Button) findViewById(R.id.right_direction);
        mControlButtonFire = (Button) findViewById(R.id.fire_button);
        mControlButtonLeft.setOnTouchListener(this);
        mControlButtonRight.setOnTouchListener(this);
        mControlButtonFire.setOnTouchListener(this);

        // set up a new game
        mGameThread.setState(GameThread.STATE_READY);

        mTimerView = (TextView) findViewById(R.id.timer);
        mGameView.setTimerView(mTimerView);
        mTimerView.setVisibility(View.INVISIBLE);

        mGameView.SetButtonView(mStartButton);
        mGameView.SetHelpTextView(mHelpTextView);
        mGameView.SetControlsLayout(mControlLayout);
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGameThread.pause(); // pause game when Activity pauses
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.startButton) {
            if (mGameThread.getGameState() == GameThread.STATE_READY) {
                mStartButton.setVisibility(View.INVISIBLE);
                mHelpTextView.setVisibility(View.INVISIBLE);
                mTimerView.setVisibility(View.VISIBLE);
                mControlLayout.setVisibility(View.VISIBLE);
                mGameThread.doStart();
            } else if (mGameThread.getGameState() == GameThread.STATE_GAME_OVER) {
                mGameThread.setState(GameThread.STATE_READY);
                mStartButton.setVisibility(View.INVISIBLE);
                mHelpTextView.setVisibility(View.INVISIBLE);
                mTimerView.setVisibility(View.VISIBLE);
                mControlLayout.setVisibility(View.VISIBLE);
                mGameThread.doStart();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGameThread.getGameState() == GameThread.STATE_RUNNING) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mGameThread.doControlDown(v.getId());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mGameThread.doControlUp(v.getId());
            }
        }

        return false;
    }
}
