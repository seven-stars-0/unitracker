package project.unitracker.utility.constant;

// Classe che contiene gli URL relativi a file utili per il funzionamento del progetto
public class URL {
    // --- Locazione dei database ---
    public final static String DATABASE = "db/database.db";
    public final static String DB_BACKUP = "/backup/database.db"; // Partendo da resources
    // ------

    // --- Locazione di dati utente ---
    public final static String dataURL = "data/";

    public final static String PERIODIC = dataURL +  "periodic.txt";
    public final static String LAST_THEME = dataURL + "last-theme.txt";
    public final static String COMPASSION = dataURL + "compassion-counter.txt";

    public final static String DARK_THEME = "/css/dark-theme.css";
    public final static String LIGHT_THEME = "/css/light-theme.css";
    // ------

    // --- Icone ---
    private final static String iconsURL = "/icons/";

    public final static String APP_ICON = iconsURL + "application.png";
    public final static String CHART_ICON = iconsURL + "chart.png";
    public final static String GROUP_ICON = iconsURL + "group.png";
    public final static String SUCCESS_ICON = iconsURL + "success-circle.png";
    public final static String ERROR_ICON = iconsURL + "error-circle.png";
    // ------

    private final static String viewURL = "/view/";

    // --- Main ---
    private final static String mainURL = viewURL + "main/";

    public final static String MAIN_FXML = mainURL + "main.fxml";
    public final static String GROUP_VIEW_FXML = mainURL + "group-tree.fxml";
    // ------

    // --- Tabs ---
    private final static String tabURL = viewURL + "tab/";

    public final static String CHART_FXML = tabURL + "chart.fxml";
    public final static String CHART_DATA_FXML = tabURL + "chart-data.fxml";
    public final static String PERIODIC_FXML = tabURL + "periodic.fxml";
    public final static String PSQL_FXML = tabURL + "psql-ide.fxml";
    // ------

    // --- Modali ---
    private final static String modalURL = viewURL + "modal-window/";

    public final static String ADD_TRANSACTION_FXML = modalURL + "add-transaction.fxml";
    public final static String ADD_PERIODIC_FXML = modalURL + "add-periodic.fxml";
    public final static String DROP_FXML = modalURL + "drop.fxml";
    public final static String RENAME_MOVE_FXML = modalURL + "rename-move.fxml";
    public final static String C_CREATE_FXML = modalURL + "c-create.fxml";
    public final static String G_CREATE_FXML = modalURL + "g-create.fxml";
    public final static String MODAL_COMPASSION = modalURL + "compassion.fxml";
    // ------

    // --- Altre ---
    public final static String otherURL = viewURL + "other/";

    public final static String TUTORIAL_FXML = otherURL + "psql-tutorial.fxml";
    public final static String CREDITS_FXML = otherURL + "credits.fxml";
    // ------
}


