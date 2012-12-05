package semicaverns.util.walking;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.Tile;
import semicaverns.util.PauseHandler;

public class Traverse {

    public static Tile[] TILES = null;

    public static boolean walk(final Locatable destination) {
        final Tile[] tiles = TILES = Nodes.Pathfinder.generate(Walking.getCollisionFlags(Game.getPlane()), Walking.getCollisionOffset(Game
                .getPlane()).derive(Game.getBaseX(), Game.getBaseY()), Players.getLocal().getLocation(), destination.getLocation());
        if(tiles != null){
            Tile next = tiles[tiles.length - 1];
            for(final Tile tile : tiles){
                if(tile.isOnMap() && Calculations.distance(tile, destination) < Calculations.distance(next, destination))
                    next = tile;
            }
            return PauseHandler.walk((next == null ? tiles[0] : next).randomize(1, 1), (long) Random.nextInt(750, 1500));
        }
        return PauseHandler.walk(destination.getLocation().randomize(1, 1), (long) Random.nextInt(750, 1500));
/*        Tile next = null;
        if(Calculations.distanceTo(destination) > 10 || !destination.isOnMap())
            for(final Tile[] tiles : GlobalConstant.WALKING_NODES)
                for(final Tile tile : tiles) {
                    if(tile.isOnMap() && (next == null || Calculations.distance(tile, destination)
                            < Calculations.distance(next, destination)))
                        next = tile;
                }
        else if(destination.isOnMap()) {
            next = destination;
        }
        return (Walking.getDestination() != null && Calculations.distance(Walking.getDestination(), destination) < 5)
                || Walking.walk(Walking.getClosestOnMap(next != null ? next.randomize(2, 2) : destination.randomize(2, 2)));*/
    }

}
