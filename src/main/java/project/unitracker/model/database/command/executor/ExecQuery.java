package project.unitracker.model.database.command.executor;

public class ExecQuery {
    public final static String G_CREATE =
            "INSERT INTO Gruppo(codice, supergruppo, nome) VALUES (?, ?, ?);";
    public final static String G_DROP =
            "DELETE FROM Gruppo WHERE codice = ?;";
    public final static String G_MOVE =
            "UPDATE Gruppo SET supergruppo = ? WHERE codice = ?;";
    public final static String G_RENAME =
            "UPDATE Gruppo SET nome = ? WHERE codice = ?;";

    public final static String C_CREATE =
            "INSERT INTO Grafico(codice, gruppo, nome, unita_di_misura) VALUES (?, ?, ?, ?);";
    public final static String C_DROP =
            "DELETE FROM Grafico WHERE codice = ?;";
    public final static String C_MOVE =
            "UPDATE Grafico SET gruppo = ? WHERE codice = ?;";
    public final static String C_RENAME =
            "UPDATE Grafico SET nome = ? WHERE codice = ?;";

    public final static String APPLIED_ADD =
            "INSERT INTO TransazioneApprovata(grafico, quantita, data_transazione, descrizione) VALUES (?, ?, ?, ?);";
    public final static String APPLIED_DELETE =
            "DELETE FROM TransazioneApprovata WHERE id = ?;";

    public final static String UNAPPLIED_ADD =
            "INSERT INTO TransazioneProvvisoria(grafico, quantita, data_transazione, descrizione) VALUES (?, ?, ?, ?);";
    public final static String UNAPPLIED_DELETE =
            "INSERT INTO TransazioneCancellataProvvisoria(id) VALUES (?);";

    public final static String COMPLETE_GROUP_DROP =
            "DELETE FROM Gruppo WHERE codice != \"ROOT\"";
    public final static String COMPLETE_CHART_DROP =
            "DELETE FROM Grafico";

    private final static String idTransaction = " WHERE id = ?";

    public final static String D_APPLY_TEMPLATE = "DELETE FROM TransazioneApprovata";

    // --- Queste vengono usate da APPLY_ALL per aggiungere e cancellare tutte le operazioni provvisorie ---
    public final static String A_APPLY_ALL =
            "INSERT INTO TransazioneApprovata(grafico, quantita, data_transazione, descrizione) SELECT grafico, quantita, data_transazione, descrizione FROM TransazioneProvvisoria";
    public final static String D_APPLY_ALL = D_APPLY_TEMPLATE + " WHERE id IN (SELECT id FROM TransazioneCancellataProvvisoria) ";

    // --- Queste vengono usate da (APPLY|CANCEL)_ALL per cancellare le transazioni già applicate
    // per eliminarle senza applicarle ---
    public final static String A_CANCEL_ALL =
            "DELETE FROM TransazioneProvvisoria";
    public final static String D_CANCEL_ALL =
            "DELETE FROM TransazioneCancellataProvvisoria";

    // --- Queste invece filtrano per id, per cancellare solo delle operazioni specifiche ---
    public final static String A_APPLY = A_APPLY_ALL + idTransaction;
    public final static String D_APPLY = D_APPLY_TEMPLATE + idTransaction;

    public final static String A_CANCEL = A_CANCEL_ALL + idTransaction;
    public final static String D_CANCEL = D_CANCEL_ALL + idTransaction;

    // Questa invece filtra per grafico
    private final static String chartFilter = " WHERE grafico = ?";

    public final static String A_APPLY_CHART = A_APPLY_ALL + chartFilter;
    public final static String D_APPLY_CHART = D_APPLY_TEMPLATE + chartFilter + " AND id IN ( SELECT id FROM TransazioneCancellataProvvisoria )";
    public final static String A_CANCEL_CHART = A_CANCEL_ALL + chartFilter;
    public final static String D_CANCEL_CHART = D_CANCEL_ALL + " WHERE id IN ( SELECT id FROM TransazioneApprovata WHERE grafico = ? )";


    // Viene chiamata da (CANCEL|APPLY)_ALL
    public final static String ALL_CHARTS_AFFECTED_CODE = "SELECT DISTINCT Grafici.grafico AS codice FROM ( SELECT grafico FROM TransazioneProvvisoria UNION ALL SELECT grafico FROM TransazioneCancellataProvvisoria AS tcp JOIN TransazioneApprovata AS ta ON tcp.id = ta.id ) AS Grafici";

    // Per capire a che grafico appartiene l'id di una transazione
    public final static String TRANSACTION_CHART_CODE = "SELECT grafico AS codice FROM TransazioneApprovata WHERE id = ?;";

    // Per capire a che grafico si riferisce una TransazioneProvvisoria
    public final static String A_APPLY_CHART_CODE = "SELECT grafico AS codice FROM TransazioneProvvisoria WHERE id = ?;";
}