package baluz.world;

import baluz.world.entity.Balloon;
import baluz.world.entity.Dart;
import cinnamon.gui.Toast;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.text.Text;
import cinnamon.utils.AABB;
import cinnamon.utils.Resource;
import cinnamon.vr.XrManager;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.Spawner;
import cinnamon.world.entity.living.Player;
import cinnamon.world.entity.xr.XrHand;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.WorldClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class BaluzWorld extends WorldClient {

    private static final Resource LEVEL = new Resource("baluz", "levels/test.json");

    private final List<Terrain> terrain = new ArrayList<>();
    private final List<Wave> waves = new ArrayList<>();

    private int score = 0;
    private int currentWave = 0;

    private final XrHand[] hands = new XrHand[2];

    public BaluzWorld() {
        super();
        this.hud = new BaluzHUD();
        this.movement = new BaluzMovement();
    }

    @Override
    protected void tempLoad() {
        Toast.clear(Toast.ToastType.WORLD);
        skyBox.renderSun = false;

        player.updateMovementFlags(false, false, true);

        if (XrManager.isInXR()) {
            for (int i = 0; i < hands.length; i++) {
                hands[i] = new XrHand(UUID.randomUUID(), i);
                this.addEntity(hands[i]);
            }
        }

        Spawner<Dart> dart = new Spawner<>(UUID.randomUUID(), 5, () -> new Dart(UUID.randomUUID()), e -> e.isRemoved() || e.isFlying());
        dart.setPos(0.75f, 0.5f, 0f);
        this.addEntity(dart);

        Spawner<Dart> dart2 = new Spawner<>(UUID.randomUUID(), 5, () -> new Dart(UUID.randomUUID()), e -> e.isRemoved() || e.isFlying());
        dart2.setPos(0.25f, 0.5f, 0f);
        this.addEntity(dart2);

        waves.addAll(WorldLoader.loadWorld(LEVEL, this));
        loadWave(currentWave);
    }

    @Override
    public void tick() {
        super.tick();

        if (currentWave >= waves.size())
            return;

        Wave curr = waves.get(currentWave);
        if (curr.isComplete()) {
            int s = curr.getScoreReward();
            score += s;
            if (s > 0)
                Toast.addToast(Text.of("Wave complete! +" + s + "pts!")).type(Toast.ToastType.WORLD);
            else
                Toast.addToast(Text.of("Wave complete!")).type(Toast.ToastType.WORLD);
            currentWave++;
            if (currentWave < waves.size()) {
                loadWave(currentWave);
            } else {
                Toast.addToast(Text.of("All waves completed! Final score: " + score)).type(Toast.ToastType.WORLD);
            }
        }
    }

    @Override
    protected void renderWorld(Camera camera, MatrixStack matrices, float delta) {
        int renderedTerrain = 0;
        for (Terrain t : terrain) {
            if (t.shouldRender(camera)) {
                t.render(matrices, delta);
                renderedTerrain++;
            }
        }

        super.renderWorld(camera, matrices, delta);
        this.renderedTerrain = renderedTerrain;
    }

    @Override
    protected void renderTargetedBlock(Entity cameraEntity, MatrixStack matrices, float delta) {
        //super.renderTargetedBlock(cameraEntity, matrices, delta);
    }

    @Override
    public void givePlayerItems(Player player) {
        //super.givePlayerItems(player);
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    @Override
    public void keyPress(int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS && key == GLFW_KEY_P) {
            for (Entity e : entities.values()) {
                if (e instanceof Balloon balloon) {
                    balloon.pop();
                }
            }
        }

        super.keyPress(key, scancode, action, mods);
    }

    @Override
    public void xrTriggerPress(int button, float value, int hand, float lastValue) {
        if (hand < 2 && hands[hand] != null) {
            if (value > 0.5f && lastValue <= 0.5f) {
                hands[hand].grab();
            } else if (value <= 0.5f && lastValue > 0.5f) {
                hands[hand].release();
            }
            return;
        }

        super.xrTriggerPress(button, value, hand, lastValue);
    }

    @Override
    public void setTerrain(Terrain terrain, float x, float y, float z) {
        if (terrain == null)
            return;

        terrain.setPos(x, y, z);
        this.terrain.add(terrain);
        scheduledTicks.add(() -> terrain.onAdded(this));
    }

    @Override
    public List<Terrain> getTerrain(AABB region) {
        List<Terrain> list = new ArrayList<>();
        for (Terrain t : terrain)
            if (t.getAABB().intersects(region))
                list.add(t);
        return list;
    }

    private void loadWave(int i) {
        for (Balloon ballon : waves.get(i).getBallons())
            addEntity(ballon);
    }
}
