package com.mypopsy.doorsignview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.mypopsy.doorsignview.R;

/**
 * Created by Cerrato Renaud <https://www.github.com/renaudcerrato>
 */
public class DoorSignView extends View {

    private static final float SHADOW_DARK_RATIO = 0.7f;
    private static final float SHADOW_ALPHA = 0.8f;

    private TextPaint mTextPaint;
    private Paint mSignPaint;
    private Paint mSignShadowPaint;
    private Paint mStringsPaint;
    private Paint mPinPaint;
    private Paint mPinShadowPaint;

    private Layout mTextLayout;

    private CharSequence mText;
    private int mCornerRadius;
    private int mPinRadius;
    private int mShadowSize;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;
    private float mPinOffsetX, mPinOffsetY;
    private float mTextSpacingMult;
    private float mTextSpacingAdd;
    private float mShadowAngle = 90;

    private RectF mBodyBounds = new RectF();
    private Path mBodyPath;
    private float mShadowOffsetX, mShadowOffsetY;



    public DoorSignView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoorSignView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.DefaultDoorScreenViewStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DoorSignView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Options args = Options.from(context, attrs, defStyleAttr, defStyleRes);

        mTextPaint = new TextPaint();
        setTextSize(TypedValue.COMPLEX_UNIT_PX, args.textSize);
        setTextColor(args.textColor);
        setTypeface(args.typeFace);

        mSignPaint = new Paint();
        setSignColor(args.color);

        mSignShadowPaint = new Paint();
        mSignShadowPaint.setColor(darkerOf(args.color, SHADOW_DARK_RATIO));

        mPinPaint = new Paint();
        setPinColor(args.pinColor);

        mPinShadowPaint = new Paint();
        mPinShadowPaint.setColor(transparent(darkerOf(args.pinColor, SHADOW_DARK_RATIO), SHADOW_ALPHA));

        mStringsPaint = new Paint();
        setStringsColor(args.stringsColor);
        setStringsWidth(args.stringsSize);

        setText(args.text);
        setCornerRadius(args.cornerRadius);
        setPinRadius(args.pinRadius);
        setShadowSize(args.shadowSize);
        setTextPadding(args.textPaddingLeft, args.textPaddingTop, args.textPaddingRight, args.textPaddingBottom);
        setPinOffset(args.pinOffsetX, args.pinOffsetY);
        setTextSpacing(args.textSpacingMult, args.textSpacingAdd);
        setShadowAngle(mShadowAngle);
    }

    private void setPinOffset(float pinOffsetX, float pinOffsetY) {
        mPinOffsetX = pinOffsetX;
        mPinOffsetY = pinOffsetY;
        updatePivot();
        requestLayout();
    }

    public void setTextPadding(int left, int top, int right, int bottom) {
        mTextPaddingLeft = left;
        mTextPaddingTop = top;
        mTextPaddingRight = right;
        mTextPaddingBottom = bottom;
        requestLayout();
    }
    
    public void setShadowSize(int size) {
        this.mShadowSize = size;
        updateShadow();
        updatePivot();
        requestLayout();
    }

    public void setPinRadius(int radius) {
        this.mPinRadius = radius;
        updatePivot();
        requestLayout();
    }

    public void setCornerRadius(int radius) {
        this.mCornerRadius = radius;
        mBodyPath = null;
        invalidate();
    }

    public void setText(CharSequence charSequence) {
        mText = charSequence == null ? "" : charSequence;
        requestLayout();
    }

    public void setStringsWidth(int width) {
        mStringsPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setStringsColor(int color) {
        mStringsPaint.setColor(color);
        invalidate();
    }

    public void setPinColor(int color) {
        mPinPaint.setColor(color);
        invalidate();
    }

    public void setSignColor(int color) {
        mSignPaint.setColor(color);
        invalidate();
    }

    public void setTypeface(Typeface typeFace) {
        mTextPaint.setTypeface(typeFace);
        requestLayout();
    }

    public void setTextSize(int unit, float size) {
        mTextPaint.setTextSize(TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics()));
        requestLayout();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setShadowAngle(float degree) {
        mShadowAngle = degree;
        updateShadow();
    }

    public void setTextSpacing(float mult, float add) {
        this.mTextSpacingMult = mult;
        this.mTextSpacingAdd = add;
        requestLayout();
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        updateShadow();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;

        mTextLayout = new StaticLayout(mText, mTextPaint,
                width - mTextPaddingLeft - mTextPaddingRight - 2 * mShadowSize,
                Layout.Alignment.ALIGN_CENTER, mTextSpacingMult, mTextSpacingAdd, true);

        mBodyBounds.right = getWidth() - 2 * mShadowSize;
        mBodyBounds.bottom = mTextLayout.getHeight() + mTextPaddingTop + mTextPaddingBottom;

        if(heightMode == MeasureSpec.EXACTLY)
            height = heightSize;
        else {
            height = Math.round(mBodyBounds.height() + mBodyBounds.width()*mPinOffsetY + 2*mShadowSize);
            height = Math.min(height, heightSize);
        }

        mBodyPath = null;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBodyPath = null;
        updatePivot();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBodyPath == null) {
            mBodyPath = new Path();
            mBodyPath.setFillType(Path.FillType.EVEN_ODD);
            mBodyPath.addRoundRect(mBodyBounds, mCornerRadius, mCornerRadius, Path.Direction.CW);
            mBodyPath.addCircle(2 * mCornerRadius, 2 * mCornerRadius, mCornerRadius, Path.Direction.CW);
            mBodyPath.addCircle(mBodyBounds.width() - 2 * mCornerRadius, 2 * mCornerRadius, mCornerRadius, Path.Direction.CW);
        }

        canvas.save();
        canvas.translate(mShadowSize, mShadowSize);
        canvas.translate(0, mBodyBounds.width()*mPinOffsetY);

        // Draw strings
        final float pinX = mPinOffsetX*mBodyBounds.width();
        final float pinY = mPinRadius - mPinOffsetY*mBodyBounds.width();
        canvas.drawLine(mCornerRadius, mCornerRadius, pinX, pinY - 2*mStringsPaint.getStrokeWidth(), mStringsPaint);
        canvas.drawLine(mBodyBounds.width() - mCornerRadius, mCornerRadius, pinX, pinY - 2*mStringsPaint.getStrokeWidth(), mStringsPaint);

        // Draw shadow
        if(mShadowOffsetX != 0 || mShadowOffsetY != 0) {
            canvas.translate(mShadowOffsetX, mShadowOffsetY);
            canvas.drawPath(mBodyPath, mSignShadowPaint);
            canvas.translate(-mShadowOffsetX, -mShadowOffsetY);
        }

        // Draw pin
        canvas.drawCircle(pinX + mShadowOffsetX, pinY + mShadowOffsetY, mPinRadius, mPinShadowPaint);
        canvas.drawCircle(pinX, pinY, mPinRadius, mPinPaint);

        // Draw body
        canvas.drawPath(mBodyPath, mSignPaint);

        // Draw text
        canvas.translate(mTextPaddingLeft, mTextPaddingTop + (mBodyBounds.height() - mTextLayout.getHeight() - mTextPaddingTop - mTextPaddingBottom)/2);
        mTextLayout.draw(canvas);

        canvas.restore();
    }

    private void updatePivot() {
        ViewCompat.setPivotX(this, mShadowSize + mPinOffsetX*mBodyBounds.width());
        ViewCompat.setPivotY(this, mShadowSize + mPinRadius);
    }

    private void updateShadow() {
        final float offsetX = (float) (mShadowSize * Math.cos(Math.toRadians(mShadowAngle - ViewCompat.getRotation(this))));
        final float offsetY = (float) (mShadowSize * Math.sin(Math.toRadians(mShadowAngle - ViewCompat.getRotation(this))));
        if(offsetX != mShadowOffsetX || offsetY != mShadowOffsetY) {
            mShadowOffsetX = offsetX;
            mShadowOffsetY = offsetY;
            invalidate();
        }
    }

    static private int transparent(int color, float alpha) {
        return Color.argb(Math.round(alpha*255), Color.red(color), Color.green(color), Color.blue(color));
    }

    static private int darkerOf(int color, float ratio) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[2]*=ratio;
        return ColorUtils.HSLToColor(hsl);
    }

    private static class Options {

        CharSequence text;
        int cornerRadius;
        int shadowSize;
        int textSize;
        Typeface typeFace;
        int textColor;
        int color;
        int pinRadius;
        int pinColor;
        int stringsSize;
        int stringsColor;
        int textPaddingLeft;
        int textPaddingTop;
        int textPaddingRight;
        int textPaddingBottom;
        float pinOffsetX = 0.5f;
        float pinOffsetY = 0.2f;
        float textSpacingMult;
        float textSpacingAdd;

        static Options from(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DoorSignView, defStyleAttr, defStyleRes);
            final Options options = new Options();

            options.text = a.getText(R.styleable.DoorSignView_android_text);
            options.cornerRadius = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_cornerRadius, 0);
            options.shadowSize = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_shadowSize, 0);
            options.textSize = a.getDimensionPixelSize(R.styleable.DoorSignView_android_textSize, 20);
            options.textColor = a.getColor(R.styleable.DoorSignView_android_textColor, Color.BLACK);
            options.color = a.getColor(R.styleable.DoorSignView_dsv_signColor, Color.DKGRAY);
            options.pinRadius = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_pinRadius, 20);
            options.pinColor = a.getColor(R.styleable.DoorSignView_dsv_pinColor, Color.WHITE);
            options.stringsSize = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_stringsWidth, 10);
            options.stringsColor= a.getColor(R.styleable.DoorSignView_dsv_stringsColor, Color.LTGRAY);
            options.textPaddingLeft = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_textPaddingLeft, 0);
            options.textPaddingTop = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_textPaddingTop, 0);
            options.textPaddingRight = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_textPaddingRight, 0);
            options.textPaddingBottom = a.getDimensionPixelSize(R.styleable.DoorSignView_dsv_textPaddingBottom, 0);
            options.pinOffsetX = a.getFloat(R.styleable.DoorSignView_dsv_pinOffsetX, 0.5f);
            options.pinOffsetY = a.getFloat(R.styleable.DoorSignView_dsv_pinOffsetY, 0.25f);
            options.textSpacingAdd = a.getDimension(R.styleable.DoorSignView_dsv_textSpacingAdd, 0);
            options.textSpacingMult = a.getFloat(R.styleable.DoorSignView_dsv_textSpacingMult, 1f);

            final String textFont = a.getString(R.styleable.DoorSignView_dsv_textFont);
            if(textFont != null)
                options.typeFace = Typeface.createFromAsset(context.getAssets(), textFont);
            else switch(a.getInt(R.styleable.DoorSignView_android_typeface, 0)) {
                case 0:
                    options.typeFace = Typeface.DEFAULT;
                    break;
                case 1:
                    options.typeFace = Typeface.SANS_SERIF;
                    break;
                case 2:
                    options.typeFace = Typeface.SERIF;
                    break;
                case 3:
                    options.typeFace = Typeface.MONOSPACE;
                    break;
            }

            a.recycle();

            return options;
        }
    }
}
