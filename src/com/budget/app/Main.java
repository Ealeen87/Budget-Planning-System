package com.budget.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static AppState state = new AppState();

    @Override
    public void start(Stage stage) {
        stage.setTitle("💰 Budget Planning System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        new Navigator(stage, state).login();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
