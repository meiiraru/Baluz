package baluz.world.entity;

import baluz.world.BaluzWorld;
import cinnamon.model.GeometryHelper;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.utils.Maths;
import cinnamon.utils.Resource;
import cinnamon.vr.XrHandTransform;
import cinnamon.vr.XrRenderer;
import cinnamon.world.collisions.CollisionResolver;
import cinnamon.world.collisions.CollisionResult;
import cinnamon.world.entity.Entity;
import cinnamon.world.entity.xr.XrGrabbable;
import cinnamon.world.entity.xr.XrHand;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.UUID;

public class Dart extends XrGrabbable {

    private static final Resource MODEL = new Resource("baluz", "models/dart/dart.obj");
    private static final int LIFETIME = 600; // in ticks
    private static final float SPEED = 0.15f;

    private int life = LIFETIME;
    private boolean flying, grounded;

    public Dart(UUID uuid) {
        super(uuid, MODEL);
    }

    @Override
    public void tick() {
        super.tick();

        if (flying) {
            life--;
            if (life < 0) {
                remove();
                return;
            }

            if (!grounded) {
                Vector3f vec = new Vector3f(motion);
                if (vec.lengthSquared() > 0f)
                    vec.normalize();

                this.rotateTo(Maths.dirToRot(vec));
            }
        }
    }

    @Override
    protected void applyForces() {
        if (flying && !grounded)
            this.motion.y -= world.gravity * 0.5f;
    }

    @Override
    protected void motionFallout() {
        //nope
    }

    @Override
    protected void collide(Entity entity) {
        super.collide(entity);

        if (!isRemoved() && flying && entity instanceof Balloon balloon) {
            balloon.pop();
            //remove();
        }
    }

    @Override
    protected void resolveCollision(CollisionResult collision, Vector3f motion, Vector3f move) {
        CollisionResolver.stick(collision, motion, move);
        grounded = true;
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }

    @Override
    public void grab(XrHand hand) {
        flying = false;
        grounded = false;
        life = LIFETIME;
        super.grab(hand);
    }

    @Override
    public void release() {
        flying = true;
        this.setMotion(getMoveDir());
        super.release();
    }

    @Override
    public void renderDebugHitbox(MatrixStack matrices, float delta) {
        super.renderDebugHitbox(matrices, delta);
        Vector3f dir = getMoveDir();
        VertexConsumer.MAIN.consume(GeometryHelper.line(matrices, pos.x, pos.y, pos.z, pos.x + dir.x, pos.y + dir.y, pos.z + dir.z, 0.001f, 0xFFFF0000));
    }

    private Vector3f getMoveDir() {
        Vector3f vec = new Vector3f();
        if (getHand() != null) {
            XrHandTransform transform = XrRenderer.getHandTransform(getHand().getHand());
            vec.add(transform.vel());
            Vector2f rot = ((BaluzWorld) getWorld()).player.getRot();
            vec.rotateY((float) Math.toRadians(-rot.y));
            vec.rotateX((float) Math.toRadians(rot.x));
            vec.mul(SPEED);
        }
        return vec;
    }

    public boolean isFlying() {
        return flying;
    }
}
