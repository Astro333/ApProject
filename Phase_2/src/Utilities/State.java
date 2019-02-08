package Utilities;

import java.util.HashMap;
import java.util.Map;

public enum State {
    MOVE_UP(4), MOVE_RIGHT(3), MOVE_LEFT(1), MOVE_DOWN(6), MOVE_UP_LEFT(0),
    MOVE_DOWN_LEFT(5), MOVE_DOWN_RIGHT(2), MOVE_UP_RIGHT(7),
    Caged(1 << 3), BrokeCage(1 << 4), Collected(1 << 5), Fight(1 << 6), Eat(1 << 7), Death(1 << 8), Spawn(1 << 9),
    Tossed(1 << 10), Crack(1 << 11), ItemDisappear(1 << 12), Waiting(1 << 13), Working(1 << 14),
    FinishedJob(1 << 15), BeingCollected(1 << 16), Spawning(1 << 17), Produced(1 << 18), MarkedToRemove(1 << 19),
    BeingTossed(1 << 20);

    public final int value;
    private static final Map<Integer, State> map = new HashMap<>();

    static {
        for (State state : values())
            map.put(state.value, state);
    }

    public static boolean is(State state, int status) {
        return ((status & state.value) != 0);
    }

    public static State get(int x) {
        return map.get(x);
    }

    State(int value) {
        this.value = value;
    }
}