package baluz.world.entity;

import java.util.UUID;

public class ActionBalloon extends Balloon {

    private final Runnable action;

    public ActionBalloon(UUID uuid, int color, String text, Runnable action) {
        super(uuid, color, null, 0);
        this.setName(text);
        this.action = action;
    }

    @Override
    public void pop() {
        super.pop();
        action.run();
    }
}
