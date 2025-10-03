package project.unitracker.model.psql.syntax;

public class PseudoSQLRegex {

    // Regex di base usate per comporre quelle public

    // Mostra le opzioni disponibili per le operazioni che coinvolgono gruppi e grafici
    private final static String optionsGroupTable = "(?:C|G)_(?:CREATE|DROP|MOVE|RENAME)";
    // Le opzioni per gestire le transazioni provvisorie
    private final static String optionsAddDelete = "(?:A|D)_(?:APPLY|CANCEL)";
    // Il separatore tra varie informazioni
    private final static String separator = "\\s*,\\s*";
    // Rileva la quantità coinvolta in una transazione
    private final static String quantitySyntax = "(\\d+(?:\\.\\d{0,2})?)";
    // Rileva la data
    private final static String dataSyntax =  "(?:" + separator + "(\\d{4}-\\d{2}-\\d{2}))?";
    // Rileva la descrizione
    private final static String descriptionSyntax = "(?:" + separator + "(?!\\s*APPLY\\b)([^,]+))?";
    // Questa viene usata nelle operazioni UNAPPLIED_ADD e UNAPPLIED_DELETE per capire se l'operazione deve essere applicata subito
    private final static String applySyntax = "(?:" + separator + "(APPLY))?\\s*$";

    // Regex esposte, che vengono effettivamente usate nei Pattern di PseudoSQLHandler

    // --- Regex per gruppi di argomenti (riutilizzabili in più comandi) ---
    // Rileva il nome di un identificatore, che sia un gruppo o un grafico
    public final static String ONE_ID_REGEX = "([^,]+)";
    // Questa riesce a identificare due identificatori
    public final static String TWO_ID_REGEX = ONE_ID_REGEX + separator + ONE_ID_REGEX;
    // Questa rileva tre identificatori
    public final static String THREE_ID_REGEX = TWO_ID_REGEX + separator + ONE_ID_REGEX;
    // Usata per rilevare quattro identificatori
    public final static String FOUR_ID_REGEX = THREE_ID_REGEX + separator + ONE_ID_REGEX;

    // --- Regex per comandi completi ---

    // Questi comandi non richiedono parametri e hanno una regex a parte per non far perdere la
    // generalità a START_REGEX
    public final static String NO_ARGS_REGEX = "^\\s*((?:CANCEL|APPLY)_ALL|COMPLETE_DROP)\\s*$";

    public final static String START_REGEX = "^\\s*(" + optionsGroupTable + "|" + optionsAddDelete + "|ADD|DELETE|CANCEL|APPLY)\\s+";
    public final static String ADD_REGEX = ONE_ID_REGEX + separator + quantitySyntax + dataSyntax + descriptionSyntax + applySyntax;
    // Questa serve per le operazioni che richiedono solo l'id di una transazione
    public final static String TRANSACTION_REGEX = "(\\d+)";
    // La DELETE ha anche l'opzione di APPLY
    public final static String DELETE_REGEX = TRANSACTION_REGEX + applySyntax;
}
