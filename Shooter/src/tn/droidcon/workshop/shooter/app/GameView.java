package tn.droidcon.workshop.shooter.app;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    /** game score points */
    static final int TARGET_HIT_POINT = 20;
    static final int TARGET_MISSED_POINT = -20;
    static final int AIR_BULLET_POINT = -10;

    /** The thread that actually draws the animation */
    private GameThread gameThread;

    private TextView mTimerView;
    private Button mButton;
    private TextView mHelpTextView;
    private RelativeLayout mControlsLayout;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        gameThread = new GameThread(holder, context, new GameHandler(this));

        setFocusable(true); // make sure we get key events
    }

    public void setTimerView(TextView tv) {
        mTimerView = tv;
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * @return the animation thread
     */
    public GameThread getThread() {
        return gameThread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus)
            gameThread.pause();
    }

    /** Callback invoked when the surface dimensions change. */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        gameThread.setSurfaceSize(width, height);
    }

    /**
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        gameThread.setRunning(true);
        gameThread.start();
    }

    /**
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public void SetButtonView(Button submiButton) {
        mButton = submiButton;
    }

    // we reuse the help screen into the end game screen.
    public void SetHelpTextView(TextView textView) {
        mHelpTextView = textView;
    }

    public void SetControlsLayout(RelativeLayout controlLayout) {
        mControlsLayout = controlLayout;
    }

    /***********************************************************/
    /***************** private classes *************************/
    /***********************************************************/

    private static class GameHandler extends Handler {

        private final WeakReference<GameView> mTarget;

        private GameHandler(GameView target) {
            mTarget = new WeakReference<GameView>(target);
        }

        @Override
        public void handleMessage(Message m) {
            GameView target = mTarget.get();

            target.mTimerView.setText(m.getData().getString(
                    target.gameThread.KEY_TIMER));

            if (m.getData().getString(target.gameThread.KEY_TIME_OUT) != null) {
                // all messages received
                int nbAirBullet = Integer.valueOf(m.getData().getString(
                        target.gameThread.KEY_AIR_BULLET));
                int nbTargetHit = Integer.valueOf(m.getData().getString(
                        target.gameThread.KEY_TARGET_HIT));
                int nbTargetMissed = Integer.valueOf(m.getData().getString(
                        target.gameThread.KEY_TARGET_MISSED));
                // calculate score
                int score = (nbAirBullet * AIR_BULLET_POINT)
                        + (nbTargetHit * TARGET_HIT_POINT)
                        + (nbTargetMissed * TARGET_MISSED_POINT);

                target.mButton.setText(R.string.replay);
                target.mButton.setVisibility(View.VISIBLE);
                target.mControlsLayout.setVisibility(View.GONE);
                target.mTimerView.setVisibility(View.INVISIBLE);
                target.mHelpTextView.setVisibility(View.VISIBLE);

                target.mHelpTextView.setText(target.getContext().getResources()
                        .getString(R.string.scoreTitle)
                        + "\n"
                        + target.getContext().getResources()
                                .getString(R.string.targetHitTitle)
                        + " "
                        + nbTargetHit
                        + " x "
                        + TARGET_HIT_POINT
                        + "\n"
                        + target.getContext().getResources()
                                .getString(R.string.targetMissedTitle)
                        + " "
                        + nbTargetMissed
                        + " x "
                        + TARGET_MISSED_POINT
                        + "\n"
                        + target.getContext().getResources()
                                .getString(R.string.airBulletTitle)
                        + " "
                        + nbAirBullet
                        + " x "
                        + AIR_BULLET_POINT
                        + "\n"
                        + target.getContext().getResources()
                                .getString(R.string.totalScoreTitle)
                        + " "
                        + score);
            }
        }
    }
}
