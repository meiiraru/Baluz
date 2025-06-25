package baluz.screens;

import baluz.world.BaluzWorld;
import cinnamon.gui.ParentedScreen;
import cinnamon.gui.Screen;
import cinnamon.gui.widgets.WidgetList;
import cinnamon.gui.widgets.types.Button;
import cinnamon.gui.widgets.types.Label;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.utils.IOUtils;
import cinnamon.utils.Resource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static baluz.Main.LOGGER;

public class WorldPicker extends ParentedScreen {

    private static final Resource LEVEL_PATH = new Resource("baluz", "levels/");

    private final Map<String, Resource> detectedLevels = new HashMap<>();

    public WorldPicker(Screen parentScreen) {
        super(parentScreen);
        detectLevels();
    }

    private void detectLevels() {
        try {
            Path levels = IOUtils.ROOT_FOLDER.resolve("levels");
            if (!levels.toFile().exists())
                Files.createDirectory(levels);

            File[] files = levels.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    Path path = file.toPath();

                    if (!file.isDirectory() && path.toString().endsWith(".json")) {
                        Resource resource = new Resource("", path.toString().replaceAll("\\\\", "/"));
                        String name = path.getFileName().toString();
                        name = name.substring(0, name.length() - ".json".length());

                        detectedLevels.put(name, resource);
                        LOGGER.debug("Detected level %s", name);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to detect levels", e);
        }
    }

    @Override
    public void init() {
        Label title = new Label(width / 2, 4, Text.of("Select a Level"), Alignment.CENTER);
        addWidget(title);

        WidgetList list = new WidgetList(width / 2, height / 2, width - 8, height - 8 - 4, 4);
        list.setAlignment(Alignment.CENTER);

        //internal levels
        for (String string : IOUtils.listResources(LEVEL_PATH, false)) {
            Button button = new Button(0, 0, 100, 20, Text.of(string.substring(0, string.length() - ".json".length())), b -> new BaluzWorld(LEVEL_PATH.resolve(string)).init());
            list.addWidget(button);
        }

        //external levels
        for (Map.Entry<String, Resource> entry : detectedLevels.entrySet()) {
            Button button = new Button(0, 0, 100, 20, Text.of("(ext) " + entry.getKey()), b -> new BaluzWorld(entry.getValue()).init());
            list.addWidget(button);
        }

        addWidget(list);

        //back button
        super.init();
    }
}
