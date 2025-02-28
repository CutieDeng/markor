package net.gsantner.markor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

@SuppressLint("ClickableViewAccessibility")
public class DraggableScrollbarScrollView extends ScrollView {

    private boolean _isFastScrolling = false;
    private boolean _fastScrollEnabled = true;
    private boolean _ltr = true;
    private int _thumbHeight;
    private int _grabWidth;

    public DraggableScrollbarScrollView(Context context) {
        super(context);
    }

    public DraggableScrollbarScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableScrollbarScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        // Issue #1025 Related:
        // When init the scroll, just set it as the bottom in here.
        // It may give a better usage for the user.
        //
        // todo: Next step, describe a micro database to store this special info, and fetch it here.
        smoothScrollTo(0, (int )_thumbHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!_fastScrollEnabled) {
            return super.onInterceptTouchEvent(ev);
        }
        if (_isFastScrolling) {
            return true;
        }
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN &&
                ((_ltr && getWidth() - _grabWidth < ev.getX())
                        || (!_ltr && _grabWidth > ev.getX()))) {
            computeThumbHeight();
            awakenScrollBars();
            float scrollbarStartPos = (float) computeVerticalScrollOffset() / computeVerticalScrollRange() * (getHeight());
            if (Math.abs(ev.getY() - scrollbarStartPos) < _thumbHeight) {
                _isFastScrolling = true;
            }
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            _isFastScrolling = false;
        }
        if (_isFastScrolling && action == MotionEvent.ACTION_MOVE) {
            smoothScrollTo(0, (int) (((ev.getY() - _thumbHeight / 2) / (getHeight() - _thumbHeight)) * (computeVerticalScrollRange() - computeVerticalScrollExtent())));
            return true;
        }
        return super.onTouchEvent(ev);
    }

    // Approximate height of thumb
    private void computeThumbHeight() {
        int height = (int) ((float) computeVerticalScrollExtent() * getHeight() / computeVerticalScrollRange());
        int minHeight = getHeight() / 8;
        if (height < minHeight) {
            height = minHeight;
        }
        _thumbHeight = height;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSmoothScrollingEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _ltr = getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
        }
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        _grabWidth = (int) (1.5 * (float) getVerticalScrollbarWidth() * displayMetrics.density);
    }

    public void setFastScrollEnabled(boolean fastScrollEnabled) {
        _fastScrollEnabled = fastScrollEnabled;
    }

    public boolean isFastScrollEnabled() {
        return _fastScrollEnabled;
    }
}
