package semicaverns.nodes;

import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.*;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import semicaverns.nodes.internal.Checks;
import semicaverns.util.walking.Traverse;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;
import semicaverns.util.walking.Nodes;

import java.util.Arrays;
import java.util.Comparator;

public class EvadeCombat extends Node {

    @Override
    public boolean activate() {
        int index = 0;
        final Tile[] tiles = GlobalConstant.TILE_ROCKS[Checks.isGold()];
        for(int i = 1; i < tiles.length; i++) {
            if(Calculations.distanceTo(tiles[i]) < Calculations.distanceTo(tiles[index]))
                index = i;
        }
        if(GlobalConstant.WIELDED_ID != -1 && (Tabs.getCurrent().equals(Tabs.INVENTORY)
                || Tabs.getCurrent().equals(Tabs.ATTACK)) && Settings.get(300) == 1000) {
            if(Checks.getLP() < Skills.getRealLevel(Skills.CONSTITUTION) * 10 - 200)
                return true;
        }
        return !Banker.isDepositOpen() && ((inCombat()/* && Calculations.distanceTo(tiles[index]) < 8 && !Inventory.isFull()*/)
                || Checks.isOutside() || (GlobalConstant.KEEP_ALIVE && Checks.getLP() < Skills.getRealLevel(Skills.CONSTITUTION) * 0.4f * 10));
    }

    private boolean inCombat() {
        return Players.getLocal().isInCombat() || NPCs.getNearest(new Filter<NPC>() {
            @Override
            public boolean accept(final NPC npc) {
                return npc.getInteracting() != null && npc.getInteracting().equals(Players.getLocal())
                        && Arrays.binarySearch(GlobalConstant.LRC_NPC, npc.getId()) >= 0;
            }
        }) != null;
    }

    @Override
    public void execute() {
        if(GlobalConstant.WIELDED_ID != -1 && Settings.get(300) == 1000 && Checks.getLP()
                < Skills.getRealLevel(Skills.CONSTITUTION) * 10 - 200) {
            if(Players.getLocal().getAppearance()[GlobalConstant.WEAPON] != GlobalConstant.EXCALIBUR
                    && (Tabs.getCurrent().equals(Tabs.INVENTORY) || Tabs.INVENTORY.open())) {
                final Item excalibur = Inventory.getItem(GlobalConstant.EXCALIBUR);
                if(excalibur != null) {
                    excalibur.getWidgetChild().click(true);
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return Players.getLocal().getAppearance()[GlobalConstant.WEAPON] == GlobalConstant.EXCALIBUR;
                        }
                    }, (long) Random.nextInt(750, 1500));
                }
            } else if(Tabs.getCurrent().equals(Tabs.ATTACK) || Tabs.ATTACK.open()) {
                final WidgetChild bar = Widgets.get(884, 4);
                if(bar.validate()) {
                    bar.click(true);
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return Settings.get(300) != 1000;
                        }
                    }, (long) Random.nextInt(400, 800));
                }
            }
            return;
        }
        if(GlobalConstant.KEEP_ALIVE && Checks.getLP() < Skills.getRealLevel(Skills.CONSTITUTION) * 0.4f * 10) {
            if(Checks.isOutside()) {
                if(Players.getLocal().getAnimation() == -1) {
                    final WidgetChild[] widgets = {Widgets.get(750, 2), Widgets.get(750, 6)};
                    if(widgets[0].validate() && widgets[1].validate()) {
                        if(widgets[Random.nextInt(0, widgets.length)].interact("Rest"))
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Players.getLocal().getAnimation() != -1;
                                }
                            }, (long) Random.nextInt(750, 1500));
                    }
                }
                Task.sleep(400, 800);
            } else {
                if(Calculations.distanceTo(GlobalConstant.TILE_BANK) > 5) {
                    if(Traverse.walk(GlobalConstant.TILE_BANK)) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 8;
                            }
                        }, (long) Random.nextInt(500, 1000));
                    }
                }
            }
        } else {
            if(Checks.isOutside()) {
                final SceneObject ladder = SceneEntities.getNearest(GlobalConstant.ROPE_DOWN_ID);
                if(ladder != null && ladder.interact("Climb"))
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return !Checks.isOutside();
                        }
                    }, 750l);
                else if(ladder != null && Calculations.distanceTo(ladder) > 5)
                    PauseHandler.walk(ladder, (long) Random.nextInt(250, 750));
            } else if(inCombat() || Players.getLocal().isInCombat()) {
                final Tile rockTile = GlobalConstant.TILE_ROCKS[Checks.isGold()][Mine.getCurrent()];
                if(true || Calculations.distanceTo(GlobalConstant.TILE_BANK) < Calculations.distanceTo(rockTile)) {
                    if(Calculations.distanceTo(GlobalConstant.TILE_BANK) > 6 && Traverse.walk(GlobalConstant.TILE_BANK)) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 8;
                            }
                        }, (long) Random.nextInt(200, 500));
                    }
                } else {
                    final SceneObject rock = SceneEntities.getAt(rockTile);
                    final NPC npc = NPCs.getNearest(new Filter<NPC>() {
                        @Override
                        public boolean accept(final NPC npc) {
                            return npc.getInteracting() != null && npc.getInteracting().equals(Players.getLocal())
                                    && Arrays.binarySearch(GlobalConstant.LRC_NPC, npc.getId()) >= 0;
                        }
                    });
                    if(rock != null && npc != null) {
                        final Tile hardcodedSafe = GlobalConstant.MINE_GOLD && Mine.getCurrent() == 0 ? GlobalConstant.GOLD_SAFE_SPOT
                                : !GlobalConstant.MINE_GOLD && Mine.getCurrent() == 2 ? GlobalConstant.COAL_SAFE_SPOT : null;
                        if(hardcodedSafe != null) {
                            hardcodedSafe.randomize(0, Mine.getCurrent() == 2 ? 4 : 1, 1, Mine.getCurrent() == 0 ? -4 : 1).clickOnMap();
                        } else {
                            final Tile[] bounds = rock.getArea().getBoundingTiles();
                            Arrays.sort(bounds, new Comparator<Tile>() {
                                @Override
                                public int compare(final Tile t1, final Tile t2) {
                                    return Calculations.distance(t1, npc.getLocation()) < Calculations
                                            .distance(t2, npc.getLocation()) ? 1 : -1;
                                }
                            });
                            final int[][] flags = Walking.getCollisionFlags(Game.getPlane());
                            final Tile colOffset = Walking.getCollisionOffset(Game.getPlane()).derive(Game.getBaseX(), Game.getBaseY());
                            Tile toWalk = null;
                            for(final int[] offset : new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}}) {
                                final Tile derive = bounds[0].derive(offset[0], offset[1]);
                                if(Nodes.walkable(flags, colOffset, derive)) {
                                    if(toWalk == null || Calculations.distance(derive, npc)
                                            > Calculations.distance(toWalk, npc))
                                        toWalk = derive;
                                }
                            }
                            if(toWalk != null) {
                                if(!toWalk.isOnScreen())
                                    Camera.turnTo(toWalk);
                                toWalk.interact("Walk here");
                            }
                        }
                        Task.sleep(100, 300);
                        final int lp = Checks.getLP();
                        final Timer timer = new Timer((long) Random.nextInt(7500, 10000));
                        while(Players.getLocal().isInCombat() && Checks.getLP() >= lp && !Context.get()
                                .getScriptHandler().isPaused() && timer.isRunning())
                            Task.sleep(200, 800);
                    }
                }
            }
        }
    }
}
