# Implementazione: Nicola Graziotin ~ @NicolaGraziotin

Il mio obiettivo principale è stato quello di garantire un'infrastruttura reattiva, robusta e funzionalmente pura per la
gestione delle mosse e del flusso di gioco, mantenendo una netta separazione tra il dominio core e gli strati applicativi
esterni. Ho implementato il GameEngine come una macchina a stati finiti puramente funzionale, disaccoppiata dalla GUI
e dai bot, e ho progettato un pattern di gestione della concorrenza basato su un Event Loop dedicato,
evitando race conditions e side-effect indesiderati.

Nell'ambito dei componenti di [model.basic](#enginemodel), ho implementato in autonomia:
- `cards`, rappresentano il dominio delle carte da gioco: `Card / SpecialCard`.
- `players`, rappresenta il dominio dei giocatori: `Player / Players`.

Successivamente, mi sono occupato della progettazione e dello sviluppo dell'[architettura esagonale](#architettura-esagonale-e-gestione-della-concorrenza), definendo le porte di ingresso/uscita e realizzando gli adattatori per la comunicazione asincrona con la GUI e i bot. Ho inoltre progettato il [contesto applicativo](#application-context) e il [dispatcher degli eventi](#gestione-degli-eventi-lato-client) lato client, garantendo un flusso di dati coerente e thread-safe. Sfruttando la base solida del [GameEngine](#il-gameengine) coordinato da @pallax03, ho integrato la logica funzionale e le regole del gioco Wizard, ottimizzando la gestione degli eventi asincroni per renderla efficiente e facilmente testabile. Infine, con la collaborazione del team, ho contribuito alla realizzazione della GUI in ScalaFX, sviluppando le schermate principali e i relativi controller, e ho finalizzato l'integrazione dell'interfaccia con il modello di eventi asincroni.

I file implementati in collaborazione con gli altri membri del team includono:
- La `GameConfiguration` per l'aggiunta del numero di bots e la loro difficoltà.
- Interfaccia generica di `Event` per la gestione dei flussi di eventi asincroni.
- La gui `application.scalafx`:
  - Implementazione delle pagine grafiche `MainPage` e `GameBoardPage`.
  - Implementazione dei controller `MainPageController` e `GameBoardPageController`.
  - Adattamento a FXML con l'utilizzo di `application.scalafx.managers`.

---

## engine.model

L'obiettivo primario che ha guidato il design di questo modulo è stato il raggiungimento della massima type
safety possibile a tempo di compilazione.

Invece di demandare il controllo di coerenza delle entità a validazioni runtime, il compilatore stesso si fa carico
di garantire la correttezza strutturale del modello. Per raggiungere questo traguardo senza introdurre penalizzazioni
prestazionali, il design si è basato su una combinazione di due potenti costrutti del type system di Scala:
gli **Algebraic Data Types (ADT)** e gli **Opaque Types**.

### Players

Le identità e i concetti fondamentali come `PlayerId`, `PlayerName` e la collezione `Players` sono stati definiti
come `opaque type`. Questo garantisce che a tempo di esecuzione non vi sia _overhead_ (vengono trattati come primitive
o liste standard), ma a tempo di compilazione il type system previene scambi accidentali come ad esempio, passare un `Int` qualsiasi al posto di un `PlayerId`.

```scala
opaque type PlayerId = Int
opaque type PlayerName = String
opaque type Players = List[PlayerId]
// ...
extension (p: PlayerId) infix def plays(c: Card): (PlayerId, Card) = (p, c)
```

L'extension method `plays` consente di associare in maniera fluida un giocatore a una carta, migliorando la leggibilità
del codice.

### Cards

Per quanto riguarda il dominio delle carte (`Card`, `SpecialCard`, `Color`, `Rank`), ho implementato una gerarchia chiusa
tramite **Algebraic Data Types (ADT)**. La modellazione cartesiana (prodotto e somma) si sposa perfettamente con
le regole del gioco Wizard, strutturando in Scala attraverso l'uso congiunto di `sealed trait` ed `enum`.

```scala
sealed trait Card
sealed trait SpecialCard extends Card:
  def id: Int

object Card:
  enum Color:
    case Blue, Green, Red, Yellow
  
  enum Rank(val value: Int):
    case One extends Rank(1)
    case Two extends Rank(2)
  // ...

  final case class Standard(color: Color, rank: Rank) extends Card
  final case class Wizard(id: Int) extends SpecialCard
  final case class Jester(id: Int) extends SpecialCard

  def wizard: Wizard = Wizard(...)
  def jester: Jester = Jester(...)
 
  extension (rank: Rank) infix def of(color: Color): Card = Standard(color, rank)
```

L'extension method `of` abilita l'uso dell'annotazione infix, traducendosi nella capacità di scrivere espressioni
estremamente fluide e vicine al linguaggio naturale (**Domain-Specific Language**):

```scala
val cartaDiGioco = One of Blue
```

Vantaggi di questa modellazione includono:
- **Tipo somma:** la carta `Card` è definita rigorosamente come una disgiunzione esclusiva: può essere una carta standard
(`Standard`) o una carta speciale (`SpecialCard`), a sua volta suddivisa in `Wizard` o `Jester`. Questa struttura chiusa
(`sealed`) permette al compilatore di effettuare il controllo di esaustività (_exhaustiveness checking_) durante le
operazioni di pattern matching.
- **Tipo prodotto:** la carta `Standard` è un tipo prodotto tra `Color` e `Rank`. Entrambi sono modellati come `enum`,
garantendo che l'intervallo di valori ammissibili sia confinato a tempo di compilazione (es. i ranghi sono strettamente
limitati da 1 a 13).
- **Identità delle Carte Speciali:** per differenziare istanze multiple della stessa tipologia di carta speciale
(essendoci più Wizard e Jester nel mazzo), il tipo `SpecialCard` introduce un campo `id: Int`. Questo garantisce la
tracciabilità univoca di ogni singola entità durante le fasi di gioco e di calcolo del punteggio.

---

## Il GameEngine

La responsabilità principale della gestione delle regole di business, dell'invarianza del gioco e delle transizioni
di fase risiede nel componente `GameEngine`. In piena aderenza ai principi del paradigma funzionale (FP) e al fine di
garantire la totale thread-safety in contesti asincroni, l'engine è stato progettato come una **macchina a stati finiti
deterministica e puramente funzionale**. Esso non altera mai uno stato interno globale tramite side-effect o mutazioni
in-place, ma opera come una funzione pura: accetta in input la configurazione corrente del sistema (`GameState`) e un'azione
esterna (`GameAction`), computando e restituendo in modo atomico lo stato e gli eventi successivi.

Per ottimizzare la pipeline di elaborazione e disaccoppiare la logica computazionale pura dagli strati di notifica
periferici (come la GUI in ScalaFX), ho implementato un pattern architetturale strettamente correlato ai principi
dell'**Event Sourcing**. L'astrazione di `GameEngine` è definita nel package `engine.model.core` tramite un `opaque type`
che incapsula una tupla immutabile:

```scala
opaque type GameEngine = (GameState, List[WizardEvent])
```

Per mappare le computazioni fallibili (es. un giocatore che tenta di giocare una carta fuori dal proprio turno o non
conforme alle regole della tavola), l'engine rifiuta l'uso di eccezioni runtime, che violerebbero il principio di
_trasparenza referenziale_. La funzione di transizione principale `processAction` solleva il tipo di ritorno nel contesto
monadico di `Either[GameError, GameEngine]`:

```scala
def processAction(state: GameState, action: GameAction): Either[GameError, GameEngine] =
  (state, action) match
    case (currentState: GameState.Bidding, GameAction.PlaceBid(playerId, bid)) =>
      handlePlaceBid(currentState, playerId, bid)
  // ...
```

Sfruttando il pattern matching tipizzato sulle tuple composte dallo stato corrente e dall'azione, il compilatore valida
le ramificazioni ammissibili. Qualsiasi accoppiamento non previsto dalle regole del gioco (es. il tentativo di piazzare
una scommessa durante la fase di gioco attivo) ricade nel ramo catch-all e produce un fallimento controllato sollevando
un `Left(InvalidAction)`. Questo approccio garantisce la totalità della funzione, rendendo esplicito il flusso d'errore
nel type system.

```scala
private def handlePlaceBid(...): Either[GameError, GameEngine] =
    val totalPlayers = currentState.core.players.totalPlayers

    for
      _ <- currentState.currentPlayer.validateTurnOf(playerId)
      updatedBids <- BiddingRules.processBid(
        bid,
        currentState.currentBids,
        playerId,
        currentState.core.round,
        currentState.core.players.totalPlayers
      )
      bidPlacedEvent = ActionEvent.BidPlaced(playerId, bid)

      finalEngine <-
        if updatedBids.isComplete(totalPlayers) then
          advanceToPlayingPhase(currentState, updatedBids, bidPlacedEvent)
        else advanceToNextBidder(currentState, updatedBids, playerId, bidPlacedEvent)
    yield finalEngine
```

L'uso combinato di `for-comprehension` e `Either` consente di propagare in modo elegante gli errori, interrompendo
la computazione (_short-circuited_) al primo fallimento e restituendo un `Left` con il dettaglio dell'errore.
In caso di successo, viene restituito un `Right` contenente la nuova istanza di `GameEngine` con lo stato aggiornato
e gli eventi generati.

---

## Architettura Esagonale e Gestione della Concorrenza

Il disaccoppiamento tra il core funzionale del dominio (`GameEngine`) e le interfacce esterne
(la GUI sviluppata in ScalaFX ed i Bot intelligenti) è stato formalizzato applicando il pattern architetturale
**Ports and Adapters (Architettura Esagonale)**. Questo approccio garantisce che la logica di business rimanga del tutto
indipendente rispetto ai canali di I/O o ai framework di persistenza e notifica applicati alla periferia del sistema.

### Inbound e Outbound Ports

I confini dell'architettura esagonale sono descritti rigidamente da due `trait` fondamentali all'interno del package `engine.ports`:
1. `WizardInboundPort`: modella i canali d'ingresso primari per stimolare il dominio. Tutte le sue firme sollevano il
tipo di ritorno nel contesto asincrono di un `Future`. Questo impone un contratto non bloccante a qualsiasi client esterno,
garantendo la reattività dei thread chiamanti. Abilita inoltre un pattern di **pubblicazione / sottoscrizione** per i
flussi di eventi tramite i metodi `subscribe[T]` e `unsubscribe`.
2. `WizardOutboundPort`: astrae i canali di uscita attraverso cui l'engine propaga i cambiamenti di stato. Espone le
primitive `publishEvent` e `publishAllEvents` per inviare gli eventi determinati dalle funzioni pure dell'engine verso
il mondo esterno.

### WizardGameAdapter e VertxEventBusAdapter

L'implementazione concreta di queste porte è strutturata nel package `engine.adapters`:
- `VertxEventBusAdapter`: adatta la `WizardOutboundPort` istanziando un meccanismo di messaggistica asincrono basato
sull'EventBus di Vert.x. Per ogni evento generato, il metodo `eventAddresses` calcola dinamicamente le stringhe di
routing tipizzate, distribuendolo in modo non bloccante sul bus locale (`this.vertx.eventBus().publish(address, event)`).
- `WizardGameAdapter`: estende `WizardInboundPort` e agisce come coordinatore dello stato globale mutabile dell'applicazione
(`var currentState: WizardGameState`). Riceve le azioni esterne, interroga l'engine funzionale passando lo stato corrente,
memorizza la nuova configurazione calcolata dall'engine e attiva la propagazione degli eventi verso l'adattatore di output.

### Event Loop

Poiché la UI e molteplici (`Bot`) possono invocare concorrentemente il metodo `submitAction` dell'adattatore,
emerge una problematica cruciale: evitare _race conditions_ sulla variabile globale `currentState` del modulo adapter.
Per risolvere radicalmente la problematica senza ricorrere a costose o rischiose barriere di sincronizzazione,
ho implementato il pattern della **Thread Confinement** sviluppando la classe di utility `VerticleExecutor`.
L'idea cardine è delegare l'esecuzione di qualunque operazione che legga o modifichi lo stato a un unico ed esclusivo
**Event Loop** gestito da **Vert.x**.

Il funzionamento segue un flusso rigoroso:
1. **Sospensione del Task (`runLater`):** il task da eseguire viene incapsulato in un `PendingTask` e registrato
in una mappa concorrente (`TrieMap`) con un identificativo univoco. Il task non viene eseguito immediatamente,
ma viene schedulato per l'esecuzione futura.
2. **Consumo e Dispatch Singolo (`runPendingTask`):** i messaggi vengono processati ed eseguiti in modo sequenziale,
atomico e su un singolo **Event Loop**.
3. **Risoluzione Asincrona:** il risultato del task viene risolto in modo asincrono completando la `Future` associata.

### Application Context

Al fine di evitare l'utilizzo di variabili globali condivise o l'accoppiamento rigido in fase di startup dei moduli
esterni, il livello più esterno dell'applicazione adotta il pattern **Context Object** combinato con una variante del
**Builder Pattern**.

L'interfaccia `WizardApplicationContext` modella in modo centralizzato e immutabile l'aggregato delle dipendenze
architetturali primarie necessarie al corretto funzionamento dell'ecosistema. L'istanza concreta
(`BasicWizardApplicationContext`) è incapsulata privatamente all'interno del companion object, il quale si occupa inoltre
di agganciare il modulo `BotLifecycleManager` fornendogli il contesto asincrono `Vertx` e i relativi canali logici delle porte.

### Bootstrap

Per orchestrare la sequenza di instanziazione, in cui alcuni componenti necessitano dello sblocco asincrono di altri,
ho implementato la classe mutabile di transizione `WizardApplicationContextBuilder`. Questo builder colleziona
progressivamente i moduli di runtime e ne valida la corretta iniezione solo al momento del bootstrap definitivo.

L'oggetto globale `WizardApplication`, che estende il framework `JFXApp3` di ScalaFX, agisce da entry point dell'intero sistema.
La separazione temporale del caricamento delle dipendenze è coordinata in modo lineare:
- **Iniezione Iniziale (`launch`):** il punto d'accesso principale memorizza le istanze attive di `WizardInboundPort`, `WizardAIPort`
e del motore `Vertx` all'interno del builder, avviando subito dopo l'inizializzazione del framework grafico.
- **Configurazione dello Stage (`start`):** All'attivazione del thread grafico (`start`), viene configurata la finestra principale
(`PrimaryStage`) e il suo riferimento viene iniettato nel builder per completare la configurazione delle dipendenze.
- **Risoluzione del Context:** Da questo istante in poi, l'intero contesto viene esposto sotto forma di
`given applicationContext: WizardApplicationContext`. Questo sfrutta le **Using Clauses** (_Context Parameters_)
di Scala per iniettare le dipendenze in modo pulito e sicuro a tutte le pagine grafiche dell'applicazione. Tale approccio
garantisce un'elevata modularità e predisposizione in modo ottimale il sistema di test di integrazione automatizzati,
abilitando il mocking programmabile delle porte.

### Gestione degli Eventi Lato Client

Per garantire il completo disaccoppiamento tra il flusso asincrono degli eventi distribuiti dall'infrastruttura
sottostante e la manipolazione dello stato grafico, l'architettura client introduce la classe `GameBoardEventDispatcher`.
Questo componente funge da mediatore all'interno del sottosistema della UI, isolando la `GameBoardView` dalle logiche
di sottoscrizione ed esecuzione asincrona dei messaggi, integrando un utility `PresentationQueue` sviluppato da @pallax03
in grado di garantire animazioni e una coda per gli eventi.

Il dispatcher si interfaccia con il mondo esterno sfruttando i meccanismi di pubblicazione / sottoscrizione definiti
dall'astrazione dell'Inbound Port. All'attivazione del ciclo di ascolto (`startListening`), il dispatcher si registra per
ricevere tutti i flussi di tipo `WizardEvent` generati dal nucleo applicativo. Le chiavi identificative ritornate dalla
sottoscrizione (`subscriptionIds`) vengono memorizzate localmente per consentire una rimozione pulita e priva di memory
leak nel momento in cui la vista viene disattivata (`stopListening`).

---

[Back to index](../../../../index.md) |
[Previous Chapter](../index.md) |
[Alex Mazzoni](../alex/index.md) |
[Francesco Marcatelli](../francesco/index.md) |
[Next Chapter](../../6-testing.md)