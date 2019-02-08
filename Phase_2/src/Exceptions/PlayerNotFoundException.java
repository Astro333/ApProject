package Exceptions;

import java.io.IOException;

public class PlayerNotFoundException extends IOException {
    public PlayerNotFoundException(){
        super();
    }
    public PlayerNotFoundException(String message){
        super(message);
    }
}
