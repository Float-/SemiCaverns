package semicaverns;

import org.powerbot.core.event.events.MessageEvent;
import org.powerbot.core.event.listeners.MessageListener;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Job;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.core.script.job.state.Tree;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.*;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import semicaverns.nodes.Banker;
import semicaverns.nodes.EvadeCombat;
import semicaverns.nodes.Mine;
import semicaverns.nodes.feature.Outside;
import semicaverns.nodes.feature.Summoning;
import semicaverns.nodes.feature.WorldLogin;
import semicaverns.nodes.internal.Checks;
import semicaverns.paint.ImageLoader;
import semicaverns.paint.PaintHelper;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;
import semicaverns.util.PriceFetcher;
import semicaverns.util.walking.Traverse;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

@Manifest(name = "SemiCaverns [Mine]", description = "Mines at LRC, Supports both Gold & Coal.", version = 1.5d, authors = "Float", website = "http://www.powerbot.org/community/topic/831767-semicaverns-lrc-gold-coal/")
public class SemiCaverns extends ActiveScript implements PaintListener, MessageListener, MouseListener, MouseMotionListener {

    private final Tree NodeTree = new Tree(new Node[]{new Banker(),new Outside(), new Summoning(), new EvadeCombat(), new Mine(), });
    private final Node Antiban = new semicaverns.nodes.feature.Antiban();
    private final Job Login = new WorldLogin();

    @Override
    public void onStart() {
        Context.setLoginWorld(84);
        if(Game.getClientState() >= WorldLogin.STATE_LOBBY) {
            if(Skills.getRealLevel(Skills.MINING) >= 77) {
                GlobalConstant.MINE_GOLD = Skills.getRealLevel(Skills.MINING) >= 80;
                if(Tabs.getCurrent().equals(Tabs.INVENTORY) || Tabs.INVENTORY.open()) {
                    if(Players.getLocal().getAppearance()[GlobalConstant.WEAPON] == GlobalConstant.EXCALIBUR
                            || Inventory.getItem(GlobalConstant.EXCALIBUR) != null) {
                        GlobalConstant.WIELDED_ID = Players.getLocal().getAppearance()[GlobalConstant.WEAPON];
                    }
                }
                for(final int id : Players.getLocal().getAppearance())
                    GlobalConstant.CACHE.add(id);
                GlobalConstant.CACHE.add(-1000);
                for(final Item item : Inventory.getItems(new Filter<Item>() {
                    @Override
                    public boolean accept(final Item item) {
                        return item.getId() != GlobalConstant.GOLD_ORE && item.getId() != GlobalConstant.COAL_ORE
                                && item.getId() != GlobalConstant.GOLD_BAR && item.getId() != 19996;
                    }
                })) {
                    if(item != null && item.getId() != -1 && !GlobalConstant.CACHE.contains(item.getId())) {
                        GlobalConstant.CACHE.add(item.getId());
                    }
                }
                PaintHelper.START_EXP = Skills.getExperience(Skills.MINING);
                PaintHelper.START_LEVEL = Skills.getRealLevel(Skills.MINING);
                GlobalConstant.CAN_SUMMON = Skills.getRealLevel(Skills.SUMMONING) >= 73
                        && Inventory.getItem(GlobalConstant.POUCHES) != null;
                Arrays.sort(GlobalConstant.GOOD_DEPOSIT);
                this.getContainer().submit(new ImageLoader());
                this.getContainer().submit(new PriceFetcher());
            } else {
                log.severe("You do not have the required level to run this script, minimum of 77 mining needed.");
                shutdown();
            }
        } else {
            log.severe("Please start logged in.");
            shutdown();
        }
    }

    @Override
    public int loop() {
        final Widget warning = Widgets.get(892);
        if(warning.validate()) {
            final WidgetChild proceed = warning.getChild(15);
            if(proceed.validate() && proceed.click(true))
                PauseHandler.pause(new PauseHandler.Condition() {
                    @Override
                    public boolean validate() {
                        return !Checks.isOutside();
                    }
                }, (long) Random.nextInt(1000, 2500));
        } else if(Game.isLoggedIn()) {
            final Node next = NodeTree.state();
            if(next != null) {
                NodeTree.set(next);
                this.getContainer().submit(next);
                next.join();
            }
            if(Antiban.activate() && Random.nextInt(0, 50) < 10)
                Antiban.execute();
        } else if(Game.getClientState() == WorldLogin.STATE_LOBBY) {
            this.getContainer().submit(Login);
        }
        return Random.nextInt(50, 200);
    }

    private Point getTilePoint(final Tile tile) {
        final double angle = -1 * Math.toRadians(Camera.getYaw());
        final Tile baseTile = Players.getLocal().getLocation();
        final int x = (tile.getX() - baseTile.getX()) * 4 - 2,
                y = (baseTile.getY() - tile.getY()) * 4 - 2;
        return new Point((int) Math.round(x * Math.cos(angle) + y * Math.sin(angle) + 628),
                (int) Math.round(y * Math.cos(angle) - x * Math.sin(angle) + 137));
    }

    @Override
    public void onRepaint(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        if(Traverse.TILES != null && (Walking.getDestination() != null || (Players.getLocal() != null && Players.getLocal().isMoving()))) {
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.RED);
            for(int i = 1; i < Traverse.TILES.length; i++) {
                final Point p1 = getTilePoint(Traverse.TILES[i - 1])//Calculations.worldToMap(Traverse.TILES[i - 1].getX(), Traverse.TILES[i - 1].getY()),
                        , p2 = getTilePoint(Traverse.TILES[i]);//Calculations.worldToMap(Traverse.TILES[i].getX(), Traverse.TILES[i].getY());
                g2d.setColor(new Color(26, 124, 126, 200));
                if(Calculations.isOnScreen(p1) && Calculations.isOnScreen(p2))
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                if(i % 5 == 0) {
                    g2d.setColor(new Color(91, 255, 15, 200));
                    g2d.fillOval(p2.x - 2, p2.y - 2, 4, 4);
                }
            }
        }
/*        for(int i = 0; i < GlobalConstant.WALKING_NODES.length; i++)
            for(int k = 0; k < GlobalConstant.WALKING_NODES[i].length; k++) {
                final Point p = Calculations.worldToMap(GlobalConstant.WALKING_NODES[i][k]
                        .getX(), GlobalConstant.WALKING_NODES[i][k].getY());
                if(Calculations.isOnScreen(p)) {
                    if(GlobalConstant.WALKING_NODES[i].length > k + 1) {
                        final Point p2 = Calculations.worldToMap(GlobalConstant.WALKING_NODES[i][k + 1]
                                .getX(), GlobalConstant.WALKING_NODES[i][k + 1].getY());
                        if(Calculations.isOnScreen(p2)) {
                            g2d.setColor(new Color(10, 200, 10, 175));
                            g2d.drawLine(p.x, p.y, p2.x, p2.y);
                        }
                    }
                    g2d.setColor(new Color(10, 10, 200, 175));
                    g2d.fillOval(p.x - 3, p.y - 3, 6, 6);
                }
            }*/
        if(ImageLoader.isLoaded()) {
/*            try {
                final AffineTransform at = g2d.getTransform();
                g2d.setColor(new Color(255, 0, 0));
                g2d.setTransform(AffineTransform.getRotateInstance(Math.toRadians(Camera.getYaw()), 626, 135));
                g2d.drawLine(626, 58, 626, 212);
                g2d.setTransform(at);
            } catch(Exception e) {
            }*/

            if(PaintHelper.TAB_STATE != 0 && Game.isLoggedIn())
                g2d.drawImage(ImageLoader.images[0], 243, 396, null);

            { //the body
                g2d.drawImage(ImageLoader.images[1], 432, 398, null);
                g2d.drawImage(ImageLoader.images[1], 462, 398, null);

                if(PaintHelper.TAB_STATE == 1)
                    g2d.drawImage(ImageLoader.images[2], 467, 402, null);
                if(PaintHelper.TAB_STATE == 2)
                    g2d.drawImage(ImageLoader.images[2], 437, 402, null);

                g2d.drawImage(ImageLoader.images[3], 440, 405, null);
                g2d.drawImage(ImageLoader.images[3], 470, 405, null);

                g2d.drawImage(ImageLoader.images[5], 444, 409, null);
                g2d.drawImage(ImageLoader.images[4], 475, 409, null);
            }
            if(Game.isLoggedIn())
                switch(PaintHelper.TAB_STATE) {
                    case 1:
                        final int expGained = Skills.getExperience(Skills.MINING) - PaintHelper.START_EXP,
                                levelsGained = Skills.getRealLevel(Skills.MINING) - PaintHelper.START_LEVEL;
                        g2d.drawImage(ImageLoader.images[6], 262, 438, null);

                        g2d.setFont(new Font("Arial", 0, 9));
                        g2d.setColor(new Color(204, 204, 204, 200));

                        g2d.drawString(PaintHelper.RUN_TIME.toElapsedString(), 282, 452);
                        g2d.drawString("EXP Gained: " + PaintHelper.format(expGained, true) + " (+" + PaintHelper.format(levelsGained, false) + ")", 268, 467);
                        g2d.drawString("P/H: " + PaintHelper.format(PaintHelper.getPerHour(expGained), true), 279, 478);
                        final double tnl = Skills.XP_TABLE[Skills.getRealLevel(Skills.MINING) + 1] - Skills.getExperience(Skills.MINING);
                        final double totalNeeded = Skills.XP_TABLE[Skills.getRealLevel(Skills.MINING) + 1]
                                - Skills.XP_TABLE[Skills.getRealLevel(Skills.MINING)];
                        g2d.drawString("TNL: " + (Skills.getRealLevel(Skills.MINING) == 99 ? "Maxed out!" : PaintHelper.format((int)
                                Math.round(tnl), true) + " (" + Math.round(100 - (tnl / totalNeeded) * 100d) + "%)"), 279, 489);

                        g2d.drawString("Banks: " + PaintHelper.format(PaintHelper.BANKS, false), 388, 452);
                        final int ores = PaintHelper.GOLD + PaintHelper.COAL;
                        g2d.drawString("Ores: " + PaintHelper.format(ores, true) + " (" + PaintHelper.format(
                                PaintHelper.getPerHour(ores), false) + " PH)", 388, 464);
                        final int profit = (PaintHelper.GOLD * PaintHelper.GOLD_PRICE) + (PaintHelper.COAL * PaintHelper.COAL_PRICE);
                        g2d.drawString("Profit: " + PaintHelper.format(profit, true), 388, 476);
                        g2d.drawString("P/H: " + PaintHelper.format(PaintHelper.getPerHour(profit), true), 395, 488);

                        g2d.setColor(new Color(69, 65, 70, 200));
                        g2d.drawLine(380, 448, 380, 487);
                        break;
                    case 2:
                        g2d.drawImage(ImageLoader.images[9], 272, 454, null);
                        g2d.drawImage(ImageLoader.images[9], 310, 454, null);
                        g2d.drawImage(ImageLoader.images[11], 284, 442, null);
                        g2d.drawImage(ImageLoader.images[7], 277, 460, null);
                        g2d.drawImage(ImageLoader.images[8], 314, 459, null);

                        g2d.drawImage(ImageLoader.images[12], PaintHelper.getSliderX(), 444, null);

                        g2d.drawImage(ImageLoader.images[13], 370, 452, null);
                        g2d.drawImage(ImageLoader.images[10], 403, 451, null);
                        g2d.drawImage(ImageLoader.images[10], 428, 470, null);

                        if(GlobalConstant.BANK)
                            g2d.drawImage(ImageLoader.images[14], 404, 453, null);
                        if(GlobalConstant.KEEP_ALIVE)
                            g2d.drawImage(ImageLoader.images[14], 429, 472, null);
                        break;
                }
        }
        PaintHelper.cursor(g2d, new Color(26, 124, 126, 220));
    }

    @Override
    public void messageReceived(final MessageEvent e) {
        final String msg = e.getMessage().toLowerCase();
        if(msg != null && e.getId() != 2) {
            if(msg.contains("spirits")) {
                GlobalConstant.JUJU_ACTIVE = true;
            } else if(msg.contains("wears off")) {
                GlobalConstant.JUJU_ACTIVE = false;
            } else if(msg.contains("prevents you from using this deposit box")) {
                final SceneObject bank = SceneEntities.getNearest(GlobalConstant.DEPOSIT_PLACED);
                if(bank != null && !GlobalConstant.BAD_DEPOSIT.contains(bank.getLocation()))
                    GlobalConstant.BAD_DEPOSIT.add(bank.getLocation());
            } else if(msg.contains("mine some gold")) {
                PaintHelper.GOLD++;
            } else if(msg.contains("mine some coal")) {
                PaintHelper.COAL++;
            } else if(msg.contains("mine two gold")) {
                PaintHelper.GOLD += 2;
            } else if(msg.contains("mine two pieces of coal")) {
                PaintHelper.COAL += 2;
            } else if(msg.contains("additional ore")) {
                if(GlobalConstant.MINE_GOLD)
                    PaintHelper.GOLD++;
                else PaintHelper.COAL++;
            } else if(msg.contains("coal bag is already")) {
                Banker.COAL_BAG_FULL = true;
            } else if(msg.contains("no coal in your bag")) {
                Banker.COAL_BAG_FULL = false;
            } else if(msg.contains("pouch is too big")) {
                Summoning.TIGHT_AREA = true;
            }
        }

    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if(PaintHelper.TAB_STATE == 2) {
            final Rectangle bounds = new Rectangle(PaintHelper.getSliderX(), 444, ImageLoader
                    .images[12].getWidth(), ImageLoader.images[12].getHeight());
            if(bounds.contains(e.getPoint()))
                PaintHelper.SLIDER_START = e.getPoint().x - PaintHelper.getSliderX();
            else {
                if(PaintHelper.SETTING_BANK.contains(e.getPoint()))
                    GlobalConstant.BANK = !GlobalConstant.BANK;
                if(PaintHelper.SETTING_ALIVE.contains(e.getPoint()))
                    GlobalConstant.KEEP_ALIVE = !GlobalConstant.KEEP_ALIVE;
            }
        }
        for(int i = 0; i < PaintHelper.TAB_BOUNDS.length; i++) {
            if(PaintHelper.TAB_BOUNDS[i].contains(e.getPoint())) {
                PaintHelper.TAB_STATE = PaintHelper.TAB_STATE == i + 1 ? 0 : i + 1;
                break;
            }
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        PaintHelper.MOUSE_POINT = e.getPoint();
        if(PaintHelper.SLIDER_START != -1) {
            final double x = PaintHelper.getSliderX();
            GlobalConstant.MINE_GOLD = Math.abs(x - PaintHelper.SLIDER_MIN_X) < Math.abs(x - PaintHelper.SLIDER_MAX_X);
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        PaintHelper.MOUSE_POINT = e.getPoint();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        PaintHelper.SLIDER_START = -1;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }
}
