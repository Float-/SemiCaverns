package semicaverns.util;

import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;

import java.util.ArrayList;
import java.util.List;

public class GlobalConstant {

    public static boolean MINE_GOLD = true, BANK = false, KEEP_ALIVE = true, CAN_SUMMON = false;
    public static int WIELDED_ID = -1;

    public static List<Integer> CACHE = new ArrayList<>();
    public static final int OBELISK_ID = 5787, SPIN_TICKET = 24154, NATURE_RUNE = 561, LADDER_DOWN_ID = 30942, COAL_BAG = 18339;
    public static int[] FIRE_STAFF = {1387, 1393, 1401, 3053, 3054, 11736, 11738}, FIRE_RUNE = {554, 4694, 4699}, POUCHES = {12788, 12792};
    public static final int GOLD_ORE = 444, COAL_ORE = 453, GOLD_BAR = 2357, WIDGET_DEPOSIT = 11, WEAPON = 3, EXCALIBUR = 14632;
    public static final int[] GOOD_DEPOSIT = {-1, GOLD_ORE, GOLD_ORE + 1, COAL_ORE, COAL_ORE + 1, 15263, GOLD_BAR, //+1 for noted
            /*SUMMONING*/229, 15263, 12146, 12144, 12142, 12140/*//*/, 19996};
    public static final int DEPOSIT_BANK = 45079, DEPOSIT_PLACED[] = {75932, 10441, 2428}, ROPE_UP_ID = 45078, ROPE_DOWN_ID = 45077;
    public static final int[] JUJU_POTIONS = {20006, 20005, 20004, 23136, 23135, 23134, 23133, 23132};
    public static boolean JUJU_ACTIVE = false;
    public static final Area CAVERN_AREA = new Area(new Tile(3600, 5000, 0), new Tile(3700, 5200, 0));

    public static final Tile TILE_BANK = new Tile(3652, 5114, 0), GOLD_SAFE_SPOT = new Tile(3669, 5076, 0), TILE_LADDER = new Tile(3019, 3450, 0),
            COAL_SAFE_SPOT = new Tile(3689, 5106, 0), TILE_OBELISK = new Tile(3032, 9823, 0), TILE_ROPE = new Tile(3013, 9831, 0);
    public static final int[] ROCK_IDS = {/*GOLD*/45076, /*COAL*/5999}, LRC_NPC = {8832, 8833};
    public static final Tile[][] TILE_ROCKS = {{new Tile(3668, 5076, 0), new Tile(3638, 5095, 0)},
            {new Tile(3665, 5091, 0), new Tile(3675, 5099, 0), new Tile(3688, 5108, 0)}};

    public static final List<Tile> BAD_DEPOSIT = new ArrayList<>(); //Used to store private deposit boxes, until they disappear

    public static final int DEATH_WALK_IDX = 0;
    public static final Tile[][] WALKING_NODES = {

            {new Tile(2951, 3368, 0), new Tile(2950, 3375, 0), new Tile(2953, 3381, 0), new Tile(2961, 3381, 0), new Tile(2965, 3389, 0),
                    new Tile(2965, 3396, 0), new Tile(2965, 3402, 0), new Tile(2967, 3409, 0), new Tile(2973, 3414, 0),
                    new Tile(2980, 3416, 0), new Tile(2985, 3420, 0), new Tile(2988, 3425, 0), new Tile(2996, 3431, 0),
                    new Tile(3003, 3431, 0), new Tile(3010, 3433, 0), new Tile(3015, 3438, 0), new Tile(3016, 3444, 0), new Tile(3016, 3450, 0)},

            {new Tile(3655, 5118, 0), new Tile(3659, 5113, 0), new Tile(3659, 5108, 0), new Tile(3659, 5104, 0),
                    new Tile(3659, 5099, 0), new Tile(3659, 5094, 0), new Tile(3659, 5090, 0), new Tile(3659, 5086, 0)},
            {new Tile(3664, 5114, 0), new Tile(3669, 5113, 0), new Tile(3673, 5109, 0), new Tile(3673, 5104, 0),
                    new Tile(3673, 5099, 0), new Tile(3669, 5093, 0), new Tile(3664, 5092, 0)},
            {new Tile(3664, 5084, 0), new Tile(3667, 5081, 0), new Tile(3671, 5080, 0), new Tile(3672, 5084, 0), new Tile(3671, 5089, 0)},
            {new Tile(3655, 5099, 0), new Tile(3649, 5096, 0), new Tile(3643, 5095, 0)},
            {new Tile(3655, 5091, 0), new Tile(3649, 5092, 0), new Tile(3643, 5092, 0)},
            {new Tile(3675, 5094, 0), new Tile(3679, 5098, 0), new Tile(3682, 5101, 0), new Tile(3684, 5105, 0), new Tile(3679, 5105, 0)}
    };

}