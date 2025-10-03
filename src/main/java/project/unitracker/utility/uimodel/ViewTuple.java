package project.unitracker.utility.uimodel;

import javafx.scene.Parent;
import project.unitracker.controller.Controller;

// Usata da StageHandler per restituire il root di una scena e il controller a essa associata
public record ViewTuple(Parent root, Controller controller) {
}
