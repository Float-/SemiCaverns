package semicaverns.nodes;

import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Magic;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import semicaverns.nodes.feature.Outside;
import semicaverns.nodes.internal.Checks;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;
import semicaverns.util.walking.Traverse;

import java.util.Arrays;

public class Mine extends Node {

    private long Timer = System.currentTimeMillis() - 15000l;
    private int LastMine = 0;

    @Override
    public boolean activate() {
        return !Checks.isOutside() && !Banker.isDepositOpen() && !Banker.COAL_BAG_FULL && (Tabs.getCurrent().equals(Tabs.MAGIC) || !Inventory.isFull()
                || (isSuperheatValid() && Inventory.getCount(GlobalConstant.GOLD_ORE) > 0))
                && (Players.getLocal().getInteracting() == null || Players.getLocal().getInteracting() instanceof Player);
    }

    @Override
    public void execute() {
        if(Outside.findGrave())
            return;
        if(GlobalConstant.WIELDED_ID != -1) {
            final int wieldedId = Players.getLocal().getAppearance()[GlobalConstant.WEAPON];
            if(Tabs.getCurrent().equals(Tabs.INVENTORY) && wieldedId == GlobalConstant.EXCALIBUR) {
                final Item item = Inventory.getItem(GlobalConstant.WIELDED_ID);
                if(item != null) {
                    item.getWidgetChild().interact("Wield");
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return Players.getLocal().getAppearance()[GlobalConstant.WEAPON] == item.getId();
                        }
                    }, (long) Random.nextInt(400, 900));
                }
            } else if(wieldedId != GlobalConstant.WIELDED_ID)
                GlobalConstant.WIELDED_ID = wieldedId;
        }
        if(GlobalConstant.MINE_GOLD && (Tabs.getCurrent().equals(Tabs.MAGIC) || (Tabs.getCurrent().equals(Tabs.INVENTORY)
                && (Players.getLocal().getAnimation() != -1 || Inventory.getCount(GlobalConstant.GOLD_ORE) > 0) && isSuperheatValid()
                && (Magic.isSpellSelected() || Inventory.getCount(GlobalConstant.GOLD_ORE) > 0)))) {
            switch(Tabs.getCurrent()) {
                case INVENTORY:
                    if(Magic.isSpellSelected()) {
                        final Item next = Inventory.getItem(GlobalConstant.GOLD_ORE);
                        if(next != null) {
                            next.getWidgetChild().interact("Cast");
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Tabs.getCurrent().equals(Tabs.MAGIC);
                                }
                            }, (long) Random.nextInt(800, 2000));
                        }
                    } else Tabs.MAGIC.open();
                    break;
                case MAGIC:
                    final WidgetChild superheat = Widgets.get(192, 50);
                    if(superheat.validate()) {
                        if(superheat.visible()) {
                            superheat.click(true);
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Tabs.getCurrent().equals(Tabs.INVENTORY);
                                }
                            }, (long) Random.nextInt(500, 1500));
                        } else {
                            final WidgetChild wc = Widgets.get(192, 14);
                            if(wc.validate()) {
                                wc.click(true);
                                Task.sleep(400, 900);
                            }
                        }
                    }
                    break;
            }
            return;
        }
        if(!GlobalConstant.JUJU_ACTIVE) {
            final Item pot = Inventory.getItem(GlobalConstant.JUJU_POTIONS);
            if(pot != null) {
                if(pot.getWidgetChild().click(true))
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return GlobalConstant.JUJU_ACTIVE;
                        }
                    }, (long) Random.nextInt(1250, 2000));
                return;
            }
        }
        final Tile[] rockTiles = GlobalConstant.TILE_ROCKS[Checks.isGold()];
        SceneObject rock = null;
        for(final Tile rockTile : rockTiles) {
            final SceneObject subject = SceneEntities.getAt(rockTile);
            if(subject != null && subject.getId() == GlobalConstant.ROCK_IDS[Checks.isGold()]) {
                if(rock == null || (Calculations.distanceTo(subject) < Calculations.distanceTo(rock)
                        && Players.getLoaded(new Filter<Player>() {
                    @Override
                    public boolean accept(final Player player) {
                        return Calculations.distance(subject, player) < 10;
                    }
                }).length > 10)) {
                    rock = subject;
                }
            }
        }
        final Item spinTicket = Inventory.getItem(GlobalConstant.SPIN_TICKET);
        if(spinTicket != null) {
            spinTicket.getWidgetChild().interact("Claim");
            Task.sleep(400, 800);
            return;
        }
        if(rock != null) {
            if(rock.isOnScreen() && Calculations.distanceTo(rock) < 5) {
                final Timer check = new Timer((long) Random.nextInt(800, 1500));
                while(Players.getLocal().getAnimation() == -1 && check.isRunning())
                    Task.sleep(30, 80);
                if(Players.getLocal().getAnimation() == -1 && rock.interact("Mine")) {
                    LastMine = getCurrent();
                    PauseHandler.pause(new PauseHandler.Condition() {
                        @Override
                        public boolean validate() {
                            return Players.getLocal().getAnimation() != -1;
                        }
                    }, (long) Random.nextInt(500, 1000));
                }
            } else {
                Traverse.walk(rock);//traverseToLocatable(rock/*.getArea().getNearest()*/);
            }
        } else {
            final Tile next = rockTiles[(LastMine + 1) % rockTiles.length];
            if(Calculations.distanceTo(next) > 5) {
                Traverse.walk(next);//traverseToLocatable(next);
                Timer = System.currentTimeMillis();
            } else {
                Task.sleep(100, 750);
                if(System.currentTimeMillis() - Timer > 15000l) {
                    final Player[] players = Players.getLoaded(new Filter<Player>() {
                        @Override
                        public boolean accept(final Player player) {
                            return Calculations.distanceTo(player) < 6 && !player.isMoving();
                        }
                    });
                    if(players.length < 25 || System.currentTimeMillis() - Timer > 120000l) //two minutes
                        LastMine = ++LastMine % rockTiles.length;
                }
            }
        }
    }

    private boolean isSuperheatValid() {
        return Inventory.getItem(GlobalConstant.NATURE_RUNE) != null && (Inventory.getItem(GlobalConstant.FIRE_RUNE) != null
                || Arrays.binarySearch(GlobalConstant.FIRE_STAFF, Players.getLocal().getAppearance()[GlobalConstant.WEAPON]) >= 0);
    }

/*    private void traverseToLocatable(final Locatable locatable) {
        if(true || Players.getLocal().getLocation().getY() > 5103 || !locatable.getLocation().isOnMap()) {
            Traverse.walk(locatable.getLocation());
        } else {
            PauseHandler.walk(locatable, (long) Random.nextInt(400, 800));
        }
        Task.sleep(400, 800);
        PauseHandler.pause(new PauseHandler.Condition() {
            @Override
            public boolean validate() {
                return Walking.getDestination() == null || Calculations.distanceTo(Walking.getDestination()) < 8;
            }
        }, (long) Random.nextInt(500, 1000));
    }*/

    public static int getCurrent() {
        final Tile[] rockTiles = GlobalConstant.TILE_ROCKS[Checks.isGold()];
        if(rockTiles.length > 0) {
            int index = 0;
            for(int i = 1; i < rockTiles.length; i++)
                if(Calculations.distanceTo(rockTiles[i]) < Calculations.distanceTo(rockTiles[index]))
                    index = i;
            return index;
        }
        return -1;
    }

}
