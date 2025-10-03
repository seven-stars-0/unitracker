package project.unitracker.model.database;

import java.sql.Connection;
import java.sql.SQLException;

// La classe di base di tutte le classi relative al database, che racchiude i loro metodi comuni
public abstract class DatabaseClass {
    protected Connection connection;

    // Ogni classe ha degli statement diversi, e alcune non ne hanno proprio
    protected abstract void setStatements() throws SQLException;

    // Questa viene chiamata alla chiusura del programma per chiudere la connessione
    public abstract void closeStatements() throws SQLException;

    // Riceve la connessione (di solito da DatabaseHandler) e inizializza i PreparedStatement
    public void setConnection(Connection connection) throws SQLException {
        if ( this.connection == null ) {
            this.connection = connection;
            setStatements();
        }
    }

    // Questa viene usata solo da DatabaseHandler e RetrieverHandler
    protected abstract void initializeInstances() throws SQLException;
}
