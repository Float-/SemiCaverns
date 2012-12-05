package semicaverns.nodes.feature;

import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.GroundItems;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.GroundItem;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import semicaverns.nodes.internal.Checks;
import semicaverns.util.walking.Traverse;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;

public class Outside extends Node {

    private boolean NO_FALADOR = false;
    private static Tile DeathTile = null;

    @Override
    public boolean activate() {
        return Checks.isOutside() || Widgets.get(18).validate();
    }

    @Override
    public void execute() {
        final Widget death = Widgets.get(18);
        if(death.validate()) {
            final WidgetChild hub = death.getChild(29).getChild(3);
            if(hub.validate() && hub.getText() != null && hub.getText().length() > 0) {
                if(hub.getText().toLowerCase().contains("falador") || NO_FALADOR) {
                    final WidgetChild confirm = death.getChild(34);
                    if(confirm.validate() && confirm.click(true))
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return !death.validate();
                            }
                        }, (long) Random.nextInt(750, 1500));
                } else {
                    final WidgetChild list = death.getChild(45);
                    if(list.validate() && list.visible()) {
                        WidgetChild falador = null;
                        for(final WidgetChild wc : list.getChildren()) {
                            if(wc.validate() && wc.getText() != null && wc.getText().toLowerCase().contains("falador"))
                                falador = wc;
                        }
                        if(falador != null && falador.validate() && falador.visible()) {
                            if(falador.click(true))
                                PauseHandler.pause(new PauseHandler.Condition() {
                                    @Override
                                    public boolean validate() {
                                        return list.validate() && !list.visible();
                                    }
                                }, (long) Random.nextInt(750, 1500));
                        } else NO_FALADOR = true;
                    } else {
                        final WidgetChild downArrow = death.getChild(29).getChild(2);
                        if(downArrow.validate() && downArrow.click(true))
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return list.validate() && list.visible();
                                }
                            }, (long) Random.nextInt(750, 1500));
                    }
                }
            }
        } else if(Checks.isInWorld()) {
            if(Checks.isInFalador()) {
                final SceneObject ladder = SceneEntities.getNearest(GlobalConstant.LADDER_DOWN_ID);
                if(ladder != null) {
                    if(Calculations.distanceTo(ladder) > 10) {
                        Traverse.walk(ladder);
                    } else if(!ladder.isOnScreen()) {
                        Camera.turnTo(ladder, 2);
                    } else if(ladder.interact("Climb-down")) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return !Checks.isInWorld();
                            }
                        }, (long) Random.nextInt(750, 2000));
                    }
                } else {
                    Tile next = null;
                    for(final Tile tile : new Tile[]{new Tile(3007, 3345, 0), new Tile(2994, 3369, 0), new Tile(2965, 3382, 0),
                            new Tile(2968, 3412, 0), new Tile(2989, 3427, 0), new Tile(3014, 3432, 0), new Tile(3015, 3451, 0)})
                        if(tile.validate() && Calculations.distanceTo(tile) < 50) {
                            next = tile;
                        }
                    Traverse.walk(next == null ? GlobalConstant.TILE_LADDER : next);
                }
            } else if(Players.getLocal().getAnimation() == -1 && (Tabs.getCurrent().equals(Tabs.MAGIC) || Tabs.MAGIC.open())) {
                final WidgetChild falador = Widgets.get(1092, 46);
                if(falador.validate()) {
                    if(falador.getTextureId() != 10103) {
                        Context.get().getScriptHandler().log.severe("You haven't unlocked the Falador lodestone!");
                        Context.get().getScriptHandler().log.severe("Script can't make it back. Sorry for the potential loss.");
                        Context.get().getScriptHandler().shutdown();
                    } else {
                        falador.click(true);
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return Players.getLocal().getAnimation() != -1;
                            }
                        }, (long) Random.nextInt(1000, 3000));
                    }
                } else {
                    final WidgetChild magicTeleport = Widgets.get(275, 46);
                    if(magicTeleport.validate()){
                        if(magicTeleport.visible()){
                            final WidgetChild home = Widgets.get(275, 16).getChild(155);
                            if(home.validate() && home.visible()){
                                home.click(true);
                                PauseHandler.widget(1092, (long) Random.nextInt(1000, 1500));
                            } else {
                                final WidgetChild homeTab = Widgets.get(275, 38);
                                if(homeTab.validate() && homeTab.click(true))
                                    PauseHandler.pause(new PauseHandler.Condition() {
                                        @Override
                                        public boolean validate() {
                                            return home.validate() && home.visible();
                                        }
                                    }, (long) Random.nextInt(750, 1500));
                            }
                        } else {
                            final WidgetChild magicTab = Widgets.get(275, 40);
                            if(magicTab.validate() && magicTab.click(true))
                                PauseHandler.pause(new PauseHandler.Condition() {
                                    @Override
                                    public boolean validate() {
                                        return magicTeleport.validate() && magicTeleport.visible();
                                    }
                                }, (long) Random.nextInt(750, 1500));
                        }
                    }
                }
            } else System.out.println(9);
        } else {
            if(Summoning.getPoints() < Skills.getRealLevel(Skills.SUMMONING)) {
                final SceneObject obelisk = SceneEntities.getNearest(GlobalConstant.OBELISK_ID);
                if(obelisk != null) {
                    if(Calculations.distanceTo(obelisk) > 10) {
                        Traverse.walk(obelisk);
                    } else if(!obelisk.isOnScreen()) {
                        Camera.turnTo(obelisk, 2);
                    } else if(obelisk.interact("Renew-points")) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return Summoning.getPoints() == Skills.getRealLevel(Skills.SUMMONING);
                            }
                        }, (long) Random.nextInt(750, 2000));
                    }
                } else {
                    Traverse.walk(GlobalConstant.TILE_OBELISK);
                }
            } else {
                final SceneObject rope = SceneEntities.getNearest(GlobalConstant.ROPE_DOWN_ID);
                if(rope != null) {
                    if(Calculations.distanceTo(rope) > 10) {
                        Traverse.walk(rope);
                    } else if(!rope.isOnScreen()) {
                        Camera.turnTo(rope, 2);
                    } else if(rope.interact("Climb")) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return !Checks.isOutside();
                            }
                        }, (long) Random.nextInt(750, 2000));
                    }
                } else {
                    Traverse.walk(GlobalConstant.TILE_ROPE);
                }
            }
        }
    }

    public static boolean findGrave() {
        final NPC grave = NPCs.getNearest(new Filter<NPC>() {
            @Override
            public boolean accept(final NPC npc) {
                return npc.validate() && GroundItems.getLoaded(new Filter<GroundItem>() {
                    @Override
                    public boolean accept(final GroundItem item) {
                        return item != null && item.getLocation().equals(npc.getLocation()) && GlobalConstant.CACHE.contains(item.getId());
                    }
                }).length > 0;
            }
        });
        if(grave != null) {
            DeathTile = grave.getLocation();
            if(Calculations.distanceTo(grave) > 10) {
                Traverse.walk(grave);
            } else if(!grave.isOnScreen()) {
                Camera.turnTo(grave, 2);
            } else {
                for(final GroundItem item : GroundItems.getLoaded(new Filter<GroundItem>() {
                    @Override
                    public boolean accept(final GroundItem item) {
                        return item != null && item.getLocation().equals(DeathTile) && GlobalConstant.CACHE.contains(item.getId());
                    }
                })) {
                    if(item != null) {
                        item.interact("Take", item.getGroundItem().getName());
                    }
                }
            }
            return true;
        } else if(DeathTile != null) {
            final NPC stone = NPCs.getNearest(new Filter<NPC>() {
                @Override
                public boolean accept(final NPC npc) {
                    return npc.getLocation().equals(DeathTile);
                }
            });
            if(stone != null) {
                if(Calculations.distanceTo(stone) > 10) {
                    Traverse.walk(stone);
                } else if(!stone.isOnScreen()) {
                    Camera.turnTo(stone, 2);
                } else if(stone.interact("Demolish")) {
                    Task.sleep(750, 1750);
                }
            }
            return true;
        }
        return false;
    }
}
