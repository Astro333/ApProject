package Utilities;

import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

public class PasswordSkin extends TextFieldSkin {

    public PasswordSkin(TextField control) {
        super(control);
    }

    @Override
    protected String maskText(String txt) {
        int n = txt.length();
        StringBuilder passwordBuilder = new StringBuilder(n);
        for (int i = 0; i < n - 1; i++)
            passwordBuilder.append('●');
        if (n > 0)
            passwordBuilder.append(txt.charAt(n-1));

        return passwordBuilder.toString();
    }
}
