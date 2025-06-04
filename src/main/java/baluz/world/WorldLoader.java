package baluz.world;

import baluz.Main;
import baluz.world.entity.Balloon;
import cinnamon.Client;
import cinnamon.model.material.Material;
import cinnamon.registry.MaterialRegistry;
import cinnamon.registry.TerrainRegistry;
import cinnamon.utils.Colors;
import cinnamon.utils.IOUtils;
import cinnamon.utils.Resource;
import cinnamon.world.entity.Spawner;
import cinnamon.world.terrain.Terrain;
import cinnamon.world.world.World;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.util.UUID;

public class WorldLoader {

    public static void loadWorld(Resource level, World world) {
        try {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(IOUtils.getResource(level))).getAsJsonObject();

            //terrain
            JsonArray terrain = json.getAsJsonArray("terrain");
            for (JsonElement jsonElement : terrain) {
                JsonObject terrainObj = jsonElement.getAsJsonObject();

                Resource model = new Resource(terrainObj.get("path").getAsString());
                Vector3f pos = parseVec3(terrainObj.getAsJsonArray("pos"));

                world.setTerrain(new Terrain(model, TerrainRegistry.BARRIER), pos.x, pos.y, pos.z);
            }

            //balloons
            JsonArray balloons = json.getAsJsonArray("balloons");
            for (JsonElement balloon : balloons) {
                JsonObject balloonObj = balloon.getAsJsonObject();

                int delay = (int) (balloonObj.get("delay").getAsFloat() * Client.TPS);
                int color = balloonObj.has("color") ? Integer.parseInt(balloonObj.get("color").getAsString(), 16) : Colors.randomRainbow().rgb;
                Vector3f pos = parseVec3(balloonObj.getAsJsonArray("pos"));
                Material material = balloonObj.has("material") ? MaterialRegistry.valueOf(balloonObj.get("material").getAsString()).material : null;

                Spawner<Balloon> balloonSpawner = new Spawner<>(UUID.randomUUID(), delay, () -> new Balloon(UUID.randomUUID(), color, material));
                balloonSpawner.setRenderCooldown(false);
                balloonSpawner.setPos(pos.x, pos.y, pos.z);
                world.addEntity(balloonSpawner);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Failed to load world: %s", level, e);
        }
    }

    private static Vector3f parseVec3(JsonArray arr) {
        return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
    }
}
