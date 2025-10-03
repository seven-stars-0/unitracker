package project.unitracker.model.psql.syntax;


import project.unitracker.utility.psql.ParsedCommand;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// La classe statica che verifica la sintassi dei comandi PseudoSQL forniti in input
// Se corretta, converte la stringa in un ParsedCommand pronto all'analisi semantica in SemanticChecker, altrimenti
// restituisce null, segnalando indirettamente la presenza di un errore di sintassi nella stringa fornita
public class SyntaxChecker {

    // --- I Pattern usati per verificare la sintassi e convertirla in ParsedCommand ---
    private final static Pattern NO_ARGS = Pattern.compile(PseudoSQLRegex.NO_ARGS_REGEX);
    private final static Pattern START = Pattern.compile(PseudoSQLRegex.START_REGEX);
    private final static Pattern DELETE = Pattern.compile(PseudoSQLRegex.DELETE_REGEX);
    private final static Pattern ADD = Pattern.compile(PseudoSQLRegex.ADD_REGEX);
    private final static Pattern TRANSACTION = Pattern.compile(PseudoSQLRegex.TRANSACTION_REGEX);

    private final static Pattern ONE_ID = Pattern.compile(PseudoSQLRegex.ONE_ID_REGEX);
    private final static Pattern TWO_ID = Pattern.compile(PseudoSQLRegex.TWO_ID_REGEX);
    private final static Pattern THREE_ID = Pattern.compile(PseudoSQLRegex.THREE_ID_REGEX);
    private final static Pattern FOUR_ID = Pattern.compile(PseudoSQLRegex.FOUR_ID_REGEX);

    // Crea una lista che contiene tutti i gruppi del matcher
    private static ArrayList<String> createArguments(Matcher matcher, int numGroups) {
        ArrayList<String> arguments = new ArrayList<>();

        for ( int i = 1; i <= numGroups; i++ )
            arguments.add(matcher.group(i));

        return arguments;
    }

    // Parsing di comandi generici
    // In base a quanti parametri prende il comando, si usa un Pattern diverso
    private static ParsedCommand parseGenericCommand(String type, String input, int numGroups) {
        Matcher matcher;
        switch (numGroups) {
            case 0 -> {
                matcher = TRANSACTION.matcher(input);
                // Questo per far comunque creare un argomento a createArguments
                numGroups = 1;
            }
            case 1 -> matcher = ONE_ID.matcher(input);
            case 2 -> matcher = TWO_ID.matcher(input);
            case 3 -> matcher = THREE_ID.matcher(input);
            case 4 -> matcher = FOUR_ID.matcher(input);
            default -> { return null; }
        }

        if (matcher.find())
            return new ParsedCommand(type, createArguments(matcher, numGroups), false);
        return null;
    }

    // Il parsing specifico per il comando ADD
    private static ParsedCommand parseAdd(String input) {
        Matcher matcher = ADD.matcher(input);

        if ( matcher.find() ) {
            // I parametri 'data' e 'descrizione' sono opzionali, quindi se non forniti sono null
            // In ogni caso vengono aggiunti ad arguments, e CommandExecutor gestisce il caso null
            ArrayList<String> arguments = createArguments(matcher, 4);
            boolean apply = matcher.group(5) != null;

            return new ParsedCommand("ADD", arguments, apply);
        }
        return null;
    }

    // Parsing specifico per il comando DELETE
    private static ParsedCommand parseDelete(String input) {
        Matcher matcher = DELETE.matcher(input);

        if ( matcher.find() ) {
            boolean apply = matcher.group(2) != null;
            return new ParsedCommand("DELETE", createArguments(matcher, 1), apply);
        }
        return null;
    }

    // Unico metodo esposto
    // Riceve una String di testo e verifica che corrisponda a un comando PSQL sintatticamente corretto
    // In caso positivo lo converte in un ParsedCommand, in caso negativo restituisce null segnalando un errore sintattico
    public static ParsedCommand parseCommand(String input) {
        Matcher matcher = NO_ARGS.matcher(input);

        // Viene prima verificato che il comando sia uno che non richiede parametri
        if (matcher.matches())
            return new ParsedCommand(matcher.group(1), new ArrayList<>(), false);

        matcher = START.matcher(input);

        // Verifica che 'input' cominci con un comando PSQL
        if ( matcher.find() ) {
            String commandType = matcher.group(1);
            String args = input.substring(matcher.end()).trim(); // <-- rimuove "G_CREATE" ecc.

            return switch (commandType) {
                // --- Comandi che richiedono solo un id di una transazione ---
                case "A_APPLY", "A_CANCEL", "D_APPLY", "D_CANCEL" -> parseGenericCommand(commandType, args, 0);

                // --- Comandi che richiedono 1 identificatore ---
                case "G_DROP", "C_DROP", "APPLY", "CANCEL" -> parseGenericCommand(commandType, args, 1);

                // --- Comandi che richiedono 2 identificatori ---
                case "G_MOVE", "G_RENAME", "C_MOVE", "C_RENAME" -> parseGenericCommand(commandType, args, 2);

                // --- Comandi che richiedono 3 identificatori ---
                case "G_CREATE" -> parseGenericCommand(commandType, args, 3);

                // --- Comandi che richiedono 4 identificatori ---
                case "C_CREATE" -> parseGenericCommand(commandType, args, 4);

                // --- Comandi con parsing specifico ---
                case "DELETE" -> parseDelete(args);
                case "ADD" -> parseAdd(args);

                // Non viene mai chiamato ma è qui per completezza
                default -> null;
            };
        }

        return null;
    }

}
