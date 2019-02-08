package Utilities;

import Interfaces.Processable;

import java.util.HashMap;
import java.util.Random;

public enum AnimalType implements Processable {
    BrownBear(true), WhiteBear(true), Grizzly(true), Lion(true), Jaguar(true),

    Cat(false), Dog(false),
    Sheep(false), GuineaFowl(false), Ostrich(false), Cow(false), Buffalo(false),
    Turkey(false), Chicken(false), Penguin(false), Llama(false), BrownCow(false), Walrus(false),
    Yak(false), KingPenguin(false), Goose(false), Goat(false);
    public final boolean IS_WILD;

    private static HashMap<String, AnimalType> wilds;
    private static HashMap<String, AnimalType> pets;

    AnimalType(boolean IS_WILD) {
        this.IS_WILD = IS_WILD;
    }

    static {
        wilds = new HashMap<>();
        pets = new HashMap<>();
        for (AnimalType type : AnimalType.values()) {
            if (type.IS_WILD) {
                wilds.put(type.toString(), type);
            } else {
                pets.put(type.toString(), type);
            }
        }
    }

    public static HashMap<String, AnimalType> getWilds() {
        return wilds;
    }

    public static AnimalType getType(String name) {
        if (pets.containsKey(name)) {
            return pets.get(name);
        }
        if (wilds.containsKey(name)) {
            return wilds.get(name);
        }
        return null;
    }
}