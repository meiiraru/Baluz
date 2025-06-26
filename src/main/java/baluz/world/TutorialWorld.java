package baluz.world;

import baluz.world.entity.ActionBalloon;
import cinnamon.Client;
import cinnamon.gui.Toast;
import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Colors;
import cinnamon.utils.Resource;
import cinnamon.world.terrain.Terrain;

import java.util.UUID;

public class TutorialWorld extends BaluzWorld {

    private static final Resource
            FLOOR = new Resource("baluz", "models/terrain/floor/model.obj"),
            SCORE = new Resource("baluz", "models/terrain/score/model.obj");

    public TutorialWorld() {
        super(null);
    }

    @Override
    protected void initLevel() {
        Toast.clear(Toast.ToastType.WORLD);

        //load dummy floor and score
        Terrain t = new Terrain(FLOOR, TerrainRegistry.BARRIER);
        setTerrain(t, 0, 0, 0);

        Terrain s = new Terrain(SCORE, TerrainRegistry.BARRIER);
        setTerrain(s, 0, 0, -30);

        //text pos
        textTransform.reset();
        textTransform.setPosPivot(0, 5.5f, -29.25f);
        textTransform.setRot(0, 180, 0);
        textTransform.setScale(8);

        //tutorial balloons
        ActionBalloon exit = new ActionBalloon(UUID.randomUUID(), Colors.randomRainbow().rgb, "And thats it!\nGood luck!", this::close);
        exit.setPos(2, 0f, -2f);

        ActionBalloon score = new ActionBalloon(UUID.randomUUID(), Colors.randomRainbow().rgb, "On the back panel, it is shown:\n\u2022 The remaining time\n\u2022 Your score\n\u2022 And the remaining balloons to pop", () -> {
            this.remaningBalloons = 0;
            this.score = 9999;
            addEntity(exit);
        });
        score.setPos(-2, 0f, -2f);

        ActionBalloon start = new ActionBalloon(UUID.randomUUID(), Colors.randomRainbow().rgb, "Use the trigger button while moving your hand\nto throw a dart\n\nPop this balloon to continue", () -> {
            this.score = 32;
            this.time = 72 * Client.TPS;
            this.remaningBalloons = 3;
            addEntity(score);
        });
        start.setPos(0.5f, 0f, -2f);
        addEntity(start);
    }

    @Override
    public void addScore(int score) {
        //super.addScore(score);
    }
}
