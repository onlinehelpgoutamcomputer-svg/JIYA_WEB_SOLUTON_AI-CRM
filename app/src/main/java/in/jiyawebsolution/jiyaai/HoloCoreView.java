package in.jiyawebsolution.jiyaai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

public class HoloCoreView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float phase = 0f;
    private boolean listening;

    public HoloCoreView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        postOnAnimation(tick);
    }
    public void setListening(boolean value) { listening = value; invalidate(); }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            phase = (phase + (listening ? 3.2f : 1.2f)) % 360f;
            invalidate(); postOnAnimation(this);
        }
    };

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h / 2f;
        canvas.drawColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.STROKE);

        for (int i = 0; i < 6; i++) {
            float radius = Math.min(w, h) * (.14f + i * .065f);
            paint.setStrokeWidth(i == 2 ? 5 : 2);
            paint.setColor(i % 2 == 0 ? 0xAA35F4FF : 0x77A96BFF);
            paint.setShadowLayer(listening ? 20 : 9, 0, 0, paint.getColor());
            canvas.drawArc(cx - radius, cy - radius * .58f, cx + radius, cy + radius * .58f,
                    phase * (i % 2 == 0 ? 1 : -1) + i * 31, 230 - i * 14, false, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(cx, cy - 90, cx, cy + 90,
                new int[]{0xFF35F4FF, 0xFF5B50FF, 0xFFA96BFF}, null, Shader.TileMode.CLAMP));
        paint.setShadowLayer(listening ? 35 : 18, 0, 0, 0xFF35F4FF);
        Path diamond = new Path();
        float pulse = listening ? (float)(8 * Math.sin(Math.toRadians(phase * 3))) : 0;
        diamond.moveTo(cx, cy - 82 - pulse);
        diamond.lineTo(cx + 62 + pulse, cy);
        diamond.lineTo(cx, cy + 82 + pulse);
        diamond.lineTo(cx - 62 - pulse, cy);
        diamond.close();
        canvas.drawPath(diamond, paint);
        paint.setShader(null);

        paint.setColor(0xEE07101F);
        Path inner = new Path();
        inner.moveTo(cx, cy - 59); inner.lineTo(cx + 43, cy);
        inner.lineTo(cx, cy + 59); inner.lineTo(cx - 43, cy); inner.close();
        canvas.drawPath(inner, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextSize(32); paint.setColor(Color.WHITE); paint.clearShadowLayer();
        canvas.drawText("JIYA", cx, cy + 10, paint);
    }
}
