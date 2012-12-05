package semicaverns.util;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.Tile;

public class PauseHandler {

    public static boolean walk(final Locatable locatable, final long delay) {
        final Tile loc = locatable.getLocation();
        return ((Walking.getDestination() != null && Calculations.distance(Walking.getDestination(), loc) < 5)
                || (loc.isOnScreen() ? loc.click(true) : Walking.walk(Walking.getClosestOnMap(loc.randomize(2, 2))))) &&
                pause(new Condition() {
                    public boolean validate() {
                        return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 8;
                    }
                }, delay);
    }

    public static boolean widget(final int widgetId, final long delay) {
        return pause(new Condition() {
            public boolean validate() {
                return Widgets.get(widgetId).validate();
            }
        }, delay);
    }

    public static boolean pause(final Condition main, final long delay) {
        final Timer timer = new Timer(delay);
        while(timer.isRunning() && !main.validate()) {
            if(Players.getLocal().isMoving())
                timer.reset();
            Task.sleep(10, 50);
        }
        return main.validate();
    }

    public static abstract class Condition {
        public abstract boolean validate();
    }

}
