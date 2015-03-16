package tn.droidcon.workshop.shooter.app;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    /**
     * State-tracking constants
     */
    public static final int STATE_GAME_OVER = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;

    /**
     * keys used for handling messages between game thread and game view
     */
    public final String KEY_TIMER = "TIMER";
    public final String KEY_TIME_OUT = "TIME_OUT";
    public final String KEY_TARGET_HIT = "TAREGT_HIT";
    public final String KEY_TARGET_MISSED = "TAREGT_MISSED";
    public final String KEY_AIR_BULLET = "AIR_BULLET";

    /**
     * game components parameters
     */
    private static final int ANDROID_SPEED_VALUE = 100;
    private static final int BULLET_SPEED_VALUE = 300;
    private static final int TARGET_TIMER_VALUE = 3;

    /**
     * Handle to the application context, used to fetch Drawables for example.
     */
    private Context mContext;

    /**
     * Android bitmap position
     */
    private float x;
    private float y;

    /**
     * target position
     */
    private int target_x;
    private int target_y;

    /**
     * the droid direction
     */
    private boolean dRight;
    private boolean dLeft;

    /** Message handler used by game thread to interact with game View */
    private Handler mHandler;

    private int mCanvasWidth;
    private int mCanvasHeight;

    private long mLastTime;
    private Bitmap mPlayerBitmap;
    private Bitmap mBulletBitmap;
    private Bitmap mTargetBitmap;

    private boolean mTargetVisibility = true;
    private int mTargetTimer = TARGET_TIMER_VALUE;
    private int mTimerLimit = 120; // Value in seconds, = 2 min
    // updates the screen clock. Also used for tempo timing.
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    // one second - used to update timer
    private int mTaskIntervalInMillis = 1000;
    // string value for timer display
    private String mTimerValue = "2:00";

    // variables to calculate score
    private int scoreNbTargetHit = 0;
    private int scoreNbTargetMissed = 0;
    private int scoreNbAirBullet = 0;

    // stores all the bullets in order
    private ArrayList<Bullet> bulletList;

    /** Message handler used by thread to post stuff back to the GameView */
    // private Handler mHandler;
    /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
    private int mState;
    /** Indicate whether the surface has been created & is ready to draw */
    private boolean mRun = false;
    /** Handle to the surface manager object we interact with */
    private SurfaceHolder mSurfaceHolder;

    public GameThread(SurfaceHolder surfaceHolder, Context context,
            Handler handler) {
        // get handles to some important objects
        mSurfaceHolder = surfaceHolder;
        mHandler = handler;
        mContext = context;

        mPlayerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.androidman);

        mBulletBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.bullet);

        mTargetBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.target);

        // put the android in the middle of the screen
        x = 10;
        y = 10;

        target_x = 400;
        target_y = 25;

        bulletList = new ArrayList<Bullet>();
        mTimer = new Timer();
    }

    /**
     * Starts the game, setting parameters for the current difficulty.
     */
    public void doStart() {
        synchronized (mSurfaceHolder) {
            // Initialize game here!
            // put the android in the middle of the screen
            x = (mCanvasWidth / 2) - (mPlayerBitmap.getWidth() / 2);
            y = mCanvasHeight - mPlayerBitmap.getHeight();

            // place the target randomly
            target_x = (new Random()).nextInt(mCanvasWidth
                    - mTargetBitmap.getWidth());

            mLastTime = System.currentTimeMillis();
            setState(STATE_RUNNING);
            mTimerLimit = 120;
        }
    }

    /**
     * Pauses the physics update & animation.
     */
    public void pause() {
        synchronized (mSurfaceHolder) {
            if (mState == STATE_RUNNING) {
                setState(STATE_PAUSE);
            }
            // pause the timer also
            if (mTimerTask != null) {
                mTimerTask.cancel();
            }
        }
    }

    @Override
    public void run() {
        while (mRun) {
            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    if (mState == STATE_RUNNING) {
                        // kick off the timer task for counter update if not
                        // already
                        // initialized
                        if (mTimerTask == null) {
                            mTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    doCountDown();
                                }
                            };
                            mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
                        }// end of TimerTask init block
                        updateGame();
                    }
                    doDraw(c);
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    /**
     * manage the game count down timer
     */
    private void doCountDown() {

        if (mTimerLimit > 0)
            mTimerLimit--;

        // target timer is used with game timer
        if (mTargetTimer > 0) {
            mTargetVisibility = true;
            mTargetTimer--;
        } else {// target missed
            // hide target
            mTargetVisibility = false;
            // update score parameter
            scoreNbTargetMissed++;
            // update target position
            target_x = (new Random()).nextInt(mCanvasWidth
                    - mTargetBitmap.getWidth());
            // Reinitialize target timer
            mTargetTimer = TARGET_TIMER_VALUE;
        }

        try {
            // subtract one minute and see what the result is.
            int moreThanMinute = mTimerLimit - 60;

            if (moreThanMinute >= 0) {

                if (moreThanMinute > 9) {
                    mTimerValue = "1:" + moreThanMinute;
                }
                // need an extra '0' for formatting
                else {
                    mTimerValue = "1:0" + moreThanMinute;
                }
            } else {
                if (mTimerLimit > 9) {
                    mTimerValue = "0:" + mTimerLimit;
                } else {
                    mTimerValue = "0:0" + mTimerLimit;
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        Message msg = mHandler.obtainMessage();

        Bundle b = new Bundle();
        b.putString(KEY_TIMER, mTimerValue);

        // time's up
        if (mTimerLimit == 0) {
            b.putString(KEY_TIME_OUT, "" + STATE_GAME_OVER);
            b.putString(KEY_TARGET_HIT, "" + scoreNbTargetHit);
            b.putString(KEY_TARGET_MISSED, "" + scoreNbTargetMissed);
            b.putString(KEY_AIR_BULLET, "" + scoreNbAirBullet);
            mTimerTask = null;
            mState = STATE_GAME_OVER;
        } else {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    doCountDown();
                }
            };
            mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
        }
        // Send data back up to the main GameView thread.
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    /**
     * Used to signal the thread whether it should be running or not. Passing
     * true allows the thread to run; passing false will shut it down if it's
     * already running. Calling start() after this was most recently called with
     * false will result in an immediate shutdown.
     * @param b
     *            true to run, false to shut down
     */
    public void setRunning(boolean b) {
        mRun = b;
    }

    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * @see #setState(int, CharSequence)
     * @param mode
     *            one of the STATE_* constants
     */
    public void setState(int mode) {
        synchronized (mSurfaceHolder) {
            setState(mode, null);
        }
    }

    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * @param mode
     *            one of the STATE_* constants
     * @param message
     *            string to add to screen or null
     */
    public void setState(int mode, CharSequence message) {
        synchronized (mSurfaceHolder) {
            mState = mode;
        }
    }

    public int getGameState() {
        synchronized (mSurfaceHolder) {
            return mState;
        }
    }

    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;
        }
    }

    /**
     * Resumes from a pause.
     */
    public void unpause() {
        // Move the real time clock up to now
        synchronized (mSurfaceHolder) {
            mLastTime = System.currentTimeMillis();
        }
        setState(STATE_RUNNING);
        // pause the timer also
        if (mTimerTask != null) {
            mTimerTask.run();
        }
    }

    /**
     * Handles a control button down event.
     * @param controlID
     * @return
     */
    boolean doControlDown(int controlID) {
        boolean handled = false;
        synchronized (mSurfaceHolder) {
            if (controlID == R.id.right_direction) {
                dRight = true;
                handled = true;
            }
            if (controlID == R.id.left_direction) {
                dLeft = true;
                handled = true;
            }

            return handled;
        }
    }

    /**
     * Handles a key-up event.
     * @param controlID
     *            the key that was pressed
     * @param msg
     *            the original event object
     * @return true if the key was handled and consumed, or else false
     */
    boolean doControlUp(int controlID) {
        boolean handled = false;
        synchronized (mSurfaceHolder) {
            if (controlID == R.id.right_direction) {
                dRight = false;
                handled = true;
            }
            if (controlID == R.id.left_direction) {
                dLeft = false;
                handled = true;
            }
            if (controlID == R.id.fire_button) {
                bulletCreate();
            }

            return handled;
        }
    }

    /**
     * Draws the game components to the provided Canvas.
     */
    private void doDraw(Canvas canvas) {
        if (canvas != null) {
            // empty canvas
            canvas.drawARGB(255, 0, 0, 0);
            // only draw game components during running state
            if (mState == STATE_RUNNING) {
                canvas.drawBitmap(mPlayerBitmap, x, y, null);

                if (mTargetVisibility) {
                    canvas.drawBitmap(mTargetBitmap, target_x, target_y, null);
                }

                if ((bulletList != null) || (bulletList.size() > 0)) {
                    for (int i = 0; i < bulletList.size(); i++) {
                        Bullet bullet = bulletList.get(i);

                        canvas.drawBitmap(mBulletBitmap, bullet.mDrawX,
                                bullet.mDrawY, null);
                    }
                }
            }
        }
    }

    /**
     * Updates the game.
     */
    private void updateGame() {

        long now = System.currentTimeMillis();
        // Do nothing if mLastTime is in the future.
        // This allows the game-start to delay the start of the physics
        // by 100ms or whatever.
        if (mLastTime > now)
            return;
        double elapsed = (now - mLastTime) / 1000.0;
        mLastTime = now;
        // // </DoNotRemove>

        /*
         * Why use mLastTime, now and elapsed? Well, because the frame rate
         * isn't always constant, it could happen your normal frame rate is
         * 25fps then your android will walk at a steady pace, but when your
         * frame
         * rate drops to say 12fps, without elapsed your character will only
         * walk half as fast as at the 25fps frame rate. Elapsed lets you manage
         * the slowdowns and speedups!
         */

        /*
         * if (dUp) y -= elapsed * SPEED; if (dDown) y += elapsed * SPEED; if (y
         * < 0) y = 0; else if (y >= mCanvasHeight - mSnowflake.getHeight()) y =
         * mCanvasHeight - mSnowflake.getHeight();
         */
        if (dLeft)
            x -= elapsed * ANDROID_SPEED_VALUE;
        if (dRight)
            x += elapsed * ANDROID_SPEED_VALUE;

        if ((bulletList != null) || (bulletList.size() > 0)) {
            for (int i = 0; i < bulletList.size(); i++) {
                Bullet bullet = bulletList.get(i);
                bullet.mDrawY -= elapsed * BULLET_SPEED_VALUE;
                if (isBulletHitsTarget(bullet)) {
                    bulletList.remove(i);
                    // update score parameter
                    scoreNbTargetHit++;
                    // hide target
                    mTargetVisibility = false;
                    // update target position
                    target_x = (new Random()).nextInt(mCanvasWidth
                            - mTargetBitmap.getWidth());
                    // Reinitialize target timer
                    mTargetTimer = TARGET_TIMER_VALUE;
                }
                if (bullet.mDrawY < 0) {
                    bulletList.remove(i);
                    // update score parameter
                    scoreNbAirBullet++;
                }
            }

        }

        if (x < 0)
            x = 0;
        else if (x >= mCanvasWidth - mPlayerBitmap.getWidth())
            x = mCanvasWidth - mPlayerBitmap.getWidth();
    }

    private void bulletCreate() {
        Bullet bullet = new Bullet();
        bullet.mStartTime = System.currentTimeMillis();
        bullet.mDrawX = (x + mPlayerBitmap.getWidth()) - 10;
        bullet.mDrawY = y - 20;
        bulletList.add(bullet);
    }

    private boolean isBulletHitsTarget(Bullet bullet) {
        if (mTargetVisibility
                && ((bullet.mDrawX + mBulletBitmap.getWidth()) > target_x)
                && (bullet.mDrawX < (target_x + mTargetBitmap.getWidth()))
                && (bullet.mDrawY < (target_y + (mTargetBitmap.getHeight() / 2) + 6))) {
            return true;
        } else {
            return false;
        }
    }
}
