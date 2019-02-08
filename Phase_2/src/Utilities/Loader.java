package Utilities;

import Items.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

public class Loader {

    private static final HashMap<String, int[]> movementZones = new HashMap<>();
    private static final HashMap<AnimalType, HashMap<String, Number>> wildsData = new HashMap<>();
    private static final HashMap<AnimalType, HashMap<String, Number>> petsData = new HashMap<>();

    private static final HashMap<String, HashMap<Byte, int[]>> workshopsConfig = new HashMap<>(); // from continent
    public static final HashMap<Item.ItemType, HashMap<String, Number>> itemsConfig = new HashMap<>();

    private static final HashMap<String, JsonObject> continentsData = new HashMap<>();

    private static final HashSet<String> workshops = new HashSet<>();
    private static final HashMap<String, JsonObject> workshopsData = new HashMap<>();

    private static final HashMap<String, Float> productsDepotSize = new HashMap<>();
    private static final HashMap<String, Integer> productsBuyCost = new HashMap<>();
    private static final HashMap<String, Integer> productsSaleCost = new HashMap<>();

    private static final HashMap<AnimalType, Integer> animalsBuyCost = new HashMap<>();
    private static final HashMap<AnimalType, Float> animalsDepotSize = new HashMap<>();
    private static final HashMap<AnimalType, String> animalsClassName = new HashMap<>();


    private static final HashMap<String, Integer[]> elementsLevelUpgradeCost = new HashMap<>();
    private static final HashMap<String, Integer[]> elementsMaxLevelUpgradeCost = new HashMap<>();

    private static boolean isInitialized = false;

    static {
        initialize();
    }

    public static HashMap<AnimalType, HashMap<String, Number>> getWildsData() {
        return wildsData;
    }

    public static HashMap<AnimalType, HashMap<String, Number>> getPetsData() {
        return petsData;
    }

    public static void destruct(){
        elementsLevelUpgradeCost.clear();
        elementsMaxLevelUpgradeCost.clear();

        animalsClassName.clear();
        animalsDepotSize.clear();
        animalsBuyCost.clear();

        productsBuyCost.clear();
        productsDepotSize.clear();
        productsSaleCost.clear();

        workshopsData.clear();

        workshops.clear();

        itemsConfig.clear();
        workshopsConfig.clear();

        petsData.clear();
        wildsData.clear();

        movementZones.clear();
        isInitialized = false;
    }

    public static void initialize(){
        if(!isInitialized){
            isInitialized = true;

            Gson gson = new GsonBuilder().create();
            Reader reader = null;
            Type type = new TypeToken<HashMap<String, Float>>() {
            }.getType();

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/productsDepotSize.json"),
                        StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            productsDepotSize.putAll(gson.fromJson(reader, type));

            type = new TypeToken<HashMap<String, Integer>>() {
            }.getType();

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/productsSaleCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            productsSaleCost.putAll(gson.fromJson(reader, type));

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/productsBuyCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            productsBuyCost.putAll(gson.fromJson(reader, type));

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/animalsClassPath.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            for (String animalName : object.keySet()) {
                animalsClassName.put(AnimalType.getType(animalName), object.get(animalName).getAsString());
            }

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/animalsBuyCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            object = gson.fromJson(reader, JsonObject.class);
            for (String animalName : object.keySet()) {
                animalsBuyCost.put((AnimalType.getType(animalName)), object.get(animalName).getAsInt());
            }

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/animalsDepotSize.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            object = gson.fromJson(reader, JsonObject.class);
            for (String animalName : object.keySet()) {
                animalsDepotSize.put((AnimalType.getType(animalName)), object.get(animalName).getAsFloat());
            }

            type = new TypeToken<HashMap<String, Integer[]>>() {
            }.getType();

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/elementsLevelUpgradeCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            elementsLevelUpgradeCost.putAll(gson.fromJson(reader, type));

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "DefaultGameData/elementsMaxLevelUpgradeCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            elementsMaxLevelUpgradeCost.putAll(gson.fromJson(reader, type));
            type = new TypeToken<HashSet<String>>() {
            }.getType();

            reader = null;
            try {
                reader = new FileReader(new File("DefaultGameData/DefaultWorkshops/names.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            workshops.addAll(gson.fromJson(reader, type));

            type = new TypeToken<HashMap<Byte, int[]>>() {
            }.getType();
            reader = null;
            try {
                reader = new FileReader(new File("DefaultGameData/ContinentsConfig/WorkshopsPosition/Africa.json"));
                HashMap<Byte, int[]> data = gson.fromJson(reader, type);
                workshopsConfig.put("Africa", data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                reader = new FileReader(new File("DefaultGameData/MovementZones/zones.json"));
                type = new TypeToken<HashMap<String, int[]>>() {
                }.getType();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            movementZones.putAll(gson.fromJson(reader, type));
            try {
                reader = new BufferedReader(new FileReader(new File("src/WildsData.json")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            type = new TypeToken<HashMap<AnimalType, HashMap<String, Number>>>() {
            }.getType();

            wildsData.putAll(gson.fromJson(reader, type));
            try {
                reader = new BufferedReader(new FileReader(new File("src/PetsData.json")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            petsData.putAll(gson.fromJson(reader, type));
            ;
            type = new TypeToken<HashMap<Item.ItemType, HashMap<String, Number>>>() {
            }.getType();
            try {
                reader = new BufferedReader(new FileReader(new File("src/res/Items/Textures/Config/config.json")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            itemsConfig.putAll(gson.fromJson(reader, type));
        }
    }

    public static JsonObject loadContinentData(String continent) {
        if(continentsData.containsKey(continent)){
            return continentsData.get(continent);
        }
        try {
            Gson gson = new Gson();
            Reader reader = new BufferedReader(new FileReader(new File("DefaultGameData/ContinentsConfig/"+continent+".json")));
            JsonObject o = gson.fromJson(reader, JsonObject.class);
            continentsData.put(continent, o);
            return o;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject loadWorkshopData(String name, String continent) {
        String s = name;

        if (workshops.contains(name + continent)) {
            s += continent;
        } else if(!workshops.contains(name))
            return null;

        if(workshopsData.containsKey(s)){
            return workshopsData.get(s);
        }
        try {
            Gson gson = new Gson();
            Reader reader = new BufferedReader(new FileReader(new File("DefaultGameData/DefaultWorkshops/"+name+"/"+s+".json")));
            JsonObject o = gson.fromJson(reader, JsonObject.class);
            workshopsData.put(s, o);
            return o;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] getWorkshopConfig(String continent, byte position) {
        return workshopsConfig.getOrDefault(continent, null).getOrDefault(position, null);
    }

    public static int[] getMovementZone(String continent) {
        return movementZones.getOrDefault(continent, null);
    }

    public static float getAnimalDepotSize(AnimalType type) {
        return animalsDepotSize.getOrDefault(type, -1F);
    }

    public static byte getElementMaxMaxLevel(String element) {
        return (byte) (elementsMaxLevelUpgradeCost.get(element).length);
    }

    public static Integer getAnimalBuyCost(AnimalType type) {
        return animalsBuyCost.getOrDefault(type, Integer.MIN_VALUE);
    }

    public static Float getProductSize(String product) {
        return productsDepotSize.getOrDefault(product, null);
    }

    public static Integer getProductSaleCost(String product) {
        return productsSaleCost.getOrDefault(product, null);
    }

    public static Integer getProductBuyCost(String product) {
        return productsBuyCost.getOrDefault(product, Integer.MIN_VALUE);
    }

    public static int getElementLevelUpgradeCost(String element, int nextLevel) {
        Integer[] cost = elementsLevelUpgradeCost.get(element);
        if(nextLevel < cost.length) {
            return cost[nextLevel];
        }
        return -1;
    }

    public static int getElementMaxLevelUpgradeCost(String element, int nextLevel) {
        Integer[] cost = elementsMaxLevelUpgradeCost.get(element);
        if(nextLevel < cost.length) {
            return cost[nextLevel];
        }
        return -1;
    }

    public static String getAnimalClassPath(AnimalType animal) {
        return animalsClassName.getOrDefault(animal, null);
    }
}
