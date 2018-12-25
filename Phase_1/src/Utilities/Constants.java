package Utilities;

import Animals.Animal;
import Interfaces.Processable;
import Items.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Constants {

    private static HashMap<String, Float> productsDepotSize;
    private static HashMap<String, Integer> productsBuyCost;
    private static HashMap<String, Integer> productsSaleCost;

    private static HashMap<String, String> animalsClassName;

    private static HashMap<String, Integer[]> elementsLevelUpgradeCost;
    private static HashMap<String, Integer[]> elementsMaxLevelUpgradeCost;

    private static HashMap<String, Pair<Integer, Integer>> workshopsDropZone; // from continent

    private static HashMap<String, Pair<Integer, Integer>> workshopsPosition; // from continent

    private static HashMap<String, Processable> processableElements;

    static {
        processableElements = new HashMap<>();
        for (Item.ItemType itemType : Item.ItemType.values())
            processableElements.put(itemType.toString(), itemType);

        for (Animal.AnimalType animalType : Animal.AnimalType.values())
            processableElements.put(animalType.toString(), animalType);
    }

    static {
        Gson gson = new GsonBuilder().create();
        Reader reader = null;
        Type type = new TypeToken<HashMap<String, Float>>() {
        }.getType();

        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/productsDepotSize.json"),
                    StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        productsDepotSize = gson.fromJson(reader, type);

        type = new TypeToken<HashMap<String, Integer>>() {
        }.getType();

        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/productsSaleCost.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        productsSaleCost = gson.fromJson(reader, type);

        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/productsBuyCost.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        productsBuyCost = gson.fromJson(reader, type);

        type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/animalsClassPath.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        animalsClassName = gson.fromJson(reader, type);

        type = new TypeToken<HashMap<String, Integer[]>>() {
        }.getType();

        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/elementsLevelUpgradeCost.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        elementsLevelUpgradeCost = gson.fromJson(reader, type);

        try {
            reader = new InputStreamReader(new FileInputStream(
                    "Phase_1/DefaultGameData/elementsMaxLevelUpgradeCost.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // ToDo: Essential to be set for Phase_2:
        workshopsPosition = new HashMap<>();
        workshopsDropZone = new HashMap<>();
        for(int i = 1; i <= 6; ++i){
            workshopsPosition.put("Africa"+i, new Pair<>(0, 0));
            workshopsPosition.put("Antarctica"+i, new Pair<>(0, 0));
            workshopsPosition.put("Prairie"+i, new Pair<>(0, 0));
            workshopsPosition.put("SouthAmerica"+i, new Pair<>(0, 0));
            workshopsPosition.put("Russia"+i, new Pair<>(0, 0));

            workshopsDropZone.put("Africa"+i, new Pair<>(i, i));
            workshopsDropZone.put("Antarctica"+i, new Pair<>(i, i));
            workshopsDropZone.put("Prairie"+i, new Pair<>(i, i));
            workshopsDropZone.put("SouthAmerica"+i, new Pair<>(i, i));
            workshopsDropZone.put("Russia"+i, new Pair<>(i, i));
        }

        elementsMaxLevelUpgradeCost = gson.fromJson(reader, type);
    }

    public static byte getElementMaxMaxLevel(String element) {
        return (byte)elementsMaxLevelUpgradeCost.get(element).length;
    }

    public static Float getProductSize(String product) {
        return productsDepotSize.getOrDefault(product, null);
    }

    public static Integer getProductSaleCost(String product) {
        return productsBuyCost.getOrDefault(product, null);
    }

    public static Integer getProductBuyCost(String product) {
        return productsBuyCost.getOrDefault(product, null);
    }

    public static Pair<Integer, Integer> getWorkshopDropZone(String continent, byte position) {
        return workshopsDropZone.getOrDefault(continent + position, null);
    }

    public static Pair<Integer, Integer> getWorkshopPosition(String continent, byte position) {
        return workshopsPosition.getOrDefault(continent + position, null);
    }

    public static int getElementLevelUpgradeCost(String element, int nextLevel) {
        return elementsLevelUpgradeCost.get(element)[nextLevel];
    }

    public static int getElementMaxLevelUpgradeCost(String element, int nextLevel) {
        return elementsMaxLevelUpgradeCost.get(element)[nextLevel];
    }

    public static String getAnimalClassPath(String animal) {
        return animalsClassName.getOrDefault(animal.toLowerCase(), null);
    }

    public static Processable getProcessableElement(String name) {
        return processableElements.getOrDefault(name, null);
    }
}
