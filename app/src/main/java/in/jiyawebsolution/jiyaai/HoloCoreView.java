package in.jiyawebsolution.jiyaai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

public class HoloCoreView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float phase = 0f;
    private boolean listening;
    private ThemeCatalog.Theme theme = ThemeCatalog.ALL[0];

    public HoloCoreView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        postOnAnimation(tick);
    }

    public void setListening(boolean value) { listening = value; invalidate(); }
    public void setTheme(ThemeCatalog.Theme value) { theme = value; invalidate(); }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            phase = (phase + (listening ? 3.2f : 1.15f)) % 360f;
            invalidate();
            postOnAnimation(this);
        }
    };

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h / 2f;
        drawBackdrop(canvas, cx, cy, w, h);
        drawRings(canvas, cx, cy, w, h);
        drawCore(canvas, cx, cy);
        drawTelemetry(canvas, cx, cy, w, h);
    }

    private void drawBackdrop(Canvas canvas, float cx, float cy, float w, float h) {
        paint.clearShadowLayer();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(withAlpha(theme.primary, 38));

        if (theme.pattern == 0 || theme.pattern == 3) {
            for (int x = 0; x < w; x += Math.max(24, (int) w / 12))
                canvas.drawLine(x, 0, x, h, paint);
            for (int y = 0; y < h; y += Math.max(24, (int) h / 10))
                canvas.drawLine(0, y, w, y, paint);
        } else if (theme.pattern == 1 || theme.pattern == 4) {
            for (int i = 0; i < 7; i++) {
                float y = h * (.15f + i * .115f);
                Path wave = new Path();
                wave.moveTo(0, y);
                for (int x = 0; x <= w; x += 12)
                    wave.lineTo(x, y + (float) Math.sin((x + phase * 2 + i * 31) * .035) * (7 + i));
                canvas.drawPath(wave, paint);
            }
        } else {
            float size = Math.min(w, h) * .09f;
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 6; col++) {
                    float x = col * size * 1.65f + (row % 2) * size * .82f;
                    float y = row * size * 1.42f;
                    drawHex(canvas, x, y, size * .48f, paint);
                }
            }
        }
    }

    private void drawRings(Canvas canvas, float cx, float cy, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 7; i++) {
            float radius = Math.min(w, h) * (.12f + i * .052f);
            paint.setStrokeWidth(i == 2 ? 5 : i % 3 == 0 ? 3 : 1.5f);
            paint.setColor(withAlpha(i % 2 == 0 ? theme.primary : theme.accent, i < 3 ? 220 : 125));
            paint.setShadowLayer(listening ? 24 : 10, 0, 0, paint.getColor());
            float sweep = theme.pattern == 5 ? 58 + i * 8 : 205 - i * 11;
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                    phase * (i % 2 == 0 ? 1 : -1) + i * 39, sweep, false, paint);

            if (theme.pattern == 1 || theme.pattern == 5) {
                float angle = (float) Math.toRadians(phase * (i + 1) + i * 50);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx + (float) Math.cos(angle) * radius,
                        cy + (float) Math.sin(angle) * radius, i % 2 == 0 ? 5 : 3, paint);
                paint.setStyle(Paint.Style.STROKE);
            }
        }
    }

    private void drawCore(Canvas canvas, float cx, float cy) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(cx, cy - 95, cx, cy + 95,
                new int[]{theme.primary, theme.accent, theme.primary}, null, Shader.TileMode.CLAMP));
        paint.setShadowLayer(listening ? 38 : 22, 0, 0, theme.primary);
        float pulse = listening ? (float) (8 * Math.sin(Math.toRadians(phase * 3))) : 0;

        Path core = new Path();
        if (theme.pattern == 2 || theme.pattern == 5) {
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(-90 + i * 60);
                float x = cx + (float) Math.cos(angle) * (68 + pulse);
                float y = cy + (float) Math.sin(angle) * (68 + pulse);
                if (i == 0) core.moveTo(x, y); else core.lineTo(x, y);
            }
        } else if (theme.pattern == 3 || theme.pattern == 4) {
            core.addCircle(cx, cy, 68 + pulse, Path.Direction.CW);
        } else {
            core.moveTo(cx, cy - 84 - pulse);
            core.lineTo(cx + 64 + pulse, cy);
            core.lineTo(cx, cy + 84 + pulse);
            core.lineTo(cx - 64 - pulse, cy);
        }
        core.close();
        canvas.drawPath(core, paint);
        paint.setShader(null);

        paint.setColor(theme.background);
        paint.setShadowLayer(0, 0, 0, 0);
        canvas.drawCircle(cx, cy, 48, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextSize(30);
        paint.setColor(theme.primary);
        canvas.drawText("JIYA", cx, cy + 10, paint);
    }

    private void drawTelemetry(Canvas canvas, float cx, float cy, float w, float h) {
        paint.clearShadowLayer();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(withAlpha(theme.accent, 135));
        float length = Math.min(w, h) * .16f;
        canvas.drawLine(20, 28, 20 + length, 28, paint);
        canvas.drawLine(w - 20 - length, h - 28, w - 20, h - 28, paint);
        canvas.drawLine(20, 28, 20, 58, paint);
        canvas.drawLine(w - 20, h - 58, w - 20, h - 28, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(10);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(withAlpha(theme.primary, 180));
        canvas.drawText("CORE " + String.format(java.util.Locale.US, "%03d", (int) phase), 28, 50, paint);
        canvas.drawText("LINK / SECURE", Math.max(28, w - 118), h - 38, paint);
    }

    private void drawHex(Canvas canvas, float cx, float cy, float radius, Paint p) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(30 + i * 60);
            float x = cx + (float) Math.cos(a) * radius;
            float y = cy + (float) Math.sin(a) * radius;
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, p);
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
    }
}
