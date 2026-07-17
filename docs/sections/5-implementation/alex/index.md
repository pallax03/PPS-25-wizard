

Sin dall'inizio del progetto, mi sono occupato del coordinamento, della suddivisione e della revisione dell'architettura della Macchina a Stati Principale (FSM) del GameEngine.
Ho seguito con precisione l'inizializzazione della Continuous Integration e ho implementato, seguendo la metodologia TDD, i primi modelli di base propedeutici all'integrazione finale della FSM.

Nell'ambito dei componenti di [basic](#enginemodelbasic), ho implementato in autonomia: 
- [`cards`](#enginemodelbasiccards): [`Deck`](#deck), [`Hand / Hands`](#hand--hands)
- [`gameplay`](#enginemodelbasicgameplay): [`Table`](#table), [`Trump`](#trump)

Successivamente ho coordinato e revisionato la sezione relativa alle [rules](#enginemodelrules), implementando nello specifico: [`TableRules`](#tablerules)

La mia successiva task era quella della totale [implementazione dei bot](#bot), a partire dal [prolog](#prolog) fino alla totale implementazione delle hint per i giocatori, come ho detto precedentemente ho coordinato, 
l'architettura del GameEngine anche per garantire disaccoppiamento tra i player e i bot, in modo che potesse agevolarmi nella creazione di una singola api utilizzata da entrambi esternamente dal GameEngine.  
Grazie all'architettura finale dell'intero progetto, implementata da [@NicolaGraziotin](/docs/sections/5-implementation/nicola/index.md), il disaccopiamento tra i due componenti attivi è stato immediato.
Mi ha permesso di riutilizzare la sua architettura, imparando e studiando il concetto di ports-and-adapters, i bot e i giocatori riescono ad interagire con la stessa API per ottenere lo stesso tipo di risultato.

Il mio compito successivo ha riguardato l'intera [implementazione dei bot](#bot), a partire dall'integrazione con [prolog](#prolog) fino alla realizzazione dei [suggerimenti (hint) per i giocatori](#...).
Come anticipato, ho coordinato l'architettura del GameEngine con l'obiettivo di garantire un netto disaccoppiamento tra player e bot, facilitando così la creazione di un'unica API esterna al GameEngine utilizzata da entrambi.
Grazie all'architettura finale dell'intero progetto, implementata da [@NicolaGraziotin](/docs/sections/5-implementation/nicola/index.md), il disaccoppiamento tra i due componenti attivi è risultato immediato. 
Questo approccio mi ha permesso di valorizzare la sua struttura, approfondendo e applicando il pattern _ports-and-adapters_; 
in questo modo, sia i bot sia i giocatori reali riescono a interagire con la medesima API per ottenere lo stesso tipo di risultato.

## engine.model.basic

La prima parte dell'implementazione ha riguardato la costruzione di un modello di dominio sufficientemente espressivo
per rappresentare le entità fondamentali di Wizard senza esporre direttamente collezioni o primitive non tipizzate.

Per questo motivo diversi concetti sono stati modellati con `opaque type`, companion object ed extension methods.

### engine.model.basic.cards

Questo sotto-package definisce il dominio relativo alle carte, raggruppando tutto ciò che è necessario alla loro gestione all'interno del gioco.

#### Deck

È stato pensato sin da subito come un `opaque type` basato su `List[Card]`. Il companion object espone due factory principali:
- `Deck.create`, che crea e mescola un mazzo standard da 60 carte.
- `Deck.create(cards)`, che crea un mazzo custom rimuovendo eventuali duplicati.

L'implementazione del deck ha posto subito una sfida architetturale nel passaggio dal paradigma OOP a quello funzionale: 
come è possibile distribuire le carte a più giocatori senza mutare lo stato globale del mazzo (evitando i side-effect)?

L'operazione più rilevante, `Deck.pop(n)`, è stata risolta adottando `cats.data.State[Deck, List[Card]]`. 
Questa soluzione ha permesso di modellare la distribuzione delle carte in maniera completamente funzionale.

Prima di consolidare l'implementazione, ho esplorato come l'API pubblica potesse essere utilizzata.
I seguenti casi d'uso hanno confermato l'efficacia di questa soluzione.

```scala
for
    h1 <- Deck.pop(10)
    h2 <- Deck.pop(5)
    h3 <- Deck.pop(20)
    trumpList <- Deck.pop(1)
  yield (List(h1, h2, h3), trumpList.headOption)
  
val (remainingDeck, (hands, trump)) = dealStatic(Round.start).run(Deck()).value
```
Il primo aspetto fondamentale è l'assoluta mancanza di side-effect: grazie all'utilizzo della monade **State** di **Cats**, 
il riferimento al mazzo aggiornato viene passato implicitamente dietro le quinte ad ogni step della `for-comprehension`, senza bisogno di riassegnare variabili.

```scala
val players = List(Player("Alice"), Player("Bob"), Player("Charlie"))

for
    handsList <- players.traverse(p => Deck.pop(ROUND).map(cards => p -> cards))
    trumpList <- Deck.pop(1)
  yield (handsList.toMap, trumpList.headOption)

val (deckAfterDeal, (playerHands, trumpCard)) = dealDynamic(players, ROUND).run(Deck()).value
```

Questa seconda implementazione dimostra come gestire in modo elegante un numero variabile di giocatori. 
Tramite l'uso di `traverse`, viene applicata l'operazione con stato (`Deck.pop`) a ogni elemento della lista `players`. 
I risultati vengono accumulati restituendo le mani dei giocatori, pur mantenendo intatto il "filo" dello stato globale del mazzo che scorre da un giocatore all'altro.

Come possiamo notare, entrambe le implementazioni sfruttano la natura monadica di State. 
Questa astrazione porta con sé un ulteriore, enorme vantaggio pratico: la gestione intrinsecamente sicura dell'esaurimento delle carte. 
Se si richiedono carte da un mazzo ormai vuoto, non vengono sollevate eccezioni; 
l'operazione estrae semplicemente le carte effettivamente disponibili (o una lista vuota), propagando coerentemente lo stato.

In un gioco come Wizard questo dettaglio è cruciale: 
nell'ultimo round le carte vengono esaurite completamente per comporre le mani dei giocatori, lasciando di fatto il mazzo vuoto. 
Di conseguenza, l'ultima estrazione per definire la Trump card fallirà in modo sicuro e puramente funzionale,
restituendo un `None` senza interrompere il flusso del programma (questa difatti è anche la base di come sono state gestite le [`Trump`](#trump)).


---

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/5-implementation/index.md) |
[Nicola Graziotin](/docs/sections/5-implementation/nicola/index.md) |
[Francesco Marcatelli](/docs/sections/5-implementation/francesco/index.md) |
[Next Chapter](/docs/sections/6-testing.md)
