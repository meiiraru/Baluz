package baluz.world;

import cinnamon.Client;
import cinnamon.render.MatrixStack;
import cinnamon.render.Window;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.world.Hud;
import cinnamon.world.entity.living.Player;
import cinnamon.world.items.Item;

public class BaluzHUD extends Hud {

    @Override
    protected void drawPlayerStats(MatrixStack matrices, Player player, float delta) {
        super.drawPlayerStats(matrices, player, delta);
        renderScore(matrices, player, delta);
    }

    protected void renderScore(MatrixStack matrices, Player player, float delta) {
        BaluzWorld world = (BaluzWorld) player.getWorld();
        Text score = Text.of("Score: " + world.getScore());

        Window w = Client.getInstance().window;
        score.render(VertexConsumer.FONT, matrices, w.getGUIWidth() / 2f, 4, Alignment.TOP_CENTER);
    }


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
