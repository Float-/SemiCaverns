package semicaverns.nodes.feature;

import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.Character;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.bot.Context;
import semicaverns.util.GlobalConstant;
import semicaverns.util.PauseHandler;

public class Antiban extends Node {

    @Override
    public boolean activate() {
        return !Players.getLocal().isInCombat();
    }

    @Override
    public void execute() {
        final Character inter = Players.getLocal().getInteracting();
        if(!Walking.isRunEnabled() && Walking.getEnergy() > Random.nextInt(10, 20))
            Walking.setRun(true);
        if(inter != null) {
            try {
                if(inter instanceof NPC || (inter instanceof Player && (inter
                        .isMoving() || Players.getLocal().isMoving()))) {
                    Tile closest = null;
                    for(final Tile[] t1 : GlobalConstant.WALKING_NODES)
                        for(final Tile t2 : t1) {
                            if(closest == null || Calculations.distanceTo(t2) < Calculations.distanceTo(closest))
                                closest = t2;
                        }
                    PauseHandler.walk(closest, (long) Random.nextInt(300, 900));
                }
            } catch(final NullPointerException e) {
                Context.get().getScriptHandler().log.severe("Internal error.");
            }
        } else {
            if(Camera.getPitch() < 80)
                Camera.setPitch(true);
            switch(Random.nextInt(0, 150)) {
                case 0:
                    Camera.setPitch(Random.nextInt(80, 100));
                    break;
                case 1:
                    Camera.setAngle(Random.nextInt(0, 360));
                    break;
                case 2:
                    final Player[] players = Players.getLoaded(new Filter<Player>() {
                        @Override
                        public boolean accept(final Player player) {
                            return Calculations.distanceTo(player) < 15;
                        }
                    });
                    if(players.length > 0) {
                        Camera.turnTo(players[Random.nextInt(0, players.length - 1)]);
                    }
                    break;
                case 3:
                    Mouse.move(Mouse.getX() + Random.nextInt(-100, 100), Mouse.getX() + Random.nextInt(-100, 100));
                    break;
                case 4:
                    Mouse.move(Mouse.getX() + Random.nextInt(-20, 20), Mouse.getY() + Random.nextInt(-20, 20));
                    break;
                case 5:
                    Task.sleep(400, 800);
                    break;
                case 6:
                    Task.sleep(800, 1800);
                    break;
            }
        }
    }
}
