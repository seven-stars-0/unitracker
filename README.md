# UniTracker

UniTracker è un'applicazione **JavaFX** per mostrare dati in formato grafico, sviluppata come progetto universitario.
Permette di creare e gestire grafici, raggrupparli in cartelle (dette **gruppi**) e di impostare transazioni periodiche per un controllo automatizzato dell'inserimento dei dati.
Introduce **PseudoSQL**, un linguaggio interpretato creato appositamente per inserire e manipolare dati nell'applicazione.

---

## Contenuto del progetto

- `Launcher.java` - entry point dell'applicazione
- `src/` - codice sorgente
- `resources/` - immagini, CSS, FXML

---

## Funzionalità principali

- Creazione e gestione di **gruppi e grafici** gerarchici
- Gestione testuale di dati, grafici e gruppi tramite **PseudoSQL**
- Visualizzazione dei dati tramite **grafici**
- Backup e import/export dei dati, grafici e gruppi tramite PseudoSQL generato automaticamente
- Tema chiaro/scuro
- Schermata dei crediti per provare compassione per lo sviluppatore

---

## Requisiti

- Java 17+
- JavaFX 17+
- Nessuna altra libreria esterna richiesta

---

## Come provare l'applicazione

1. Clonare il repository:
   ```bash
   git clone https://github.com/seven-stars-0/unitracker.git
   ```
2. Aprire il progetto con IntelliJ IDEA o un IDE compatibile

3. Eseguire il main Launcher.java

Un tempo esistevano anche dei **dati_prova.psql** per provare il progetto, ma il mio cane li ha mangiati e quindi non posso includerli.
(Nota: non ho un cane)

---

## PseudoSQL

PseudoSQL è un linguaggio interpretato creato per UniTracker, che permette di:

- Creare, gestire ed eliminare dati, grafici e gruppi
- **Importazione/esportazione** di grafici e gruppi come backup o per trasferire dati su altre macchine
- Gestione di **transazioni periodiche**

Nel caso di esportazione il codice PseudoSQL viene generato automaticamente.
Per un tutorial completo: **Aiuto > PseudoSQL** nell'applicazione.

---

## Crediti

Come testimoniato in **Aiuto > Crediti**, il progetto è stato sviluppato da Malachy Parisi.
L'unica eccezione è rappresentata da eventuali errori nel codice, quelli li ha messi un hacker intento a sabotarmi.

Le icone utilizzate provengono da set a **licenza gratuita** per uso non commerciale e non ho intenzione di assumere il merito della loro creazione.

---

## Possibili estensioni

UniTracker è stato sviluppato come progetto universitario durante **Agosto 2025**; vista la finestra temporale limitata non ho avuto occasione di aggiungere alcune funzionalità:
- Selezione multipla di operazioni
- Mostrare più grafici contemporaneamente
- Fusione di grafici
- Modifica di dati e susseguente estensione del linguaggio PseudoSQL
- Personalizzazione della schermata
- Transazioni periodiche più dettagliate
