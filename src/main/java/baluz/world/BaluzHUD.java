package baluz.world;

import cinnamon.render.MatrixStack;
import cinnamon.world.Hud;
import cinnamon.world.entity.living.Player;
import cinnamon.world.items.Item;

public class BaluzHUD extends Hud {

    @Override
    protected void drawHotbar(MatrixStack matrices, Player player, float delta) {
        //super.drawHotbar(matrices, player, delta);
    }

    @Override
    protected void drawHealth(MatrixStack matrices, Player player, float delta) {
        //super.drawHealth(matrices, player, delta);
    }

    @Override
    protected void drawItemStats(MatrixStack matrices, Item item, float delta) {
        //super.drawItemStats(matrices, item, delta);
    }

    @Override
    protected void drawSelectedTerrain(MatrixStack matrices, float delta) {
        //super.drawSelectedTerrain(matrices, delta);
    }
}
