package semicaverns.paint;

import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.util.Timer;
import semicaverns.util.GlobalConstant;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class PaintHelper {

    public static final Timer RUN_TIME = new Timer(0);
    public static int START_EXP = 0, START_LEVEL = 0, BANKS = 0, GOLD = 0, COAL = 0, GOLD_PRICE = 0, COAL_PRICE = 0;

    public static int SLIDER_START = -1, TAB_STATE = 1; //0 = Nothing, 1 = Info, 2 = Settings
    public static final int SLIDER_MIN_X = 284, SLIDER_MAX_X = 319;
    public static final Rectangle[] TAB_BOUNDS = {new Rectangle(466, 399, 26, 26), new Rectangle(435, 399, 26, 26)};
    public static final Rectangle SETTING_BANK = new Rectangle(404, 451, 11, 13), SETTING_ALIVE = new Rectangle(428, 470, 11, 13);

    public static Point MOUSE_POINT = new Point(0, 0);

    public static int getPerHour(final int value) {
        return (int) (value * 3600000d / PaintHelper.RUN_TIME.getElapsed());
    }

    public static int getSliderX(){
        return SLIDER_START == -1 ? (GlobalConstant.MINE_GOLD ? SLIDER_MIN_X : SLIDER_MAX_X) :
                Math.max(Math.min((int) MOUSE_POINT.getX() - SLIDER_START, SLIDER_MAX_X), SLIDER_MIN_X);
    }

    public static String format(final int number, final boolean shorten) {
        if(shorten && number > 999999) {
            return Math.round(number / 1000d) + "k";
        }
        return String.format("%,d", number);
    }

    public static void cursor(final Graphics2D g2d, final Color base) {
        final AffineTransform at = new AffineTransform(), atCurrent = g2d.getTransform();
        at.rotate(Math.toRadians(System.currentTimeMillis() % (360 * 5) / 5), Mouse.getX(), Mouse.getY());
        g2d.setStroke(new BasicStroke(2));
        g2d.setTransform(at);
        g2d.setColor(Mouse.getPressTime() != -1 && System.currentTimeMillis() - Mouse.getPressTime() < 1000 ? Color.RED : base);
        g2d.drawLine(Mouse.getX() - 4, Mouse.getY() - 4, Mouse.getX() - 2, Mouse.getY() - 2);
        g2d.drawLine(Mouse.getX() - 4, Mouse.getY() + 4, Mouse.getX() - 2, Mouse.getY() + 2);
        g2d.drawLine(Mouse.getX() + 4, Mouse.getY() - 4, Mouse.getX() + 2, Mouse.getY() - 2);
        g2d.drawLine(Mouse.getX() + 4, Mouse.getY() + 4, Mouse.getX() + 2, Mouse.getY() + 2);
        g2d.setTransform(atCurrent);
        g2d.setColor(base.darker());
        g2d.drawOval(Mouse.getX() - 6, Mouse.getY() - 6, 12, 12);
        g2d.drawOval(Mouse.getX() - 2, Mouse.getY() - 2, 4, 4);
    }

}
