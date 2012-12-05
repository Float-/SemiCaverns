package semicaverns.nodes.feature;

import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import semicaverns.nodes.internal.Checks;
import semicaverns.util.walking.Traverse;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;

public class Summoning extends Node {

    private static final int WIDGET_ID = 747, WIDGET_TEXTURE = 0, WIDGET_POINTS = 7, TEXTURE_ID = 1244;
    public static boolean TIGHT_AREA = false;

    @Override
    public boolean activate() {
        return GlobalConstant.CAN_SUMMON && !isSummoned();
    }

    @Override
    public void execute() {
        if(getPoints() < 10) {
            if(!Checks.isOutside()) {
                final SceneObject rope = SceneEntities.getNearest(GlobalConstant.ROPE_UP_ID);
                if(rope != null) {
                    if(Calculations.distanceTo(rope) < 5 && rope.isOnScreen()) {
                        if(rope.interact("Climb")) {
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Checks.isOutside();
                                }
                            }, (long) Random.nextInt(500, 1500));
                        }
                    } else PauseHandler.walk(rope, (long) Random.nextInt(500, 1500));
                } else if(Traverse.walk(GlobalConstant.TILE_BANK)) {
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 8;
                        }
                    }, (long) Random.nextInt(500, 1000));
                }
            }
        } else {
            final Item pouch = Inventory.getItem(GlobalConstant.POUCHES);
            if(pouch != null) {
/*                final int[][] flags = Walking.getCollisionFlags(Game.getPlane());
                final Tile offset = Walking.getCollisionOffset(Game.getPlane()).derive(Game.getBaseX(), Game.getBaseY());
                if(Nodes.walkable(flags, offset, new Area(Players.getLocal().getLocation().derive(-2, -2),
                        Players.getLocal().getLocation().derive(2, 2)))) {*/
                if(!TIGHT_AREA){
                    if(pouch.getWidgetChild().interact("Summon")) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return isSummoned();
                            }
                        }, (long) Random.nextInt(500, 2000));
                    }
                } else {
                    Traverse.walk(GlobalConstant.TILE_BANK);
                    TIGHT_AREA = false;
                }
/*                } else {
                    final Tile[] walkable = Nodes.findFreeArea(flags, offset, 10, new Dimension(2, 2));
                    if(walkable != null && walkable.length > 0) {
                        final Tile tile = walkable[0].randomize(1, 1);
                        if(tile != null && Nodes.walkable(flags, offset, tile)) {
                            if(tile.isOnScreen())
                                tile.click(true);
                            else
                                tile.clickOnMap();
                            Task.sleep(400, 800);
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return !Players.getLocal().isMoving();
                                }
                            }, (long) Random.nextInt(750, 1500));
                        }
                    } else if(Traverse.walk(GlobalConstant.TILE_BANK)) {
                        Task.sleep(750, 1500);
                    }
                }*/
            } else {
                GlobalConstant.CAN_SUMMON = false;
                Context.get().getScriptHandler().log.severe("No pouches left, turning summoning off.");
            }
        }
    }

    private boolean isSummoned() {
        final WidgetChild wc = Widgets.get(WIDGET_ID, WIDGET_TEXTURE);
        return wc.validate() && wc.getTextureId() != TEXTURE_ID;
    }

    public static int getPoints() {
        final WidgetChild wc = Widgets.get(WIDGET_ID, WIDGET_POINTS);
        return wc.validate() ? Integer.valueOf(wc.getText()) : -1;
    }
}
