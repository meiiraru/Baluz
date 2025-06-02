package baluz.world.entity;

import baluz.world.BaluzWorld;
import cinnamon.Client;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.MatrixStack;
import cinnamon.utils.Colors;
import cinnamon.utils.Maths;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.particle.DustParticle;

import java.util.UUID;

public class Balloon extends PhysEntity {

    private static final Resource MODEL = new Resource("baluz", "models/balloon/balloon.obj");

    public Balloon(UUID uuid) {
        super(uuid, MODEL);
    }

    @Override
    public void tick() {
        super.tick();
        this.rotate(0, 1);
    }

    @Override
    protected void applyModelPose(MatrixStack matrices, float delta) {
        matrices.translate(0, (float) Math.sin((Client.getInstance().ticks + delta) * 0.05f) * 0.15f, 0);
        super.applyModelPose(matrices, delta);
    }

    public void pop() {
        if (isRemoved())
            return;

        ((BaluzWorld) getWorld()).addScore(1);

        for (int i = 0; i < 20; i++) {
            DustParticle particle = new DustParticle((int) (Math.random() * 10) + 5, Colors.randomRainbow().rgba);
            particle.setPos(aabb.getCenter());
            particle.setMotion(Maths.rotToDir((float) Math.random() * 360, (float) Math.random() * 360).mul((float) Math.random() * 0.05f + 0.05f));
            particle.setScale(1.5f);
            world.addParticle(particle);
        }

        remove();
    }

    @Override
    protected void applyForces() {
        //super.applyForces();
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();
        float h = aabb.getHeight();
        aabb.scale(1f, 0.5f, 1f);
        aabb.translate(0, h * 0.25f, 0);
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
