package semicaverns.util;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.bot.Context;
import semicaverns.paint.PaintHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PriceFetcher extends Task {

    @Override
    public void execute() {
        PaintHelper.GOLD_PRICE = getPrice(GlobalConstant.GOLD_ORE);
        Context.get().getScriptHandler().log.info("Loaded gold price: " + PaintHelper.GOLD_PRICE);
        PaintHelper.COAL_PRICE = getPrice(GlobalConstant.COAL_ORE);
        Context.get().getScriptHandler().log.info("Loaded coal price: " + PaintHelper.COAL_PRICE);
    }

    private int getPrice(final int id) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new URL("http://services.runescape.com/m=" +
                    "itemdb_rs/api/catalogue/detail.json?item=" + id).openStream()));
            final String sp = br.readLine().split("\"price\":")[1].split("\\}")[0];//.split("\\{")[2].split(":")[2];
            if(sp != null && sp.length() > 0)
                return Integer.parseInt(sp);
        } catch(final Exception e) {
            e.printStackTrace(System.err);
            Context.get().getScriptHandler().log.severe("Unable to get price for ID: " + id);
        } finally {
            if(br != null)
                try {
                    br.close();
                } catch(final IOException e) {
                    e.printStackTrace(System.err);
                }
        }
        return 0;
    }
}
