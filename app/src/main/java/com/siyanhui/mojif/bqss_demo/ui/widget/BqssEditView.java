package com.siyanhui.mojif.bqss_demo.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.siyanhui.mojif.bqss_demo.R;


/**
 * Created by fantasy on 16/12/30.
 */

public class BqssEditView extends EditText {
    private String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private Drawable mDrawable;
    private float searchSize = 0;
    private float textSize = 0;
    private Paint paint;
    private int bgDrawableResId;

    public BqssEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResource(context, attrs);
        InitPaint();
        initUI();
    }

    private void InitPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.gray_97));
        paint.setTextSize(textSize);
    }

    private void initResource(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.bqss_editview);
        float density = context.getResources().getDisplayMetrics().density;
        searchSize = typedArray.getDimension(R.styleable.bqss_editview_imageWidth, 18 * density + 0.5f);
        textSize = typedArray.getDimension(R.styleable.bqss_editview_textSize, 14 * density + 0.5f);
        bgDrawableResId = attributeSet.getAttributeResourceValue(NAMESPACE, "bgResources", -1);
        typedArray.recycle();
    }

    private void initUI() {
        this.setBackgroundResource(bgDrawableResId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSearchIcon(canvas);
    }

    private void drawSearchIcon(Canvas canvas) {
        float textHeight = getFontLeading(paint);
        float dx = getWidth() / 12;
        float dy = (getHeight() - searchSize) / 2;
        canvas.save();
        canvas.translate(getScrollX() + dx - 40, getScrollY() + dy);
        if (this.getText().toString().length() == 0) {
            if (mDrawable != null) {
                mDrawable.draw(canvas);
            }
            canvas.drawText(getResources().getText(R.string.search_interseting_sticker).toString(), getScrollX() + searchSize + 10, (getHeight() - (getHeight() - textHeight) / 2) - paint.getFontMetrics().bottom - dy, paint);
            canvas.restore();
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDrawable == null) {
            mDrawable = getResources().getDrawable(R.mipmap.search_icon);
            mDrawable.setBounds(0, 0, (int) searchSize, (int) searchSize);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            mDrawable = null;
        }
        super.onDetachedFromWindow();
    }

    public float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.bottom - fm.top;
    }
}
