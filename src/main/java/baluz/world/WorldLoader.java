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
import cinnamon.world.terrain.Terrain;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldLoader {

    public static List<Wave> loadWorld(Resource level, BaluzWorld world) {
        List<Wave> waveList = new ArrayList<>();

        try {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(IOUtils.getResource(level))).getAsJsonObject();

            //time
            world.initialTime = (int) (json.get("starting_time").getAsFloat() * Client.TPS);
            world.timeBonusMul = json.get("time_bonus_mul").getAsFloat();

            //text
            JsonObject text = json.getAsJsonObject("text");
            world.textTransform.setPos(parseVec3(text.getAsJsonArray("pos")));
            world.textTransform.setPivot(world.textTransform.getPos());
            world.textTransform.setRot(parseVec3(text.getAsJsonArray("rot")));
            world.textTransform.setScale(text.get("scale").getAsFloat());

            //skybox
            if (json.has("skybox"))
                world.getSky().setSkyBox(new Resource(json.get("skybox").getAsString()));

            //terrain
            JsonArray terrain = json.getAsJsonArray("terrain");
            for (JsonElement jsonElement : terrain) {
                JsonObject terrainObj = jsonElement.getAsJsonObject();

                Resource model = new Resource(terrainObj.get("path").getAsString());
                Vector3f pos = parseVec3(terrainObj.getAsJsonArray("pos"));

                Terrain t = new Terrain(model, TerrainRegistry.BARRIER);
                float rot = terrainObj.has("rot") ? terrainObj.get("rot").getAsFloat() : 0;
                t.setRotation((byte) (rot / 90 % 4));

                world.setTerrain(t, pos.x, pos.y, pos.z);
            }

            //waves
            JsonArray waves = json.getAsJsonArray("waves");
            for (JsonElement wave : waves) {
                JsonObject waveObj = wave.getAsJsonObject();
                Wave waveInstance = parseWave(waveObj);
                waveList.add(waveInstance);

                JsonArray balloons = waveObj.getAsJsonArray("balloons");
                for (JsonElement balloon : balloons) {
                    Balloon ballonInstance = parseBalloon(balloon);
                    waveInstance.getBallons().add(ballonInstance);
                }
            }
        } catch (Exception e) {
            Main.LOGGER.error("Failed to load world: %s", level, e);
        }

        return waveList;
    }

    private static Vector3f parseVec3(JsonArray arr) {
        return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
    }

    private static Wave parseWave(JsonObject waveObj) {
        //awards
        int time = 0;
        int score = 0;

        if (waveObj.has("awards")) {
            JsonObject awards = waveObj.getAsJsonObject("awards");
            if (awards.has("time"))
                time = (int) (awards.get("time").getAsFloat() * Client.TPS);
            if (awards.has("score"))
                score = awards.get("score").getAsInt();
        }

        return new Wave(time, score);
    }

    private static Balloon parseBalloon(JsonElement balloon) {
        JsonObject balloonObj = balloon.getAsJsonObject();

        Material material = balloonObj.has("material") ? MaterialRegistry.valueOf(balloonObj.get("material").getAsString()).material : null;
        int color = balloonObj.has("color") ? Integer.parseInt(balloonObj.get("color").getAsString(), 16) : material == null ? Colors.randomRainbow().rgb : Colors.WHITE.rgb;
        Vector3f pos = parseVec3(balloonObj.getAsJsonArray("pos"));
        int score = balloonObj.has("score") ? balloonObj.get("score").getAsInt() : 1;

        Balloon balloonInstance = new Balloon(UUID.randomUUID(), color, material, score);
        balloonInstance.setPos(pos);
        return balloonInstance;
    }
}
