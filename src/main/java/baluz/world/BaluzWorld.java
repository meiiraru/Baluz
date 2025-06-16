package baluz.world;

import baluz.world.entity.ActionBalloon;
import baluz.world.entity.Balloon;
import baluz.world.entity.Dart;
import cinnamon.Client;
import cinnamon.gui.Toast;
import cinnamon.model.GeometryHelper;
import cinnamon.model.Transform;
import cinnamon.render.Camera;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Style;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.utils.Colors;
import cinnamon.utils.Resource;
import cinnamon.utils.TextUtils;
import cinnamon.vr.XrManager;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.Spawner;
import cinnamon.world.entity.living.Player;
import cinnamon.world.entity.xr.XrHand;
import cinnamon.world.world.WorldClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.lwjgl.glfw.GLFW.*;

public class BaluzWorld extends WorldClient {

    private static final Resource[] SCORE_ICONS = {
            new Resource("baluz", "textures/icons/time.png"),
            new Resource("baluz", "textures/icons/score.png"),
            new Resource("baluz", "textures/icons/balloon.png"),
    };

    private final List<Wave> waves = new ArrayList<>();
    public final Transform textTransform = new Transform();

    private final XrHand[] hands = new XrHand[2];

    private final Resource level;

    private int score = 0;
    private int currentWave = 0;
    private int time = 0;
    private int remaningBalloons = 0;

    public int initialTime = Integer.MAX_VALUE;
    public float timeBonusMul = 1f;

    private int prepare = 0;

    public BaluzWorld(Resource level) {
        super();
        this.level = level;
        this.hud = new BaluzHUD();
        this.movement = new BaluzMovement();
    }

    @Override
    protected void tempLoad() {
        player.updateMovementFlags(false, false, true);
        player.getAbilities().canBuild(false);

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

        initLevel();
    }

    private void initLevel() {
        Toast.clear(Toast.ToastType.WORLD);

        popAllBalloons();

        terrainManager.clear();

        waves.clear();
        waves.addAll(WorldLoader.loadWorld(level, this));

        score = 0;
        currentWave = 0;
        time = 0;
        remaningBalloons = 0;
        prepare = Client.TPS * 4 + 1;
    }

    private void endLevel() {
        Colors color = Colors.randomRainbow();
        ActionBalloon exit = new ActionBalloon(UUID.randomUUID(), color.rgb, "Exit", this::close);
        exit.setPos(-0.5f, 0f, -2f);

        while (true) {
            Colors newColor = Colors.randomRainbow();
            if (newColor != color) {
                color = newColor;
                break;
            }
        }
        ActionBalloon retry = new ActionBalloon(UUID.randomUUID(), color.rgb, "Retry", this::initLevel);
        retry.setPos(1.5f, 0f, -2f);

        addEntity(exit);
        addEntity(retry);
    }

    @Override
    public void tick() {
        super.tick();

        if (prepare > 0) {
            prepare--;
            switch (prepare) {
                case Client.TPS * 4 -> Toast.addToast("Get ready!").type(Toast.ToastType.WORLD).length(Client.TPS);
                case Client.TPS * 3 -> Toast.addToast("3...").type(Toast.ToastType.WORLD).length(Client.TPS);
                case Client.TPS * 2 -> Toast.addToast("2...").type(Toast.ToastType.WORLD).length(Client.TPS);
                case Client.TPS     -> Toast.addToast("1...").type(Toast.ToastType.WORLD).length(Client.TPS);
                case 0 -> {
                    Toast.addToast("Go!").type(Toast.ToastType.WORLD);
                    time = initialTime;
                    loadWave(currentWave);
                }
            }
            return;
        }

        if (currentWave >= waves.size())
            return;

        if (time > 0) {
            time--;

            if (time <= 0) {
                Toast.addToast("Time's up! Final score: " + score).type(Toast.ToastType.WORLD).length(200);
                time = 0;
                currentWave = waves.size();
                int score = this.score;
                int balloons = remaningBalloons;
                popAllBalloons();
                this.score = score;
                this.remaningBalloons = balloons;
                endLevel();
                return;
            }
        }

        Wave curr = waves.get(currentWave);
        if (curr.isComplete()) {
            int s = curr.getScoreReward();
            score += s;
            time += curr.getTimeReward();
            if (s > 0)
                Toast.addToast("Wave complete! +" + s + "pts!").type(Toast.ToastType.WORLD);
            else
                Toast.addToast("Wave complete!").type(Toast.ToastType.WORLD);
            currentWave++;
            if (currentWave < waves.size()) {
                loadWave(currentWave);
            } else {
                int bonus = (int) (time * timeBonusMul);
                score += bonus;
                Toast.addToast("All waves completed! Final score: " + score).type(Toast.ToastType.WORLD).length(200);
                if (bonus > 0)
                    Toast.addToast("Time bonus: +" + bonus + "pts!").type(Toast.ToastType.WORLD).length(200);
                endLevel();
            }
        }
    }

    @Override
    protected void renderWorld(Camera camera, MatrixStack matrices, float delta) {
        super.renderWorld(camera, matrices, delta);
        renderScore(matrices, delta);
    }

    private void renderScore(MatrixStack matrices, float delta) {
        //format the time as mm:ss.S
        float seconds = (float) time / Client.TPS;
        String time = String.format("%02d:%02d.%d", (int) seconds / 60, (int) seconds % 60, (int) (seconds * 10) % 10);

        //create the text
        Text text = Text.empty().append(Text.of(time).withStyle(Style.EMPTY.color(this.time < 100 ? Colors.RED : Colors.GREEN)))
                .append("\n")
                .append(Text.of(score).withStyle(Style.EMPTY.color(Colors.BLUE)))
                .append("\n")
                .append(Text.of(remaningBalloons).withStyle(Style.EMPTY.color(Colors.PINK)));

        matrices.pushMatrix();
        matrices.translate(0.5f, 0f, 0f);

        textTransform.applyTransform(matrices);

        matrices.scale(-1 / 48f);
        int textWidth = TextUtils.getWidth(text);
        matrices.translate((textWidth + 10) / 2f, 0f, 0f);


        //render
        text.render(VertexConsumer.WORLD_FONT, matrices, 0, 0, Alignment.CENTER_RIGHT);

        //icons
        matrices.translate(-textWidth, (-TextUtils.getHeight(text) - 1) / 2f, 0f);
        for (int i = 0; i < SCORE_ICONS.length; i++)
            VertexConsumer.WORLD_MAIN.consume(GeometryHelper.quad(matrices, -8 - 2, 8 * i, 8, 8), SCORE_ICONS[i]);

        matrices.popMatrix();
    }

    @Override
    protected void renderTargetedBlock(Entity cameraEntity, MatrixStack matrices, float delta) {
        //super.renderTargetedBlock(cameraEntity, matrices, delta);
    }

    @Override
    public void givePlayerItems(Player player) {
        //super.givePlayerItems(player);
    }

    public void addScore(int score) {
        this.score += score;
        remaningBalloons--;
    }

    @Override
    public void keyPress(int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            switch (key) {
                case GLFW_KEY_R -> {
                    initLevel();
                    return;
                }
                case GLFW_KEY_F -> {
                    popClosestBalloon();
                    return;
                }
                case GLFW_KEY_P -> {
                    popAllBalloons();
                    return;
                }
            }
        }
        super.keyPress(key, scancode, action, mods);
    }

    private void popClosestBalloon() {
        Balloon closest = null;
        float closestDist = Float.MAX_VALUE;

        for (Entity e : entities.values()) {
            if (!(e instanceof Balloon balloon) || balloon.isRemoved())
                continue;

            float dist = balloon.getPos().distanceSquared(player.getPos());
            if (dist < closestDist) {
                closestDist = dist;
                closest = balloon;
            }
        }

        if (closest != null)
            closest.pop();
    }

    private void popAllBalloons() {
        for (Entity e : entities.values()) {
            if (e instanceof ActionBalloon)
                e.remove();
            else if (e instanceof Balloon balloon)
                balloon.pop();
        }
    }

    @Override
    public void xrTriggerPress(int button, float value, int hand, float lastValue) {
        if (hand < hands.length && hands[hand] != null) {
            if (value > 0.5f && lastValue <= 0.5f) {
                hands[hand].grab();
            } else if (value <= 0.5f && lastValue > 0.5f) {
                hands[hand].release();
            }
            return;
        }

        super.xrTriggerPress(button, value, hand, lastValue);
    }

    private void loadWave(int i) {
        for (Balloon ballon : waves.get(i).getBallons())
            addEntity(ballon);
        remaningBalloons = waves.get(i).getBallons().size();
    }
}
