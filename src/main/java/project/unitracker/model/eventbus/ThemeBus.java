package project.unitracker.model.eventbus;

import project.unitracker.controller.Controller;

import java.util.ArrayList;
import java.util.List;

// Il singleton che fa da tramite tra StageHandler e tutti i Controller di una finestra
// Segnala cambiamenti di tema in modo che tutte le finestre aperte mantengano una coerenza di tema tra di loro
public class ThemeBus {
    private static ThemeBus instance;
    private final List<Controller> controllerList;

    private ThemeBus() {
        controllerList = new ArrayList<>();
    }

    public static ThemeBus getInstance() {
        if (instance == null) instance = new ThemeBus();
        return instance;
    }

    // I Controller che si iscrivono a questo chiamano nella loro implementazione di reload()
    // StageHandler.applyTheme(), che cambia il CSS della loro scena
    public void signalThemeChange() {
        for (Controller controller : controllerList)
            controller.reload();
    }

    public void register(Controller controller) { controllerList.add(controller); }
    public void unregister(Controller controller) { controllerList.remove(controller); }
}
