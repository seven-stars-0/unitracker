package project.unitracker.utility.psql;

import java.util.List;

// Classe utilizzata per comunicare con DatabaseHandler per la verifica o l'esecuzione di una certa operazione
// Strettamente legata a PseudoSQL, ma non esclusiva al linguaggio (i Controller creano ParsedCommand senza passare per PSQL)
public class ParsedCommand {
    public String type;
    public List<String> parameters;
    public boolean apply;

    public ParsedCommand(String type,List<String> parameters, boolean apply) {
        this.type = type;
        this.parameters = parameters;
        this.apply = apply;
    }

    @Override
    // Converte il ParsedCommand in un comando PseudoSQL
    public String toString() {
        StringBuilder command = new StringBuilder(type);

        if ( !parameters.isEmpty() ) {
            // Il primo parametro è preceduto da uno spazio
            command.append(" ").append(parameters.getFirst());

            String currParameter;
            // Gli altri da una virgola, come separatore con gli altri parametri
            for (int i = 1; i < parameters.size(); i++)
                if ( (currParameter = parameters.get(i)) != null )
                    command.append(", ").append(currParameter);
        }

        if (apply)
            command.append(", APPLY");

        return command.toString();
    }
}
