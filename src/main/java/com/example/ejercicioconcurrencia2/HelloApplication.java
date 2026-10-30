package com.example.ejercicioconcurrencia2;

import com.example.ejercicioconcurrencia2.controller.PedidosController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("pedidos-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        PedidosController controller = fxmlLoader.getController();

        stage.setOnCloseRequest(e -> controller.cerrarVentana());

        stage.setTitle("Procesamiento de pedidos");
        stage.setScene(scene);
        stage.show();
    }
}