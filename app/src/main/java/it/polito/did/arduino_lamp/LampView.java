package it.polito.did.arduino_lamp;

/**
 * Created by mlnmtt on 20/05/2018.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class LampView extends View {

    // variabili
    private Path path1;
    private Paint paint1;
    private float angleL = 90;
    private float angleR = 90;

    // costruttori
    public LampView(Context ctx) {
        this(ctx, null, 0);
    }
    public LampView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }
    public LampView(Context ctx, AttributeSet attrs, int theme) {
        super(ctx, attrs, theme);

        paint1 = new Paint();
        paint1.setColor(0xffffa500);
        paint1.setStrokeWidth(10.0f);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setPathEffect(new CornerPathEffect(15));

        path1 = new Path();

        setMinimumWidth(100);
        setMinimumHeight(100);
    }

    // metodi canvas
    public void setAngleL(float angle) {
        if(angle>= 0 && angle<=2000 && this.angleL != angle) {
            this.angleL = angle;
            invalidate();
        }
    }

    public void setAngleR(float angle) {
        if(angle>= 0 && angle<=2000 && this.angleR != angle) {
            this.angleR = angle;
            invalidate();
        }
    }

    public float getAngleL() {
        return this.angleL;
    }

    public float getAngleR() { return angleR; }

    @Override
    protected void onDraw(Canvas canvas) {
        float h = canvas.getHeight();
        float w = canvas.getWidth();
        float l = Math.min(w,h)*0.8f;
        double radL = Math.toRadians(angleL);
        double radR = Math.toRadians(angleR);

        path1 = new Path();
        path1.moveTo(w/2+l/3, h/2);
        path1.cubicTo(w/2+l/3, h/2, w/2, h/100, w/2-l/3, h/2);
        path1.lineTo(w/2+l/3, h/2);
        canvas.drawPath(path1, paint1);
        canvas.drawCircle(w/2+l/3, h/2, 5, paint1);
        canvas.drawCircle(w/2-l/3, h/2, 5, paint1);
        canvas.drawLine(w/2+l/3, h/2, (float)(w/2+l/3+l/3*Math.cos(radR)), (float)(h/3+l/2*Math.sin(radR)), paint1);
        canvas.drawLine(w/2-l/3, h/2, (float)(w/2-l/3+l/3*Math.cos(Math.PI - radL)), (float)(h/3+l/2*Math.sin(Math.PI - radL)), paint1);
    }
}