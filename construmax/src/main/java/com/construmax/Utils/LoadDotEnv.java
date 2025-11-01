package com.construmax.Utils;

import io.github.cdimascio.dotenv.Dotenv;

public class LoadDotEnv {
    public static void loadDotEnv () {
        Dotenv dotenv = Dotenv.configure().directory("/Users/joaovitor/Documents/GitHub/Trabalho-de-Poo/construmax/.env").load();

    }
}
