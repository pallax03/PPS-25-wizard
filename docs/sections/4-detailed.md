# Design di dettaglio

Come spiegato precedentemente, il sistema rappresentato in figura è suddiviso in due moduli:

- Engine Module: contiene l'implementazione del servizio relativo all'engine.
- Application Module: contiene l'implementazione dell'applicazione.

## Tecnologie utilizzate

Oltre a quelle obbligatorie citate nei requisiti, sono state scelte ulteriori tecnologie per scopi ben precisi:

- **_Vert.x_**: toolkit utilizzato allo scopo di rendere l'engine reattivo e asincrono.
- _**ScalaFX**_: framework d'interfaccia grafica per sviluppare schermate in maniera facile, grazie all'utilizzo di
  _FXML_.
- **_TuProlog_**: framework per la programmazione logica, al fine d'implementare alcune regole tramite _Prolog_ e
  utilizzarle all'interno dell'applicativo.

Inoltre, altre due tecnologie sono state utilizzate per effettuare il _testing_ del sistema:

- **_ScalaTest_**: tool per lo sviluppo di test idiomatico in _Scala_.
- **_ScalaFmt_**: sistema lo stile visivo del codice.
- **_ScalaFix_**: è uno strumento di refactoring e linting per il codice in _Scala_.

Infine, grazie alla **_SCoverage_** è possibile conoscere in maniera approssimativa la quantità di codice verificato.\
Ovviamente questo non dà una certezza assoluta di mancanza di _bug_, però permette di essere relativamente sicuri
sul codice coperto dalla _coverage_.\
Volontariamente, e per mancanze di tempistiche, non sono stati sviluppati test per l'interfaccia grafica.

## Engine Module

Il servizio è stato realizzato attraverso la libreria _Vertx_, che facilita la creazione di sistemi
reattivi a certi eventi.

![Engine Package Diagram](/docs/diagrams/engine_package.webp)

### WizardGameAdapter & GameEngine



#### Ottenimento dello stato del gioco corrente

In ogni momento, è possibile ottenere lo stato della partita modellato dalla enumerazione `WizardGameState`, che
definisce i valori `NotConfigured`, `Running` e `Finished` come possibili stati della partita, rispecchiando quelli
identificati durante la fase di design architetturale.

Nel caso in cui la partita sia configurata e in esecuzione (`Running`), il suo stato comprende anche un `GameState` che
permette di accedere alle seguenti informazioni a seconda della specifica fase di gioco attiva:

* **ChoosingTrump (Scelta del Trionfo):** Rappresenta lo stato in cui il gioco attende la definizione del seme di
  trionfo per il round corrente. Contiene il `CoreState` con i dati fondamentali di gioco.
* **Bidding (Fase di Scommessa):** Rappresenta lo stato in cui i giocatori dichiarano il numero di prese che prevedono
  di effettuare. Permette di accedere al `CoreState`, alle scommesse correntemente effettuate (`Bids`) e
  all'identificativo del giocatore di turno (`currentPlayer`).
* **Playing (Fase di Gioco):** Rappresenta lo stato in cui i giocatori calano le proprie carte per completare le prese.
  Permette di accedere al `CoreState`, alle scommesse registrate (`Bids`), alle carte attualmente giocate sul tavolo (
  `Table`), al giocatore attivo per il turno corrente (`currentPlayerTurn`) e al conteggio delle prese vinte dai
  partecipanti (`Tricks`).
* **Ended (Partita Terminata):** Rappresenta lo stato in cui la partita si è conclusa. Consente di accedere all'elenco
  dei giocatori (`Players`) e al tabellone finale con tutti i punteggi accumulati (`Scoreboard`).

#### Sottoscrizione agli eventi

In ogni momento, è possibile sottoscriversi ai diversi eventi dell’engine, ricevendoli quando sono generati e quindi
potendo reagire di conseguenza. I tipi di eventi che vengono generati estendono la classe WizardEvent e sono:

##### Lifecycle Events (Eventi del Ciclo di Vita del Gioco)

* **GameStarted:** Evento generato all'avvio del match. Esso contiene l'elenco finale dei giocatori (inclusi i bot) e il
  livello di difficoltà degli avversari virtuali.
* **GameEnded:** Evento generato quando la partita si conclude. Esso racchiude il tabellone dei punteggi finali e
  l'elenco dei partecipanti.

##### Progress Events (Eventi di Avanzamento del Turno e del Gioco)

* **CardsDealt:** Evento generato all'inizio di ciascun round. Esso racchiude l'identificativo del destinatario, le
  carte specifiche assegnate alla sua mano (garantendo la privacy del giocatore) e la carta estratta come trionfo.
* **TrickWon:** Evento generato quando una presa viene assegnata. Esso contiene il vincitore della presa, il numero
  aggiornato di prese da lui effettuate e l'elenco delle carte rimosse dal tavolo.
* **RoundScored:** Evento generato alla chiusura di un round. Esso contiene il tabellone aggiornato con i punteggi
  parziali e totali della partita.
* **PhaseChanged:** Evento generato al cambio di fase di gioco. Esso informa della nuova fase di gioco attiva (ad
  esempio "Bidding" o "Playing").

##### Action Events (Eventi di Azione dei Giocatori)

* **BidPlaced:** Evento generato alla conferma di una scommessa. Esso contiene l'ID del giocatore e il valore della
  scommessa effettuata.
* **CardPlayed:** Evento generato quando viene giocata una carta. Esso contiene l'ID e il nome del giocatore, la carta
  giocata, l'eventuale carta temporaneamente vincente e il seme di piombo corrente.
* **TrumpColorResolved:** Evento generato alla risoluzione del seme di trionfo. Esso contiene l'ID del giocatore che ha
  effettuato la scelta e il colore selezionato.

##### Invitation Events (Eventi di Sollecito Input)

* **WaitingForBid:** Evento generato per sollecitare la scommessa. Esso contiene l'ID del giocatore e i dati del round
  corrente.
* **WaitingForCard:** Evento generato per sollecitare la giocata. Esso contiene l'ID del giocatore e la lista delle
  carte legalmente giocabili dalla sua mano.
* **WaitingForTrump:** Evento generato per sollecitare la scelta del seme di trionfo. Esso contiene l'ID del giocatore
  designato.

##### Failure Events (Eventi di Errore)

* **ActionFailed:** Evento generato in caso di errore di gioco. Esso contiene l'ID del giocatore e il dettaglio dell'
  errore riscontrato.

Un esempio di sottoscrizione agli eventi dell’engine è mostrato nel seguente diagramma degli stati:

![Publish-Subscribe Architecture](/docs/diagrams/pub-sub-diagram.webp)

### Prolog Module

Per agevolare la testabilità delle query, le teorie sono strutturate in un unico file sorgente, 
ma i commenti e l'indentazione delineano la separazione logica dei moduli e la loro gerarchia di dipendenze.

![Prolog Package Diagram](/docs/diagrams/prolog.webp)

- **Utils**: predicati di base utilizzati da tutti gli altri packages, funge da supporto alla libreria standard.
- **Engine_Basic**: Definisce le teorie di base del gioco di Wizard.
- **Engine_Rules**: Implementa le regole rigide del gioco.
- **Strategy_Helper**: Presenta varie utility per il calcolo delle strategie calcolando informazioni aggiuntive derivate dalle regole del gioco e dalle teorie di base.
- **Strategy**: Descrive le tattiche di gioco utilizzate nelle API finali.
- **Wizard_API**: Interfaccia pubblica, espone i predicati che l'applicazione chiamante dovrà utilizzare per interagire con l'intelligenza.

---

## Application Module

L’applicazione è stata realizzata utilizzando la libreria ScalaFX, la quale è un wrapper di JavaFX che permette di
realizzare semplici interfacce grafiche molto velocemente, sfruttando la dichiaratività di Scala.

![Application Package Diagram](/docs/diagrams/application_package.webp)

---

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/3-architectural.md) |
[Next Chapter](/docs/sections/5-implementation/index.md)
