package baluz;

import baluz.screens.MainMenu;
import cinnamon.Cinnamon;
import cinnamon.Client;
import cinnamon.logger.Logger;
import cinnamon.utils.Resource;

public class Main {

    public static final Logger LOGGER = new Logger("baluz");

    public static void main(String[] args) {
        Cinnamon.TITLE = "Baluz";
        Cinnamon.NAMESPACE = "baluz";
        Cinnamon.ICON = new Resource("baluz", "textures/icon.png");
        Cinnamon.ENABLE_XR = true;
        Client.getInstance().mainScreen = MainMenu::new;
        new Cinnamon().run();
    }
}
