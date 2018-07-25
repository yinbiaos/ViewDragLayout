package com.boyin.weiget;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * 可拖拽子控件的布局
 *
 * @author yinbiao
 * @date 2018/7/10
 */
public class ViewDragLayout extends FrameLayout {

    private static final String TAG = "ViewDragLayout";

    private ViewDragHelper mDragHelper;
    /**
     * map用于缓存子View移动后的偏移量
     */
    private Map<View, Point> map = new HashMap<>(1);

    public ViewDragLayout(@NonNull Context context) {
        this(context, null);
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                /*控制左边的拖曳距离，不能越界。
                当拖曳的距离超过左边的padding值，也意味着child view越界，复位
                默认的padding值=0*/
                int paddingleft = getPaddingLeft();
                if (left < paddingleft) {
                    return paddingleft;
                }

               /* 这里是控制右边的拖曳边缘极限位置。
                假设pos的值刚好是子view child右边边缘与父view的右边重合的情况
                pos值即为一个极限的最右边位置，超过也即意味着拖曳越界：越出右边的界限，复位。
                可以再加一个paddingRight值，缺省的paddingRight=0，所以即便不加也在多数情况正常可以工作*/
                int pos = getWidth() - child.getWidth() - getPaddingRight();
                if (left > pos) {
                    return pos;
                }
                /*其他情况属于在范围内的拖曳，直接返回系统计算默认的left即可*/
                return left;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                int paddingTop = getPaddingTop();
                if (top < paddingTop) {
                    return paddingTop;
                }
                int pos = getHeight() - child.getHeight() - getPaddingBottom();
                if (top > pos) {
                    return pos;
                }
                return top;
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                /*缓存子View移动偏移量*/
                Point p;
                if (map.containsKey(changedView)) {
                    p = map.get(changedView);
                } else {
                    p = new Point(0, 0);
                }
                p.x += dx;
                p.y += dy;
                map.put(changedView, p);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                postInvalidate();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*每次布局重新定位时，子View会被初始化复位，使用缓存的偏移量，重新定位子View*/
        if (!map.isEmpty()) {
            for (Map.Entry<View, Point> entry : map.entrySet()) {
                Point p = entry.getValue();
                View v = entry.getKey();
                v.offsetLeftAndRight(p.x);
                v.offsetTopAndBottom(p.y);
            }
        }
    }
}
