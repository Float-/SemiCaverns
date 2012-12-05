package semicaverns.nodes;


import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import semicaverns.nodes.internal.Checks;
import semicaverns.util.walking.Traverse;
import semicaverns.paint.PaintHelper;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;

import java.util.*;

public class Banker extends Node { //Banking, Dropping

    public static boolean ACTIVE = false, COAL_BAG_FULL = false;
    private final int Widget_All = 19, Widget_Close = 15, Widget_Items = 17;

    @Override
    public boolean activate() {
        return true || !Checks.isOutside() && (isDepositOpen() || ACTIVE || (ACTIVE = Inventory.isFull()) || COAL_BAG_FULL);
    }

    @Override
    public void execute() {
        if(!GlobalConstant.BANK) {
            final int[] drop = {GlobalConstant.GOLD_ORE, GlobalConstant.COAL_ORE, GlobalConstant.GOLD_BAR};
            if(Inventory.getCount(drop) == 0) {
                ACTIVE = false;
            } else {
                final WidgetChild[] actionBar = Widgets.get(640).getChildren();
                String keyBind = null;
                for(int i = 0; i < actionBar.length; i++)
                    if(actionBar[i] != null && actionBar[i].validate()) {
                        if(Arrays.binarySearch(drop, actionBar[i].getChildId()) >= 0
                                && actionBar.length >= i + 3) {
                            final WidgetChild textHolder = Widgets.get(640, i + 3);
                            if(textHolder.validate() && textHolder.getText().length() > 0)
                                keyBind = textHolder.getText().trim();
                        }
                    }
                if(keyBind != null) {
                    System.out.println(keyBind);
                    Keyboard.sendText(keyBind, false, 10, 75);
                } else for(final Item item : Inventory.getItems(new Filter<Item>() {
                    @Override
                    public boolean accept(final Item item) {
                        return Arrays.binarySearch(drop, item.getId()) >= 0;
                    }
                })) {
                    if(item != null && Context.get().getScriptHandler().isActive()
                            && !GlobalConstant.BANK) {
                        item.getWidgetChild().interact("Drop");
                    }
                }
                Task.sleep(50, 200);
            }
        } else {
            final Widget deposit = Widgets.get(GlobalConstant.WIDGET_DEPOSIT);
            ACTIVE = false;
            if(deposit.validate()) {
                boolean bad = false, needsDeposit = false;
                for(final WidgetChild wc : deposit.getChild(Widget_Items).getChildren())
                    if(wc.validate()) {
                        if(Arrays.binarySearch(GlobalConstant.GOOD_DEPOSIT, wc.getChildId()) < 0) {
                            bad = true;
                        } else if(wc.getChildId() != -1) {
                            needsDeposit = true;
                        }
                    }
                if(!needsDeposit) {
                    final WidgetChild close = deposit.getChild(Widget_Close);
                    if(close.validate()) {
                        if(close.click(true)) {
                            PaintHelper.BANKS++;
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    final WidgetChild wc = Widgets.get(548, 138);
                                    return !isDepositOpen() && wc.validate() && wc.getTextureId() != -1;
                                }
                            }, (long) Random.nextInt(750, 1250));
                        }
                    }
                } else {
                    if(bad) {
                        final Map<Integer, ArrayList<WidgetChild>> items = new HashMap<>();
                        for(final WidgetChild wc : deposit.getChild(Widget_Items).getChildren()) {
                            if(wc.validate() && wc.getChildId() != -1 && Arrays.binarySearch(
                                    GlobalConstant.GOOD_DEPOSIT, wc.getChildId()) >= 0)
                                if(items.containsKey(wc.getChildId()))
                                    items.get(wc.getChildId()).add(wc);
                                else {
                                    final ArrayList<WidgetChild> list = new ArrayList<>();
                                    list.add(wc);
                                    items.put(wc.getChildId(), list);
                                }
                        }
                        for(final Integer id : items.keySet()) {
                            final List<WidgetChild> list = items.get(id);
                            final WidgetChild wc = list.get(Random.nextInt(0, list.size() - 1));
                            if(wc.validate()) {
                                if(wc.interact("Deposit-All")) {
                                    PauseHandler.pause(new PauseHandler.Condition() {
                                        @Override
                                        public boolean validate() {
                                            for(final WidgetChild wc : deposit.getChild(Widget_Items).getChildren())
                                                if(wc.validate() && wc.getChildId() == id)
                                                    return false;
                                            return true;
                                        }
                                    }, (long) Random.nextInt(500, 1250));
                                }
                            }
                        }
                    } else {
                        final WidgetChild depositAll = deposit.getChild(Widget_All);
                        if(depositAll.validate()) {
                            if(depositAll.click(true)) {
                                PauseHandler.pause(new PauseHandler.Condition() {
                                    @Override
                                    public boolean validate() {
                                        for(final WidgetChild wc : deposit.getChild(Widget_Items).getChildren())
                                            if(wc.validate() && wc.getChildId() != -1)
                                                return false;
                                        return true;
                                    }
                                }, (long) Random.nextInt(1000, 1500));
                            }
                        }
                    }
                }
            } else {
                final Item coalBag = Inventory.getItem(GlobalConstant.COAL_BAG);
                if(coalBag != null && !GlobalConstant.MINE_GOLD) {
                    if(!COAL_BAG_FULL) {
                        final Item coal = Inventory.getItem(GlobalConstant.COAL_ORE);
                        if(coal != null) {
                            coal.getWidgetChild().interact("Use");
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Inventory.getSelectedItem() != null;
                                }
                            }, (long) Random.nextInt(750, 1250));
                            final int coalCount = Inventory.getCount(GlobalConstant.COAL_ORE);
                            if(Inventory.getSelectedItem() != null && coalBag.getWidgetChild().click(true)) {
                                PauseHandler.pause(new PauseHandler.Condition() {
                                    @Override
                                    public boolean validate() {
                                        return Inventory.getCount(GlobalConstant.COAL_ORE) != coalCount || COAL_BAG_FULL;
                                    }
                                }, (long) Random.nextInt(800, 1500));
                                Task.sleep(400, 800);
                                return;
                            }
                        }
                    } else if(!Inventory.isFull() && coalBag.getWidgetChild().interact("Withdraw-many")) {
                        Task.sleep(500, 1500);
                    }
                    if(Inventory.getCount(GlobalConstant.COAL_ORE) == 0)
                        return;
                }
                for(final Tile tile : GlobalConstant.BAD_DEPOSIT) {
                    if(tile.isOnMap()) {
                        final SceneObject obj = SceneEntities.getAt(tile);
                        if(obj == null)
                            GlobalConstant.BAD_DEPOSIT.remove(tile);
                    }
                }
                final PauseHandler.Condition condition = new PauseHandler.Condition() {
                    @Override
                    public boolean validate() {
                        return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 6;
                    }
                };
                final SceneObject bank = SceneEntities.getNearest(new Filter<SceneObject>() {
                    @Override
                    public boolean accept(final SceneObject obj) {
                        if(!GlobalConstant.BAD_DEPOSIT.contains(obj.getLocation())
                                && !Players.getLocal().isInCombat())
                            for(final int i : GlobalConstant.DEPOSIT_PLACED)
                                if(obj.getId() == i) {
                                    return NPCs.getNearest(new Filter<NPC>() {
                                        @Override
                                        public boolean accept(final NPC npc) {
                                            return npc.getInteracting() != null && npc.getName().contains("Living rock")
                                                    && npc.getInteracting().equals(Players.getLocal()) && Calculations.distance(npc, obj) < 5;
                                        }
                                    }) == null;
                                }
                        return obj.getId() == GlobalConstant.DEPOSIT_BANK;
                    }
                });
                if(bank != null) {
                    if(Calculations.distanceTo(bank) > 7) {
                        Traverse.walk(bank);
                        /*if(Calculations.distance(bank, GlobalConstant.TILE_BANK) < 10 &&
                                Players.getLocal().getLocation().getX() < bank.getLocation().getX()) {
                            if(Traverse.walk(bank.getLocation())) {
                                PauseHandler.pause(condition, (long) Random.nextInt(200, 500));
                            }
                        } else {
                            PauseHandler.walk(bank, (long) Random.nextInt(400, 800));
                        }*/
                    } else if(!bank.isOnScreen()) {
                        Camera.turnTo(bank);
                    } else if(bank.interact("Deposit")) {
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                final Widget deposit = Widgets.get(GlobalConstant.WIDGET_DEPOSIT);
                                if(deposit.validate()) {
                                    for(final WidgetChild wc : deposit.getChild(Widget_Items).getChildren()) {
                                        if(wc.validate() && wc.getChildId() != -1)
                                            return true;
                                    }
                                }
                                return false;
                            }
                        }, (long) Random.nextInt(1000, 2500));/*
                        PauseHandler.widget(GlobalConstant.WIDGET_DEPOSIT, (long) Random.nextInt(1000, 2500));
                        Task.sleep(200, 750);*/
                    }
                } else {
                    if(Traverse.walk(GlobalConstant.TILE_BANK)) {
                        PauseHandler.pause(condition, (long) Random.nextInt(200, 500));
                    }
                }
            }
        }
    }

    public static boolean isDepositOpen() {
        return Widgets.get(GlobalConstant.WIDGET_DEPOSIT).validate();
    }
}
