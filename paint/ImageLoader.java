package semicaverns.paint;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.bot.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ImageLoader extends Task {

    /**
     * 00 - Main paint frame
     * 01 - Tab: Background
     * 02 - Tab: Blue, Pressed
     * 03 - Tab: Overlay
     * 04 - Tab: Paper
     * 05 - Tab: Question
     * 06 - (Info) Clock
     * 07 - (Settings) Gold
     * 08 - (Settings) Coal
     * 09 - (Settings) Box
     * 10 - (Settings) Checkbox
     * 11 - (Settings) Slider bkg
     * 12 - (Settings) Slider
     * 13 - (Settings) Checkbox Text
     * 14 - (Settings) Checkbox cross
     */
    public static final BufferedImage[] images = new BufferedImage[15];

    @Override
    public void execute() {
        final long timer = System.currentTimeMillis();
        try {
            final BufferedImage in = ImageIO.read(new URL("http://i50.tinypic.com/sybku9.png"));
            if(in != null) {
                final int[][] cutoffs = {{33, 0, 254, 110}, {0, 49, 34, 31}, {5, 21, 24, 23},
                        {8, 83, 20, 18}, {6, 6, 9, 11}, {20, 6, 11, 11}, {262, 110, 19, 19},
                        {8, 116, 25, 25}, {43, 117, 25, 25}, {74, 112, 33, 33}, {144, 126, 14, 14},
                        {112, 112, 45, 8}, {112, 123, 10, 5}, {163, 112, 54, 31}, {250, 114, 11, 10}};
                if(cutoffs.length == images.length) {
                    for(int i = 0; i < images.length; i++){
                        images[i] = in.getSubimage(cutoffs[i][0], cutoffs[i][1], cutoffs[i][2], cutoffs[i][3]);
                    }
                    Context.get().getScriptHandler().log.info("Images loaded: " + images.length +
                            ", Duration: " + (System.currentTimeMillis() - timer) / 1000d + " secs.");
                } else Context.get().getScriptHandler().log.severe("Internal coding error. Post this on the thread.");
            } else throw new Exception();
        } catch(final Exception e) {
            Context.get().getScriptHandler().log.severe("Unable to download images for paint! The paint will not be shown.");
        }
    }

    public static boolean isLoaded() {
        for(final BufferedImage bi : images)
            if(bi == null) {
                return false;
            }
        return true;
    }
}
