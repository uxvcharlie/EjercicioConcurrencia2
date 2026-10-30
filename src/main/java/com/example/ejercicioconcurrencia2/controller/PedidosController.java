package com.example.ejercicioconcurrencia2.controller;

import com.example.ejercicioconcurrencia2.EstadoApp;
import com.example.ejercicioconcurrencia2.Evento;
import com.example.ejercicioconcurrencia2.ProcesarPedidosTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PedidosController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnIniciar;

    @FXML
    private CheckBox chkSimular;

    @FXML
    private TableColumn<Evento, String> colDetalle;

    @FXML
    private TableColumn<Evento, String > colEstado;

    @FXML
    private TableColumn<Evento, String> colHora;

    @FXML
    private Label lblContador;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblPorcentaje;

    @FXML
    private Label lblDetalle;

    @FXML
    private ProgressBar pgrEstado;

    @FXML
    private TableView<Evento> tblRegistro;


    private final ObservableList<Evento> eventos = FXCollections.observableArrayList();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r,
            "trabajador-pedidos")
    );

    private ProcesarPedidosTask tareaActual;

    private EstadoApp estado;

    private int ejecuciones = 0;

    @FXML
    public void initialize() {
        configurarTableViwe();
        pgrEstado.setProgress(0.0);

        lblPorcentaje.textProperty().bind(pgrEstado.progressProperty().
                multiply(100).
                asString("%.0f %%"));
        lblContador.setText(textoContador(0));
        lblDetalle.setText("Preciona el boton iniciar para procesar los pedidos");
        mostrarEstado(EstadoApp.LISTO);
    }

    private void configurarTableViwe(){
        colHora.setCellValueFactory(data -> new
                SimpleStringProperty(data.getValue().getHora()));
        colEstado.setCellValueFactory(data ->new SimpleStringProperty(
                data.getValue().getEstado()
        ));

        colDetalle.setCellValueFactory(data ->new SimpleStringProperty(
                data.getValue().getDetalle()

        ));


        tblRegistro.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblRegistro.setItems(eventos);
    }


    @FXML
    private void cancelar() {
        if (tareaActual == null || estado != EstadoApp.EN_EJECUCION) {
            return;
        }
        mostrarEstado(EstadoApp.CANCELANDO);
        tareaActual.cancel();
    }
    @FXML
    void iniciar(){
        if (tareaActual != null) {
            registrar("Inicio ignorado, porque aun hay un trabajador activo");
            return;
        }
        ejecuciones++;
        boolean conError = chkSimular.isSelected();
        ProcesarPedidosTask tarea = new ProcesarPedidosTask(conError);

        tareaActual = tarea;

        lblContador.setText(textoContador(0));

        pgrEstado.progressProperty().bind(tarea.progressProperty());

        lblDetalle.textProperty().bind(tarea.messageProperty());

        tarea.valueProperty().addListener((observable, oldValue, nuevo) -> {
            if(nuevo != null){

                lblContador.setText(textoContador(nuevo));
            }
        });

        registrar("Ejecucion #" + ejecuciones + "iniciada" + (conError ? "con error simulado"
                : ""));
        mostrarEstado(EstadoApp.EN_EJECUCION);


        CompletableFuture.runAsync(tarea, executor)
                .whenComplete((nada, error) -> Platform.runLater(() -> trabajadorTerminado(tarea)));

    }

    private void trabajadorTerminado(ProcesarPedidosTask tarea) {
        pgrEstado.progressProperty().unbind();
        lblDetalle.textProperty().unbind();
        tareaActual = null;

        int completados = tarea.getValue() == null ? 0 : tarea.getValue();
        registrar("Trabajador terminado (Task: " + tarea.getState()
                + ", completados: " + completados + ")");

        switch (tarea.getState()) {
            case SUCCEEDED -> {
                lblDetalle.setText("Se procesaron los " + ProcesarPedidosTask.TOTAL_PEDIDOS + " pedidos");
                mostrarEstado(EstadoApp.COMPLETADO);
            }
            case CANCELLED -> {
                lblDetalle.setText("Se canceló con " + completados + " pedidos completados");
                mostrarEstado(EstadoApp.CANCELADO);
            }
            default -> {
                lblDetalle.setText("Error: " + tarea.getException().getMessage());
                mostrarEstado(EstadoApp.ERROR);
            }
        }

    }


    private void mostrarEstado(EstadoApp nuevo) {
        estado = nuevo;
        lblEstado.setText(nuevo.getTexto());
        String color = switch (nuevo) {
            case LISTO -> "#555555";
            case EN_EJECUCION -> "#1565c0";
            case CANCELANDO -> "#e67e22";
            case CANCELADO -> "#8e44ad";
            case COMPLETADO -> "#2e7d32";
            case ERROR -> "#c62828";
        };

        lblEstado.setStyle("-fx-text-fill: " + color + ";");

        boolean trabajando = tareaActual != null;
        btnIniciar.setDisable(trabajando);
        btnCancelar.setDisable(estado != EstadoApp.EN_EJECUCION);

        chkSimular.setDisable(trabajando);
        registrar("Cambio de estado");

    }


    private void registrar(String detalle) {
        String hora = LocalTime.now().format(formatter);

        eventos.add(new Evento(hora, estado.getTexto(), detalle));
        tblRegistro.scrollTo(eventos.size() - 1);
        System.out.println(hora + "" + estado.getTexto() + "" + detalle);

    }

    private String textoContador(int completados){
        return completados + "|" + ProcesarPedidosTask.TOTAL_PEDIDOS;
    }

    public void cerrarVentana() {
        registrar("Cerrando la ventana");
        if (tareaActual != null) {
            tareaActual.cancel();   // pedirle al trabajador que pare
        }
        executor.shutdownNow();     // no acepta más tareas e interrumpe al hilo; NO espera

        // La espera va en otro hilo para no parar el hilo
        Thread vigilante = new Thread(() -> {
            try {
                boolean apagado = executor.awaitTermination(2, TimeUnit.SECONDS);
                System.out.println("Ejecutor apagado y sin trabajadores activos: " + apagado);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "vigilante-cierre");
        vigilante.start();
    }

}
