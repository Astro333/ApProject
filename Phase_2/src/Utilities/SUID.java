package Utilities;

import java.util.UUID;

public class SUID {

    /**
     * @return ID: must generate a Unique id
     * */
    public static Long generateId(){
        return UUID.randomUUID().getLeastSignificantBits();
    }
}
