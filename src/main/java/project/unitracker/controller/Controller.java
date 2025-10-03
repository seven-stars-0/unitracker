package project.unitracker.controller;

import java.util.List;

// L'interfaccia di tutti i controller dell'applicazione
// Mostra tutti i metodi che ogni controller deve implementare
public interface Controller {
    void setParameters(List<Object> parameters);
    void reload();
    void onClosed();
}
