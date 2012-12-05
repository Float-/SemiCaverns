package semicaverns.nodes.feature;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import semicaverns.util.PauseHandler;

public class WorldLogin extends Task {

    private static final int WIDGET_LOGIN = 906, SUB_FAV1_BUTTON = 191, SUB_FAV1_TEXT = 193, SUB_FAV2_BUTTON = 192,
            SUB_FAV2_TEXT = 194, SUB_WORLD_BUTTON = 215, SUB_LOGIN = 197;
    private static final int WIDGET_WORLDS = 910, SUB_WORLD_TEXT = 11, SUB_WORLD_LIST = 64, SUB_WORLD_ID = 69,
            SUB_SCROLL = 86, SUB_LIST_BOUNDS = 62;
    public static final int STATE_LOBBY = 7;

    @Override
    public void execute() {
        if(Game.getClientState() == STATE_LOBBY) {
            final Widget login = Widgets.get(WIDGET_LOGIN);
            if(login.validate()) {
                final WidgetChild fav1Child = login.getChild(SUB_FAV1_TEXT), fav2Child = login.getChild(SUB_FAV2_TEXT);
                if(fav1Child.validate() && fav2Child.validate()) {
                    final int buttonId = fav1Child.getText() != null && fav1Child.getText().equals("84") ? SUB_FAV1_BUTTON
                            : fav2Child.getText() != null && fav2Child.getText().equals("84") ? SUB_FAV2_BUTTON : -1;
                    if(buttonId != -1) {
                        final WidgetChild button = login.getChild(buttonId);
                        if(button.validate() && button.click(true)) {
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Game.isLoggedIn();
                                }
                            }, (long) Random.nextInt(5000, 10000));
                            return;
                        }
                    }
                }
            }
            final Widget worlds = Widgets.get(WIDGET_WORLDS);
            if(worlds.validate()) {
                final WidgetChild current = worlds.getChild(SUB_WORLD_TEXT);
                if(current.validate() && current.visible()) {
                    if(current.getText() != null && current.getText().contains("84")) {
                        final WidgetChild playButton = login.getChild(SUB_LOGIN);
                        if(playButton.validate() && playButton.click(true))
                            PauseHandler.pause(new PauseHandler.Condition() {
                                @Override
                                public boolean validate() {
                                    return Game.isLoggedIn();
                                }
                            }, (long) Random.nextInt(5000, 10000));
                    } else {
                        final WidgetChild list = worlds.getChild(SUB_WORLD_ID);
                        if(list.validate()) {
                            final WidgetChild[] children = list.getChildren();
                            for(int i = 0; i < children.length; i++)
                                if(children[i].validate() && children[i].getText() != null
                                        && children[i].getText().contains("84")) {
                                    final WidgetChild worldLine = worlds.getChild(SUB_WORLD_LIST).getChild(i),
                                            listBounds = worlds.getChild(SUB_LIST_BOUNDS);
                                    if(worldLine.validate() && listBounds.validate()) {
                                        if(listBounds.getBoundingRectangle().contains(worldLine.getBoundingRectangle())) {
                                            if(worldLine.click(true))
                                                Task.sleep(500, 1500);
                                        } else if(Widgets.scroll(worldLine, worlds.getChild(SUB_SCROLL)))
                                            Task.sleep(750, 1500);
                                    }
                                    break;
                                }
                        }
                    }
                } else if(login.validate()) {
                    final WidgetChild worldButton = login.getChild(SUB_WORLD_BUTTON);
                    if(worldButton.validate() && worldButton.click(true))
                        PauseHandler.pause(new PauseHandler.Condition() {
                            @Override
                            public boolean validate() {
                                return current.validate() && current.visible();
                            }
                        }, (long) Random.nextInt(750, 1750));
                }
            }
        }
    }
}
