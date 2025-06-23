package baluz.world.entity;

import baluz.world.BaluzWorld;
import cinnamon.model.ModelManager;
import cinnamon.model.material.Material;
import cinnamon.registry.EntityRegistry;
import cinnamon.render.MatrixStack;
import cinnamon.render.model.ModelRenderer;
import cinnamon.render.shader.Shader;
import cinnamon.sound.SoundCategory;
import cinnamon.utils.Colors;
import cinnamon.utils.Maths;
import cinnamon.utils.Resource;
import cinnamon.world.entity.PhysEntity;
import cinnamon.world.particle.DustParticle;

import java.util.UUID;

public class Balloon extends PhysEntity {

    private static final Resource
            MODEL_TOP = new Resource("baluz", "models/balloon/balloon.obj"),
            MODEL_STRING = new Resource("baluz", "models/balloon/string.obj"),
            POP_SOUND = new Resource("baluz", "sounds/pop.ogg");

    private final ModelRenderer stringModel;
    private final Material material;
    private final int color;
    private final int score;

    private int time;

    public Balloon(UUID uuid, int color, Material material, int score) {
        super(uuid, MODEL_TOP);
        this.stringModel = ModelManager.load(MODEL_STRING);
        this.color = color;
        this.material = material;
        this.score = score;
        this.time = (int) (Math.random() * 1000);
    }

    @Override
    public void tick() {
        super.tick();
        this.rotateTo(0, ++time);
        float oSin = (float) Math.sin((time - 1) * 0.05f) * 0.15f;
        float sin = (float) Math.sin(time * 0.05f) * 0.15f;
        this.moveTo(pos.x, pos.y - oSin + sin, pos.z);
    }

    @Override
    protected void applyModelPose(MatrixStack matrices, float delta) {
        super.applyModelPose(matrices, delta);
    }

    @Override
    protected void renderModel(MatrixStack matrices, float delta) {
        Shader.activeShader.applyColor(color);
        model.render(matrices, material);
        Shader.activeShader.applyColor(0xFFFFFF);
        stringModel.render(matrices);
    }

    public void pop() {
        if (isRemoved())
            return;

        ((BaluzWorld) getWorld()).addScore(score);
        popParticles();
        popSound();
        remove();
    }

    protected void popParticles() {
        for (int i = 0; i < 20; i++) {
            DustParticle particle = new DustParticle((int) (Math.random() * 10) + 5, Colors.randomRainbow().rgba);
            particle.setPos(aabb.getCenter());
            particle.setMotion(Maths.rotToDir((float) Math.random() * 360, (float) Math.random() * 360).mul((float) Math.random() * 0.05f + 0.05f));
            particle.setScale(1.5f);
            world.addParticle(particle);
        }
    }

    protected void popSound() {
        if (!isSilent())
            world.playSound(POP_SOUND, SoundCategory.ENTITY, pos).pitch(Maths.range(0.8f, 1.2f)).volume(0.3f);
    }

    @Override
    protected void applyForces() {
        //super.applyForces();
    }

    @Override
    public EntityRegistry getType() {
        return EntityRegistry.UNKNOWN;
    }
}
