package com.lzq.flowtaglayoutview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * function : 标签流布局
 * Created by lzq on 2017/7/18.
 */

public class FlowTagLayout extends ViewGroup{

    private int horizontalSpacing = dip2px(10); //水平方向上标签之间的间隔
    private int verticalSpacing = dip2px(10); //行距
    private Line mCurrLine;
    private List<Line> mLines = new ArrayList<>();
    private int mLineSize;


    public FlowTagLayout(Context context) {
        this(context, null);
    }

    public FlowTagLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowTagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowTagLayout, defStyleAttr, 0);
        horizontalSpacing = ta.getDimensionPixelSize(R.styleable.FlowTagLayout_horizontalSpacing, horizontalSpacing);
        verticalSpacing = ta.getDimensionPixelSize(R.styleable.FlowTagLayout_verticalSpacing, verticalSpacing);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //实际可用高度
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);

        //初始化
        mLines.clear();
        mLineSize = 0;
        mCurrLine = new Line();

        for (int i = 0; i < getChildCount(); i++){
            //测量子View的高度
            View childView = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthModel == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthModel);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightModel == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightModel);
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            mLineSize += childView.getMeasuredWidth();
            if (mLineSize < width){
                mCurrLine.addView(childView);
                mLineSize += horizontalSpacing;
            }else{ //换行
                if (mCurrLine != null){
                    mLines.add(mCurrLine);
                }
                mCurrLine = new Line();
                mLineSize = 0;
                mCurrLine .addView(childView);

                mLineSize += childView.getMeasuredWidth() + horizontalSpacing;
            }
        }

        //加上最后一行
        if (mCurrLine != null && !mLines.contains(mCurrLine)){
            mLines.add(mCurrLine);
        }

        int totalHeight = 0;
        for (int i = 0; i < mLines.size(); i++){
            totalHeight += mLines.get(i).getLineHeight();
        }

        totalHeight += verticalSpacing * (mLines.size() - 1);
        totalHeight += getPaddingBottom() + getPaddingTop();
        //重测高度
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        left = getPaddingLeft();
        top = getPaddingTop();
        for (int i = 0; i < mLines.size(); i++){
            Line line = mLines.get(i);
            line.layout(left, top);
            top += line.getLineHeight() + verticalSpacing;
        }
    }

    /**
     * 行管理器, 管理每一行的孩子
     */
    private class Line{
        private List<View> lineViews = new ArrayList<>();
        int maxHeight;

        private void addView(View view){
            lineViews.add(view);
            if (maxHeight < view.getMeasuredHeight()){
                maxHeight = view.getMeasuredHeight();
            }
        }

        /**
         * 指定绘制子View的位置
         * @param left 左上角x轴坐标
         * @param top 左上角y轴坐标
         */
        private void layout(int left, int top){
            int currLeft = left;
            for (View view : lineViews){
                view.layout(currLeft, top, currLeft + view.getMeasuredWidth(), top + view.getMeasuredHeight());
                currLeft += (view.getMeasuredWidth() + horizontalSpacing);
            }
        }

        private int getLineHeight(){
            return maxHeight;
        }
    }


    public void setFlowTagLayout(List<String> dataList, final OnItemClickListener onItemClickListener){
        for (final String tag : dataList){
            TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_item_tag, null);
            tv.setText(tag);
            this.addView(tv, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null){
                        onItemClickListener.onItemClick(tag);
                    }
                }
            });
        }
    }

    //对外开放接口
    public interface OnItemClickListener{
        void onItemClick(String tag);
    }

    private int dip2px(float dip){
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int)(scale * dip + 0.5);
    }
}
