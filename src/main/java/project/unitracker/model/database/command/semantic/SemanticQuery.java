package project.unitracker.model.database.command.semantic;

public class SemanticQuery {
    private final static String countColumn = "SELECT COUNT(*) AS count FROM ";

    public final static String CHART_EXISTENCE = countColumn + "Grafico WHERE codice = ?;";
    public final static String GROUP_EXISTENCE = countColumn + "Gruppo WHERE codice = ?;";
    public final static String APPLIED_TRANSACTION_EXISTENCE = countColumn + "TransazioneApprovata WHERE id = ?";

    // Questa serve quando facciamo DELETE provvisorio due volte sulla stessa transazione
    // Per evitare che vengano aggiunte a TransazioneCancellataProvvisoria due volte (causando una violazione di PRIMARY KEY)
    // qui filtriamo in modo da rendere valida l'operazione solo se esiste e non è già stata cancellata
    public final static String APTE_NOT_CANCEL = APPLIED_TRANSACTION_EXISTENCE + " AND id NOT IN ( SELECT id FROM TransazioneCancellataProvvisoria );";
    public final static String ADD_PROV_TRANS_EXISTENCE = countColumn + "TransazioneProvvisoria WHERE id = ?;";
    public final static String DEL_PROV_TRANS_EXISTENCE = countColumn + "TransazioneCancellataProvvisoria WHERE id = ?;";

    public final static String CHART_GROUP = "SELECT gruppo FROM Grafico WHERE codice = ?;";
    public final static String GROUP_SUPERGROUP = "SELECT supergruppo AS gruppo FROM Gruppo WHERE codice = ?;";

    public final static String CHART_NAME_EXISTENCE = countColumn + "Grafico WHERE gruppo = ? AND nome = ?;";
    public final static String GROUP_NAME_EXISTENCE = countColumn + "Gruppo WHERE supergruppo = ? AND nome = ?;";
}
