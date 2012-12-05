package semicaverns.util.walking;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Nodes {

    public static Tile[] findFreeArea(final int[][] flags, final Tile offset, final int radius, final Dimension areaDim) {
        final List<Tile> valid = new ArrayList<>();
        for(int i = -radius; i < radius - 1; i++) {
            for(int k = -radius; k < radius - 1; k++) {
                final Tile loc = Players.getLocal().getLocation().derive(i, k);
                if(walkable(flags, offset, new Area(loc.derive(-areaDim.width / 2, -areaDim.height / 2),
                        loc.derive(areaDim.width / 2, areaDim.height / 2))))
                    valid.add(loc);
            }
        }
        Collections.sort(valid, new Comparator<Tile>() {
            @Override
            public int compare(final Tile t1, final Tile t2) {
                return Calculations.distanceTo(t1) < Calculations.distanceTo(t2) ? -1 : 1;
            }
        });
        return valid.toArray(new Tile[valid.size()]);
    }

    public static boolean walkable(final int[][] flags, final Tile offset, final Area area) {
        for(final Tile tile : area.getTileArray()) {
            if(!walkable(flags, offset, tile))
                return false;
        }
        return true;
    }

    public static boolean walkable(final int[][] flags, final Tile offset, final Tile tile) {
        return flags[tile.getX() - offset.getX()][tile.getY() - offset.getY()] == 0x0;
    }

    public static class Pathfinder {

        public static Tile[] generate(final int[][] flags, final Tile offset, final Tile start, final Tile end) {
            final List<TileNode> open = new ArrayList<>(), closed = new ArrayList<>();
            final Timer timer = new Timer(750l);
            open.add(new TileNode(start, 0, null));
            while(!open.isEmpty() && timer.isRunning()) {
                Collections.sort(open, new Comparator<TileNode>() {
                    @Override
                    public int compare(final TileNode t1, final TileNode t2) {
                        final int f1 = (int) Calculations.distance(t1.tile, end) + t1.g,
                                f2 = (int) Calculations.distance(t2.tile, end) + t2.g;
                        return f1 > f2 ? 1 : f1 < f2 ? -1 : 0;
                    }
                });
                final TileNode next = open.remove(0);
                closed.add(next);
                if(next.tile.equals(end))
                    return trace(closed, start, end);
                for(final int[] i : new int[][]{{-1, -1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, 1}, {-1, 0}}) { //nw clockwise
                    final Tile derived = next.tile.derive(i[0], i[1]);
                    final TileNode neighbour = new TileNode(derived, next.g + (Math.abs(i[0]) + Math.abs(i[1])
                            == 2 ? 14 : 10), next), current = getFromList(open, derived);
                    if(getFromList(closed, derived) != null || (!walkable(flags, offset, derived) && !derived.equals(end)))
                        continue;
                    if(current == null)
                        open.add(neighbour);
                    else {
                        if(neighbour.g < current.g) {
                            current.parent = neighbour.parent;
                            current.g = neighbour.g;
                        }
                    }
                }
            }
            return null;
        }

        private static Tile[] trace(final List<TileNode> pathTiles, final Tile start, final Tile end) {
            final List<Tile> tiles = new ArrayList<>();
            TileNode current = getFromList(pathTiles, end);
            while(!tiles.contains(start)) {
                tiles.add((current = current.parent).tile);
            }
            return tiles.toArray(new Tile[tiles.size()]);
        }

        private static TileNode getFromList(final List<TileNode> list, final Tile tile) {
            for(final TileNode node : list)
                if(node.tile.equals(tile)) {
                    return node;
                }
            return null;
        }

        public static class TileNode {
            public final Tile tile;
            private TileNode parent;
            private int g;

            private TileNode(final Tile tile, final int g, final TileNode parent) {
                this.g = g;
                this.tile = tile;
                this.parent = parent;
            }
        }
    }

}
