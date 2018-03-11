package it.polito.did.arduino_lamp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mlnmtt on 10/12/17.
 */

public class LampView extends View {

    // variabili
    private Paint paint1;
    private float angle = 90;
    private Path path1;

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
        paint1.setColor(0xffffffff);
        paint1.setStrokeWidth(10.0f);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setPathEffect(new CornerPathEffect(15));

        path1 = new Path();

        setMinimumWidth(100);
        setMinimumHeight(100);
    }

    // metodi canvas
    public void setAngle(float angle) {
        if(angle>= 0 && angle<=2000 && this.angle != angle) {
            this.angle = angle;
            invalidate();
        }
    }

    public float getAngle() {
        return this.angle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float h = canvas.getHeight();
        float w = canvas.getWidth();
        float l = Math.min(w,h)*0.8f;
        double rad = Math.toRadians(angle);

        /*
        path1.rewind();
        path1.moveTo((float)(w/2-l/3+l/4*Math.cos(Math.PI - rad)), (float)(h/2+l/4*Math.sin(Math.PI - rad)));
        path1.lineTo(w/2-l/3, h/2);
        path1.lineTo(w/2-l/3, h/2-l/4);
        path1.lineTo(w/2+l/3, h/2-l/4);
        path1.lineTo(w/2+l/3, h/2);
        path1.lineTo((float)(w/2+l/3+l/4*Math.cos(rad)), (float)(h/2+l/4*Math.sin(rad)));
        path1.addCircle(w/2+l/3, h/2, 5, Path.Direction.CW);
        path1.addCircle(w/2-l/3, h/2, 5, Path.Direction.CW);

        canvas.drawPath(path1, paint1);
        */

        // disegna il disegno dinamico della lampada
        //canvas.drawLine(w/2-l/3, h/2-l/4, w/2+l/3, h/2-l/4, paint1);
        // canvas.drawLine(w/2-l/3, h/2-l/4, w/2-l/3, h/2, paint1);
        // canvas.drawLine(w/2+l/3, h/2-l/4, w/2+l/3, h/2, paint1);

        // disegna la nostra lampada?
        canvas.drawLine(w/2-l/3, h/2-l/4, w/2, h/2+l/2, paint1);
        canvas.drawLine(w/2+l/3, h/2-l/4, w/2, h/2+l/2, paint1);
        canvas.drawCircle(w/2+l/3, h/2, 5, paint1);
        canvas.drawCircle(w/2-l/3, h/2, 5, paint1);
        canvas.drawLine(w/2+l/3, h/2, (float)(w/2+l/3+l/3*Math.cos(rad)), (float)(h/2+l/3*Math.sin(rad)), paint1);
        canvas.drawLine(w/2-l/3, h/2, (float)(w/2-l/3+l/3*Math.cos(Math.PI - rad)), (float)(h/2+l/3*Math.sin(Math.PI - rad)), paint1);
    }
}
