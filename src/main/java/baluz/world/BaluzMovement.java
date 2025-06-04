package baluz.world;

import cinnamon.input.Movement;

public class BaluzMovement extends Movement {

    @Override
    public void xrButtonPress(int button, boolean pressed, int hand) {
        //super.xrButtonPress(button, pressed, hand);
    }

    @Override
    public void xrJoystickMove(float x, float y, int hand, float lastX, float lastY) {
        if (hand == 0) hand = 1;
        super.xrJoystickMove(x, y, hand, lastX, lastY);
    }
}
