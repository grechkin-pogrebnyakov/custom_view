package com.example.serg.castomview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;


/**
 * Created by S.Grechkin-Pogrebnyakov on 23.04.15.
 */
public class ParentViewGroup extends ViewGroup {

    // The gesture threshold expressed in dp
    private static final float GESTURE_THRESHOLD_DP = 16.0f;

    // Get the screen's density scale
    final float scale = getResources().getDisplayMetrics().density;
// Convert the dps to pixels, based on density scale
    final float mGestureThreshold = (int) (GESTURE_THRESHOLD_DP * scale + 0.5f);
    private final Scroller s;

    private float x, started_x;
    private int width, offset;
    private boolean move_started;

    public ParentViewGroup(Context context) {
        this(context, null, 0);
    }

    public ParentViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        s = new Scroller(getContext());
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a from a set of
     * XML attributes.
     *
     * @param context   Context, mazafaka
     * @param attrs   An attribute set which can contain attributes from
     *                from {@link android.view.View}.
     */
    public ParentViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        offset = -width;
        int count = getChildCount();
        for (int i = 0; i < count; i++){
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec( width, MeasureSpec.EXACTLY ), heightMeasureSpec );
        }
        setMeasuredDimension(count * width, MeasureSpec.getSize( heightMeasureSpec ));
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        offsetLeftAndRight(offset);
        View child = getChildAt(0);
        child.layout(l, t, r - width, b);
        child = getChildAt(1);
        child.layout(l + width, t, r, b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if( ev.getAction() == MotionEvent.ACTION_DOWN ) {
            x = ev.getAxisValue(MotionEvent.AXIS_X) + offset;
            started_x = x;
            s.abortAnimation();
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void move(float new_x) {
        int new_offset = offset + (int)(new_x - x);
        x = new_x;
        if ( !move_started )
            if ( Math.abs(new_x - started_x) < mGestureThreshold )
                return;
            else
                move_started = true;

        if ( new_offset > 0 ) new_offset = 0;
        else if ( new_offset < -width ) new_offset = -width;

        offsetLeftAndRight(new_offset - offset);
        offset = new_offset;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        if( action == MotionEvent.ACTION_MOVE ) {
            float new_x = event.getAxisValue(MotionEvent.AXIS_X) + offset;
            move(new_x);
        } else if ( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ) {
            float new_x = event.getAxisValue(MotionEvent.AXIS_X) + offset;
            if ( offset < -width/2 ) {
                s.startScroll((int) new_x, 0, -offset - width, 0, 500);
                animationHandler.sendEmptyMessage(scrollMsg);
               // offsetLeftAndRight( -width - offset );
                //offset = -width;
            } else {
                s.startScroll((int) new_x, 0, -offset, 0, 500);
                animationHandler.sendEmptyMessage(scrollMsg);
                //offsetLeftAndRight( -offset );
                //offset = 0;
            }
        }
        return true;
    }
    int scrollMsg = 42;


    Handler animationHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if( msg.what == scrollMsg )
                if (s.computeScrollOffset()) {
                    float new_x = s.getCurrX();
                    move(new_x);
                    sendEmptyMessage(scrollMsg);
                } else {
                    move_started = false;
            }
        }


    };
}
