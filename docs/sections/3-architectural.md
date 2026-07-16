# Design Architetturale

Per quanto riguarda il design architetturale del sistema, si identificano due macro-componenti principali, i quali
vengono suddivisi a loro volta in altri sotto-componenti:

- *Engine*: gestisce la creazione e lo sviluppo di una partita di wizard. E stato realizzato come servizio, seguendo
  un'archietttura di tipo esagonale, anche conosciuta come *ports & adapters*, allo scopo di facilitarne la composizione
  con altri servizi. In particolare, l'architettura prevede i seguenti componenti:

    - *Model*: definisce la *business logic* del sistema.
    - *Port*: espone le funzionalità del modello in rapporto a uno specifico caso d'uso del servizio.
    - *Adapter*: permette l'interazione con una specifica porta attraverso una specifica tecnologia.

- *Application*: gestisce l'interazione dell'utente col servizio. In particolare, è composto da:

    - *Proxy*: media l'interazione tra l'applicazione e l'engine al fine di proteggere l'utilizzatore dai cambiamenti del servizio.
    - *ViewController*: gestisce l'interazione tra utente e l'applicazione.

Di seguito, si riporta l'architettura del sistema in cui l'engine può includere modelli, porte e adapter diversi.

![image](../images/hexagonal-diagram.png)

![image](../images/pub-sub-diagram.png)

## Engine

Per esplicitare i vari casi d'uso, il contratto del servizio prevede le seguenti funzionalità:

- *Ottenimento dello stato del servizio*: funzionalità che permette di ottenere lo stato corrente della partita, il
  quale include le carte in mano al giocatore, le carte attualmente sul tavolo (la manche corrente), la carta del
  briscola (se presente), il round attuale, il turno del giocatore corrente, i punteggi e le scommesse/previsioni di
  tutti i giocatori.

- *Avvio della partita*: funzionalità che, a partire da una data configurazione (es. numero di giocatori, nomi dei
  partecipanti, eventuale presenza di bot), permette di inizializzare il mazzo, distribuire le carte per il round
  corrente e avviare la partita.

- *Ottenimento delle carte giocabili*: funzionalità che, a partire dalle carte presenti nella mano del giocatore di
  turno e dalla prima carta giocata nella manche attuale (che determina il seme di piombo), restituisce l'elenco delle
  carte che il giocatore è legalmente autorizzato a calare (rispettando l'obbligo di rispondere al seme, salvo l'uso di
  Wizard o Fool).

- *Scommessa sulle mani (Previsione)*: funzionalità che permette a un giocatore, all'inizio di ogni round e dopo aver
  visto le proprie carte e il briscola, di dichiarare il numero esatto di prese (tricks) che prevede di vincere in quel
  round.

- *Applicazione di una mossa (Giocata carta)*: funzionalità che applica la giocata di una specifica carta dalla mano del
  giocatore al tavolo, verificandone la validità. Se la giocata conclude la manche, il servizio calcola il vincitore
  della presa, gli assegna il punto e aggiorna il turno per la manche successiva.

- *Calcolo e aggiornamento del punteggio*: funzionalità che, alla fine di un round (quando tutte le carte distribuite
  sono state giocate), confronta le prese effettuate da ogni giocatore con le rispettive scommesse iniziali, calcola i
  punteggi positivi o negativi accumulati e, se si è raggiunto l'ultimo round, decreta il vincitore della partita.

## Eventi di Gioco

In seguito all'applicazione delle varie funzionalità vengono creati determinati eventi, ai quali è possibile
sottoscriversi. Gli eventi sono suddivisi nelle seguenti categorie:

#### Lifecycle Events (Eventi del Ciclo di Vita del Gioco)

* **GameStarted:** Evento generato all'avvio della partita. Esso viene lanciato nel momento in cui il gioco
  viene inizializzato con la lista completa dei partecipanti (giocatori reali e bot) e la relativa
  configurazione.
* **GameEnded:** Evento generato per avvisare che la partita è terminata definitivamente. Esso viene emesso
  quando si conclude l'ultimo round di gioco e vengono decretati i punteggi finali, oppure nel momento in cui la partita
  viene interrotta bruscamente.

#### Progress Events (Eventi di Avanzamento del Turno e del Gioco)

* **CardsDealt:** Evento generato per avvisare che è iniziato un nuovo round di gioco. Viene emesso subito dopo
  che il mazzo è stato rimescolato e le carte sono state distribuite a ciascun giocatore.
* **IsTurnOf:** Evento generato per segnalare che la priorità di gioco è passata a un partecipante. Viene
  lanciato ogni volta che un giocatore termina la propria azione e tocca al giocatore successivo agire.
* **TrickWon:** Evento generato per segnalare la conclusione di una presa (manche) all'interno del round. Viene
  emesso nel momento in cui tutti i giocatori hanno calato la propria carta sul tavolo e viene calcolato il vincitore
  della presa.
* **RoundScored:** Evento generato per notificare la fine di un intero round. Viene lanciato dopo che tutte le
  carte in mano ai giocatori sono state giocate e si rende necessario il calcolo dei punteggi parziali.
* **PhaseChanged:** Evento generato per segnalare il passaggio a una fase successiva del round. Viene emesso
  nel momento in cui si conclude una fase (ad esempio le scommesse) e inizia la successiva (il gioco delle
  carte).

#### Action Events (Eventi di Azione dei Giocatori)

* **BidPlaced:** Evento generato per segnalare che una scommessa è stata registrata. Viene emesso ogni volta
  che un singolo giocatore conferma la propria previsione sulle prese da effettuare.
* **CardPlayed:** Evento generato per avvisare che la situazione sul tavolo è cambiata. Viene lanciato
  ogniqualvolta un giocatore cala una carta valida dalla propria mano.
* **TrumpColorResolved:** Evento generato per segnalare la definizione del colore di trionfo del round. Viene
  lanciato quando la carta di trionfo estratta richiede la scelta esplicita di un colore da parte di un giocatore (ad
  esempio con un Wizard) e questa viene risolta.

#### Invitation Events (Eventi di Sollecito Input)

* **WaitingForBid:** Evento generato per sollecitare l'inserimento di una scommessa. Viene emesso all'inizio
  della fase di scommessa per ciascun giocatore, quando il sistema attende la sua previsione.
* **WaitingForCard:** Evento generato per sollecitare la giocata di una carta. Viene lanciato durante la fase
  di gioco quando tocca a un determinato partecipante calare la propria carta sul tavolo.
* **WaitingForTrump:** Evento generato per richiedere la scelta del colore di trionfo. Viene lanciato nel caso
  in cui la carta scoperta sia un Wizard e il giocatore di turno debba dichiarare il seme dominante per quel
  round.

#### Failure Events (Eventi di Errore)

* **ActionFailed:** Evento generato in situazioni di errore durante il flusso di gioco. Viene emesso ogni volta che un
  giocatore tenta di effettuare una giocata o una scommessa non consentita dalle regole.

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/2-requirements.md) |
[Next Chapter](/docs/sections/4-detailed.md)
