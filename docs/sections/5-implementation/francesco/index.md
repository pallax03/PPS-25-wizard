# Implementazione: Francesco Marcatelli ~ @framarcaa

Il mio contributo fondamentale all'interno del progetto si è concentrato sulla formalizzazione e sull'implementazione
delle regole di business e delle strutture informative necessarie a governare il ciclo di vita dei round, la gestione
dei turni e i meccanismi di punteggio del gioco. Lavorando in stretta sinergia con il nucleo puro del
`GameEngine`, mi sono occupato di delineare un modello funzionale, robusto ed esente da side-effect per
modellare la contabilità interna del gioco (punteggi e scommesse) e per governare in sicurezza le
transizioni di fase all'interno del tavolo da gioco.

Nell'ambito dei componenti di `model.basic`, ho implementato in autonomia:

* `gameplay`: ridefinizione del concetto temporale di avanzamento del gioco tramite `Round`.
* `bidding`: modellazione dei vincoli e delle collezioni dedicati alle scommesse (`Bid` / `Bids`) e alle prese
  effettuate (`Trick` / `Tricks`).
* `scoreboard`: progettazione dello storico centralizzato e delle strutture tabellari per la persistenza in-memory dei
  risultati (`Scoreboard` / `RoundRow`).

Successivamente ho coordinato e implementato l'intera sezione relativa alle regole logiche e di ciclo del gioco in
`model.rules`, sviluppando nello specifico:

* `BiddingRules`: validazione algoritmica delle scommesse.
* `ScoringRules`: calcolo matematico dei punteggi incrementali o decrementali a fine round.
* `RoundManager`: orchestrazione dello stato di distribuzione delle carte, calcolo dei turni successivi ed evoluzione
  complessiva della partita.

I file invece implementati in collaborazione con gli altri membri del team includono:

* Integrazione di `GameState` e `CoreState` nel ciclo del round: Estensione delle strutture core per supportare le
  transizioni monadiche di inizializzazione e distribuzione eseguite dal mio `RoundManager`.
* La gui `application.scalafx`:
    - Implementazione delle pagine grafiche `MainPage` e `ScoreboardPage`.
    - Implementazione dei controller `ScoreboardPageController` e `GameBoardPageController`.

---

## engine.model (Bidding & Scoreboard)

In linea con la filosofia di massimizzare il controllo statico dei tipi a tempo di compilazione, ho strutturato
le entità legate a scommesse, turni e punteggi sfruttando estensivamente gli `opaque type` e gli *extension methods* di
Scala 3.

### Round e Bidding

Il concetto di `Round`, `Bid` (la scommessa) e `Trick` (la presa) sono intrinsecamente legati a dei valori numerici
interi, ma trattarli come semplici `Int` avrebbe esposto l'applicazione a scambi accidentali di parametri. Per
questa ragione sono stati incapsulati in tipi opachi:

```scala
opaque type Round = Int
opaque type Bid = Int
opaque type Bids = Map[PlayerId, Bid]
opaque type Tricks = Map[PlayerId, Trick]
```

## Scoreboard e RoundRow

La persistenza e la rappresentazione dello storico di una partita sono state formalizzate nell'astrazione `Scoreboard`,
definita come una doppia mappa immutabile che associa a ogni giocatore la cronologia dei suoi punteggi (`Score`) e delle
sue scommesse (`Bid`) round per round:

```scala
opaque type Scoreboard = Map[PlayerId, Map[Round, (Score, Bid)]]
```

Per disaccoppiare questa struttura puramente computazionale dalle esigenze di visualizzazione tabellare della GUI, ho
introdotto il costrutto `RoundRow`. Questo componente agisce come un vero e proprio "proiettore" dello stato immutabile:
estrae i dati storici aggregandoli in righe orizzontali adatte a essere visualizzate in una griglia, calcolando
preventivamente il numero massimo di round giocabili in funzione del numero di partecipanti (`totalDeckCards` /
`numPlayers`) ed
evitando la propagazione di valori nulli grazie all'uso combinato di Option e trasformazioni funzionali.

## model.rules: Logica di Business Funzionale

Le regole applicative di Wizard richiedono validazioni stringenti in base alla fase corrente della partita. Tutta questa
logica è stata isolata in moduli interamente puri e stateless all'interno del package

### BiddingRules e il Vincolo dell'Ultimo Giocatore

La fase di scommessa è regolata da limiti statici (un giocatore non può scommettere più carte di quante ne abbia in mano
nel round corrente) e da vincoli dinamici legati all'ordine di turno. In particolare, l'ultimo giocatore della rotazione
non può effettuare una scommessa che, sommata alle precedenti, renda la somma totale dei bid esattamente uguale al
numero del round corrente (regola che costringe matematicamente almeno un giocatore a fallire l'obiettivo).

```scala
def processBid(bid: Bid, currentBids: Bids, currentPlayer: PlayerId, round: Round, totalPlayers: Int): Either[GameError, Bids] =
  bid.validateBid(round, currentBids, totalPlayers)
    .map(_ => currentBids + (currentPlayer -> bid))
```

La scelta di sollevare il ciclo di validazione nel contesto monadico di `Either` risponde alla necessità di ripudiare
l'uso di eccezioni a runtime, salvaguardando il principio di trasparenza referenziale del motore di gioco.
Gestire gli errori tramite i tipi (`Left` e `Right`) costringe il sistema, già a tempo di compilazione, a farsi carico
esplicitamente sia dello scenario di successo che del fallimento controllato.

Questo approccio si rivela fondamentale per astrarre la logica di business: anziché interrompere bruscamente
l'esecuzione del programma in caso di scommessa non valida (come una violazione dei confini numerici o del vincolo
sull'ultimo giocatore), l'invariante di gioco produce un valore d'errore tipizzato e puro. Ciò permette ai
moduli periferici (come l'Event Loop dell'architettura esagonale o l'intelligenza dei Bot) di intercettare il rifiuto e
reagire in modo reattivo ed elastico, ad esempio richiedendo una nuova mossa senza mai compromettere la stabilità
globale dello stato dell'applicazione.

### ScoringRules e il Calcolo dei Punteggi

Al termine di ciascun round, la funzione pura compute all'interno di `ScoringRules` si fa carico di aggiornare
massivamente lo stato della Scoreboard. Sfruttando il costrutto funzionale `foldLeft` sulla lista dei giocatori attivi,
il
calcolo accumula i punteggi in modo puramente deterministico:

```scala
players.toList.foldLeft(scoreboard): (sb, player) =>
  val bid = bids(player.id)
  val tricksWon = tricks(player.id)
  val roundPoints = bid.calculatePointsFor(tricksWon)
  // ...
  sb.addScore(player.id, round, cumulativePoints, bid)
```

Il calcolo analitico del punteggio è demandato all'estensione `calculatePointsFor` che implementa le regole ufficiali:
se
il giocatore soddisfa esattamente la propria scommessa, gli vengono assegnati i punti che gli spettano; in caso
contrario, subisce una penalità proporzionale all'errore commesso.

## Gestione del Ciclo del Round (RoundManager)

Il fulcro del coordinamento delle transizioni tra round è rappresentato dal modulo `RoundManager`, il quale fonde
insieme
il calcolo algoritmico dei turni e la manipolazione dello stato del mazzo di carte attraverso la monade `State` di
`Cats`.

### Calcolo dei Turni e Rotazione del Primo Giocatore

La determinazione del flusso dei turni all'interno di una partita di Wizard è dinamica. L'estensione `nextAfter`
implementa la ricerca dell'indice del giocatore corrente all'interno della collezione circolare dei partecipanti:

```scala
def nextAfter(current: PlayerId): Either[GameError, PlayerId] =
  val idx = players.toList.indexWhere(_.id == current)
  Either.cond(idx >= 0, players.toList((idx + 1) % players.totalPlayers).id, GameError.NotYourTurn)
```

Inoltre, per garantire l'equità del gioco, il primo giocatore chiamato a scommettere e a giocare all'inizio di ogni
round deve ruotare in senso orario rispetto al mazziere. La funzione `firstPlayer` calcola deterministicamente questo
identificativo.

### Inizializzazione del Round tramite la Monade State

La sfida più complessa risiede nella funzione initialize, il cui compito è consumare il mazzo di carte immutabile (`
Deck`), distribuire le mani corrette a ciascun giocatore in base al round corrente, determinare l'eventuale carta
`Trump` (
briscola) e restituire il corretto `GameState` iniziale. Per fare ciò senza rompere la trasparenza referenziale ed
evitando variabili globali mutate, l'intera pipeline è stata modellata come una transizione di stato monadica operante
sul contesto di `CoreState`:

```scala
def initialize(deck: Deck): State[CoreState, GameState] =
  for
    core <- State.get[CoreState]
    (hands, optionTrump) = round.deal(core.players).runA(deck).value
    firstPlayer = round.firstPlayer(core.players)
    newCore = core.copy(hands = hands, trump = optionTrump.asTrump)
    _ <- State.set(newCore)
  yield
    val isUnresolved: Boolean = newCore.trump match
    case Trump.WizardUnresolved(c)
    => true
    case _ => false
    
    if isUnresolved then GameState.ChoosingTrump(newCore)
    then GameState.ChoosingTrump(newCore)
    else GameState.Bidding(core = newCore, currentBids = Bids.empty, currentPlayer = firstPlayer)
```

Il processo si articola in modo lineare e sicuro per rispondere a precise necessità di business e architetturali:

1. **Garanzia di consistenza iniziale**: Prima di poter operare qualsiasi transizione, il sistema deve basarsi su
   un'istantanea immutabile e coerente del contesto di gioco. Questo approccio previene alla radice la possibilità che
   letture concorrenti o concorrenza spuria legata all'I/O possano operare su dati obsoleti o parziali.
2. **Isolamento e determinismo della distribuzione**: L'estrazione simultanea delle mani e della briscola è concepita
   per blindare l'integrità del mazzo. Poiché il gioco non ammette anomalie nella composizione delle carte e richiede un
   tracciamento matematico esatto di ogni entità, la linearità di questo blocco logico assicura che nessuna carta venga
   duplicata o persa, traducendo le regole distributive in passaggi atomici privi di effetti collaterali.
3. **Preservazione della Thread-Safety**: La creazione di un nuovo contesto immutabile risponde alla necessità di
   garantire la totale thread-safety dell'applicazione. Invece di modificare lo stato in-place, operazione
   rischiosa in contesti reattivi asincroni, la generazione di una nuova istanza pulita permette di propagare i
   cambiamenti in modo sicuro a tutti i componenti periferici senza ricorrere a costosi blocchi di
   sincronizzazione.
4. **Biforcazione deterministica del flusso di gioco**: La scelta di valutare immediatamente la natura della briscola
   serve a vincolare l'esperienza utente e la logica del motore al rispetto delle regole di Wizard.
   Intercettare tempestivamente una transizione speciale (la necessità di scegliere un colore per il Wizard) rispetto al
   flusso ordinario (la fase di scommessa) impedisce al sistema di entrare in stati inconsistenti o illegali,
   instradando la partita verso il corretto stato di avanzamento e abilitando in modo sicuro e tempestivo le sole mosse
   concesse in quella specifica frazione di gioco.

---

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/5-implementation/index.md) |
[Alex Mazzoni](/docs/sections/5-implementation/alex/index.md) |
[Nicola Graziotin](/docs/sections/5-implementation/nicola/index.md) |
[Next Chapter](/docs/sections/6-testing.md)