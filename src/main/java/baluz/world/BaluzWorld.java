package baluz.world;

import baluz.world.entity.Balloon;
import baluz.world.entity.Dart;
import cinnamon.gui.Toast;
import cinnamon.registry.TerrainRegistry;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.utils.AABB;
import cinnamon.utils.Resource;
import cinnamon.vr.XrManager;
import cinnamon.vr.XrRenderer;
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

    private static final Resource FLOOR = new Resource("baluz", "models/terrain/model.obj");

    private final List<Terrain> terrain = new ArrayList<>();

    private int score = 0;
    private final XrHand[] hands = new XrHand[2];

    public BaluzWorld() {
        super();
        this.hud = new BaluzHUD();
        this.movement = new BaluzMovement();
    }

    @Override
    protected void tempLoad() {
        Toast.clear(Toast.ToastType.WORLD);

        player.updateMovementFlags(false, false, true);

        if (XrManager.isInXR()) {
            for (int i = 0; i < hands.length; i++) {
                hands[i] = new XrHand(UUID.randomUUID(), i);
                this.addEntity(hands[i]);
            }
        }

        Spawner<Balloon> balloon = new Spawner<>(UUID.randomUUID(), 10, () -> new Balloon(UUID.randomUUID()));
        balloon.setPos(0f, 0f, -2f);
        this.addEntity(balloon);

        Spawner<Dart> dart = new Spawner<>(UUID.randomUUID(), 5, () -> new Dart(UUID.randomUUID()), e -> e.isRemoved() || e.isFlying());
        dart.setPos(0.75f, 0.5f, 0f);
        this.addEntity(dart);

        setTerrain(new Terrain(FLOOR, TerrainRegistry.BARRIER), 0, 0, 0);
    }

    @Override
    protected void renderWorld(Camera camera, MatrixStack matrices, float delta) {
        if (client.screen == null)
            renderHands(camera, matrices);

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

    private void renderHands(Camera camera, MatrixStack matrices) {
        matrices.pushMatrix();
        matrices.translate(camera.getPos());
        matrices.rotate(camera.getRot());
        XrRenderer.renderHands(matrices);
        matrices.popMatrix();
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
        if (hand < 2 && button == 1) {
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
    public void setTerrain(Terrain terrain, int x, int y, int z) {
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
}
