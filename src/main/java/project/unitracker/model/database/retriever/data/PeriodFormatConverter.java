package project.unitracker.model.database.retriever.data;

import java.util.HashMap;
import java.util.Map;

// Converte i periodi in formato PeriodFormat, per interagire con il database e mostrare i dati
// relativi a una certa finestra temporale
public class PeriodFormatConverter {

    public static final Map<String, PeriodFormat> PERIOD_MAP = new HashMap<>();

    static {
        PERIOD_MAP.put("D", new PeriodFormat("%Y-%m-%d", "-14 days")); // ultimi 14 giorni
        PERIOD_MAP.put("W", new PeriodFormat("%Y-W%W", "-84 days")); // ultime 12 settimane
        PERIOD_MAP.put("M", new PeriodFormat("%Y-%m", "-12 months")); // ultimi 12 mesi
        PERIOD_MAP.put("Y", new PeriodFormat("%Y", "-5 years")); // ultimi 5 anni
    }

    public static PeriodFormat get(String key) {
        return PERIOD_MAP.get(key);
    }
}
