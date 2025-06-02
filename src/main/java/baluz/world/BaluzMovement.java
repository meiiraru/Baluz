package baluz.world;

import cinnamon.input.Movement;
import cinnamon.world.entity.Entity;

public class BaluzMovement extends Movement {

    private float snapRot;

    @Override
    public void tick(Entity target) {
        rotation.add(snapRot, 0);
        snapRot = 0;
        super.tick(target);
    }

    @Override
    public void xrButtonPress(int button, boolean pressed, int hand) {
        //super.xrButtonPress(button, pressed, hand);
    }

    @Override
    public void xrJoystickMove(float x, float y, int hand, float lastX, float lastY) {
        if (hand == 0) {
            float f = 0.9f;
            int dx = lastX < f && x >= f ? 1 : lastX > -f && x <= -f ? -1 : 0;
            if (dx != 0) snapRot = dx * 45f;
            return;
        }

        super.xrJoystickMove(x, y, hand, lastX, lastY);
    }
}
