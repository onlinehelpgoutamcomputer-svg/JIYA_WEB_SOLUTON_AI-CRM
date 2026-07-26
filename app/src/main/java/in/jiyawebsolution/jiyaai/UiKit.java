package in.jiyawebsolution.jiyaai;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

public final class UiKit {
    public static final int BG = 0xFF050811;
    public static final int PANEL = 0xFF0C1422;
    public static final int PANEL_2 = 0xFF101B2C;
    public static final int CYAN = 0xFF35F4FF;
    public static final int VIOLET = 0xFFA96BFF;
    public static final int GREEN = 0xFF65FFB1;
    public static final int RED = 0xFFFF5577;
    public static final int TEXT = 0xFFF4F7FC;
    public static final int MUTED = 0xFF8D9AAF;

    private UiKit() {}

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable background(int color, int radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable outlined(int color, int stroke, int radiusDp, Context context) {
        GradientDrawable drawable = background(color, radiusDp, context);
        drawable.setStroke(dp(context, 1), stroke);
        return drawable;
    }

    public static void styleButton(TextView view, int background, int text, Context context) {
        view.setTextColor(text);
        view.setTextSize(14);
        view.setGravity(android.view.Gravity.CENTER);
        view.setAllCaps(false);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setBackground(background(background, 16, context));
        view.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        view.setElevation(dp(context, 3));
    }

    public static void pressEffect(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(.97f).scaleY(.97f).setDuration(80).start();
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                    || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        });
    }
}
