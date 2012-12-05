package semicaverns.nodes.internal;

import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.wrappers.Tile;
import semicaverns.util.GlobalConstant;

public class Checks {

    public static boolean isInWorld(){
        return Players.getLocal().getLocation().getY() < 9000;
    }

    public static boolean isOutside(){
        return !GlobalConstant.CAVERN_AREA.contains(Players.getLocal());
    }

    public static boolean isInFalador(){
        final Tile loc = Players.getLocal().getLocation();
        return loc.getX() < 3053 && loc.getX() > 2945 && loc.getY() < 3515 && loc.getY() > 3325;
    }

    public static int getLP(){
        return Widgets.get(748).validate() ? Integer.valueOf(Widgets.get(748, 8).getText()) : -1;
    }

    public static int isGold(){
        return Boolean.compare(!GlobalConstant.MINE_GOLD, false);
    }

}
