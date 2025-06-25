package baluz.screens;

import baluz.world.TutorialWorld;
import cinnamon.gui.Screen;
import cinnamon.gui.widgets.WidgetList;
import cinnamon.gui.widgets.types.Button;
import cinnamon.model.GeometryHelper;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.utils.Resource;

public class MainMenu extends Screen {

    private static final Resource LOGO = new Resource("baluz", "textures/logo.png");

    @Override
    public void init() {
        WidgetList list = new WidgetList(width / 2, height / 2, width, height, 12);
        list.setAlignment(Alignment.CENTER);

        list.addWidget(new Button(0, 0, 100, 20, Text.of("Play"), (button) -> client.setScreen(new WorldPicker(this))));
        list.addWidget(new Button(0, 0, 100, 20, Text.of("Tutorial"), (button) -> new TutorialWorld().init()));
        list.addWidget(new Button(0, 0, 100, 20, Text.of("Exit"), (button) -> client.window.exit()));

        addWidget(list);

        super.init();
    }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta) {
        renderSolidBackground(0xFFFFFFAA);
        VertexConsumer.MAIN.consume(GeometryHelper.quad(matrices, width / 2f - 105, height / 2f - 35 - 100, 210, 70), LOGO);
    }
}
