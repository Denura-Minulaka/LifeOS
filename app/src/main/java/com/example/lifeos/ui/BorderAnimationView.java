package com.example.lifeos.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BorderAnimationView extends View {
    private Paint paint;
    private Path path;
    private PathMeasure pathMeasure;
    private float length;
    private float phase;
    private ValueAnimator animator;

    public BorderAnimationView(Context context) {
        super(context);
        init();
    }

    public BorderAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12f); // Increased stroke width
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        path.reset();
        float margin = paint.getStrokeWidth() / 2;
        path.addRect(margin, margin, w - margin, h - margin, Path.Direction.CW);
        pathMeasure = new PathMeasure(path, false);
        length = pathMeasure.getLength();

        // Blue to Purple Gradient
        Shader shader = new LinearGradient(0, 0, w, h,
                new int[]{Color.parseColor("#4285F4"), Color.parseColor("#9B27B0")},
                null, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setShadowLayer(25, 0, 0, Color.parseColor("#9B27B0")); // Increased glow
    }

    public void startAnimation(long duration) {
        stopAnimation();
        setVisibility(VISIBLE);
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        setVisibility(GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (phase == 0) return;

        Path segment = new Path();
        pathMeasure.getSegment(0, length * phase, segment, true);
        canvas.drawPath(segment, paint);
    }
}
