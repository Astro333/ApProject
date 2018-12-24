package Utilities;

import Animals.Animal;
import Interfaces.Processable;
import Items.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    private static HashMap<String, Float> productsDepotSize;
    private static HashMap<String, Integer> productsBuyCost;
    private static HashMap<String, Integer> productsSaleCost;

    private static HashMap<String, String> animalsClassName;

    private static HashMap<String, Integer[]> elementsLevelUpgradeCost;
    private static HashMap<String, Integer[]> elementsMaxLevelUpgradeCost;

    private static HashMap<Byte, Pair<Integer, Integer>> workshopsDropZoneAfrica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsDropZoneAntarctica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsDropZoneSouthAmerica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsDropZonePrairie; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsDropZoneRussia; // from continent

    private static HashMap<Byte, Pair<Integer, Integer>> workshopsPositionAfrica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsPositionAntarctica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsPositionSouthAmerica; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsPositionPrairie; // from continent
    private static HashMap<Byte, Pair<Integer, Integer>> workshopsPositionRussia; // from continent

    private static HashMap<String, Processable> processableElements;

    static {
        processableElements = new HashMap<>();
        for(Item.ItemType itemType : Item.ItemType.values())
            processableElements.put(itemType.toString(), itemType);

        for(Animal.AnimalType animalType : Animal.AnimalType.values())
            processableElements.put(animalType.toString(), animalType);
    }

    private static boolean isInitiated = false;

    // MUST INITIATE THIS CLASS AT LOADING MENU
    public static boolean initiate() {
        if(!isInitiated){
            Gson gson = new GsonBuilder().create();
            Reader reader = null;
            Type type = new TypeToken<HashMap<String, Float>>(){}.getType();

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "../DefaultGameData/productsDepotSize.json"),
                        StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            productsDepotSize = gson.fromJson(reader, type);

            type = new TypeToken<HashMap<String, Integer>>(){}.getType();

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "../DefaultGameData/productsSaleCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            productsSaleCost = gson.fromJson(reader, type);

            try {
                reader = new InputStreamReader(new FileInputStream(
                        "../DefaultGameData/productsBuyCost.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            productsBuyCost = gson.fromJson(reader, type);



            isInitiated = true;
            return true;
        }
        return false;
    }

    public static Float getProductSize(String product){
        return productsDepotSize.getOrDefault(product, null);
    }
    public static Integer getProductSaleCost(String product){return productsBuyCost.getOrDefault(product, null);}
    public static Integer getProductBuyCost(String product){
        return productsBuyCost.getOrDefault(product, null);
    }
    public static Pair<Integer, Integer> getWorkshopDropZone(String continent, byte position){
        switch (continent){
            case "prairie":
                return workshopsDropZonePrairie.get(position);
            case "southAmerica":
                return workshopsDropZoneSouthAmerica.get(position);
            case "russia":
                return workshopsDropZoneRussia.get(position);
            case "antarctica":
                return workshopsDropZoneAntarctica.get(position);
            case "africa":
                return workshopsDropZoneAfrica.get(position);
            default:
                return null;
        }
    }
    public static Pair<Integer, Integer> getWorkshopPosition(String continent, byte position){
        switch (continent){
            case "prairie":
                return workshopsPositionPrairie.get(position);
            case "southAmerica":
                return workshopsPositionSouthAmerica.get(position);
            case "russia":
                return workshopsPositionRussia.get(position);
            case "antarctica":
                return workshopsPositionAntarctica.get(position);
            case "africa":
                return workshopsPositionAfrica.get(position);
            default:
                return null;
        }
    }
    public static int getElementLevelUpgradeCost(String element, int nextLevel){
        return elementsLevelUpgradeCost.get(element)[nextLevel];
    }
    public static int getElementMaxLevelUpgradeCost(String element, int nextLevel){
        return elementsMaxLevelUpgradeCost.get(element)[nextLevel];
    }
    public static String getAnimalClassPath(String animal){
        return animalsClassName.getOrDefault(animal.toLowerCase(), null);
    }

    public static Processable getProcessableElement(String name){
        return processableElements.getOrDefault(name, null);
    }
}
