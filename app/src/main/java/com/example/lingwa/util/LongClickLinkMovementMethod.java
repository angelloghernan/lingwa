package com.example.lingwa.util;

import android.os.Handler;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

// See: https://stackoverflow.com/questions/26813104/long-click-on-clickable-span-not-firing-until-click-is-released
// I attempted to use Facebook's implementation in Litho, but it did not work without a custom MovementMethod.
public class LongClickLinkMovementMethod extends LinkMovementMethod {

    private Handler mLongClickHandler;
    private static int LONG_CLICK_TIME = 750;
    private boolean mIsLongPressed = false;
    public int clickX = 0;
    public int clickY = 0;

    @Override
    public boolean onTouchEvent(final TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if(action == MotionEvent.ACTION_CANCEL){
            if(mLongClickHandler!=null){
                mLongClickHandler.removeCallbacksAndMessages(null);
            }
        }

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            clickX = x;
            clickY = y;

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            final LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if(mLongClickHandler!=null){
                        mLongClickHandler.removeCallbacksAndMessages(null);
                    }
                    if(!mIsLongPressed) {
                        link[0].onClick(widget);
                    }
                    mIsLongPressed = false;
                } else {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                    mLongClickHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            link[0].onLongClick(widget);
                            mIsLongPressed = true;
                        }
                    },LONG_CLICK_TIME);
                }
                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }


    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new LongClickLinkMovementMethod();
            sInstance.mLongClickHandler = new Handler();
        }

        return sInstance;
    }
    private static LongClickLinkMovementMethod sInstance;
}