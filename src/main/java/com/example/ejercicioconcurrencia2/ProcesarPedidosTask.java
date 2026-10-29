package com.example.ejercicioconcurrencia2;

import javafx.concurrent.Task;

public class ProcesarPedidosTask extends Task<Integer> {
    public static final int TOTAL_PEDIDOS = 30;
    public static final long ESPERA_MS = 200;
    public static final int PEDIDO_CON_ERROR = 15;

    private final boolean simularError;

    public ProcesarPedidosTask(boolean simularError) {
        this.simularError = simularError;
        updateProgress(0, TOTAL_PEDIDOS);
    }



    @Override
    protected Integer call() throws Exception{
        int completado = 0;
        try {
            for (int pedido = 0; pedido < TOTAL_PEDIDOS; pedido++) {
                if(isCancelled()){
                    break;
                }
                updateMessage("Iniciando proceso de pedido de" + pedido + "de: " + TOTAL_PEDIDOS );

                try {
                    Thread.sleep(ESPERA_MS);
                }
                catch (InterruptedException e) {
                    if (isCancelled())
                        break;
                    throw e;
                }


                if (simularError && pedido == PEDIDO_CON_ERROR) {
                    throw new Exception("Pedido fallido en pedido: " + pedido);
                }

                completado++;
                updateProgress(completado, TOTAL_PEDIDOS);
                updateValue(completado);
            }
            return completado;
        }

        finally {
            System.out.println("Terminando proceso de pedido" + Thread.currentThread().getName()
            + "salio del call() con:" + completado + "pedidos completados") ;
        }
    }
}
