# Requisiti di sistema

Durante l'analisi del problema sono stati identificati i seguenti requisiti del sistema da realizzare.

## Business

- Creare un sistema in grado di poter effettuare una partita di Wizard, completa di tutte le regole del gioco originale.
- Permette di giocare una partita in single player contro due o più avversari controllati da un computer (minimo giocatori da 3 a 6).

## Utente
- Gli utenti dovranno interagire con il sistema attraverso un'interfaccia grafica (GUI).
- Gli utenti possono visualizzare i diversi componenti che rappresentano lo stato della partita in qualsiasi momento:
  - mostrare la configurazione attuale della partita
  - la fase del round
  - le carte in mano
  - le carte giocate
  - i punteggi totali
  - le previsioni delle prese di tutti i giocatori
  - le prese fatte da tutti i giocatori
  - la briscola del round
  - il turno corrente
- Gli utenti possono interagire con il sistema attraverso l'interfaccia grafica (GUI) per:
  - iniziare una nuova partita
  - scegliere il numero di bots (da 2 a 5) e la relativa difficoltà
  - effettuare le previsioni delle prese
  - giocare le carte in mano
  - scegliere la briscola del round

## Funzionali
- La configurazione della partita deve essere personalizzabile, permettendo di scegliere il numero di bot (da 2 a 5).
- Il gioco si svolge in maniera interattiva, facendo eseguire ai giocatori le azioni previste dalle regole del gioco originale.
- Il campo da gioco deve essere aggiornato dinamicamente in base alle azioni di tutti i giocatori.
- A ogni giocatore devono essere assegnate carte in mano all'inizio di ogni round, con un numero di carte che aumenta a ogni round (1 carta al primo round, 2 carte al secondo round, e così via).
- Le carte sono suddivise in:
  - carte numeriche (da 1 a 13) di quattro colori (blu, giallo, rosso, verde)
  - 8 carte speciali: 4 Wizard (che contano come dei 14) e 4 Jester (contano come degli 0). 
- Ogni giocatore deve poter effettuare una mossa legale durante il proprio turno, rispettando le regole del gioco originale:
  - Durante la fase di previsione, ogni giocatore deve poter fare una previsione delle prese che intende fare durante il round in modo che la somma totale delle previsioni non sia uguale al numero di prese totali del round (si applica solo al'ultimo giocatore).
  - Durante la fase di gioco, ogni giocatore deve poter giocare una carta in mano, rispettando le regole:
    - Se il giocatore ha carte dello stesso colore della prima carta giocata nel round (following color), è forzato a giocare una carta di quel colore (o una qualsiasi carta speciale).
    - Se il giocatore non ha carte dello stesso colore della prima carta giocata nel round, può giocare qualsiasi carta in mano.
    - Le carte Wizard battono tutte le altre carte, il primo giocatore che lo gioca vince la mano, indipendentemente dalle altre carte giocate.
    - Le carte Jester perdono contro tutte le altre carte.
  - Durante la fase di fine round, vengono calcolati e mostrati i punteggi di tutti i giocatori in base alle previsioni e alle prese fatte durante il round.
- Il sistema deve gestire correttamente la fine della partita, mostrando i punteggi finali.

## Non funzionali
- Realizzazione di software in grado di essere facilmente estendibile, in modo da poter aggiungere nuove funzionalità o modificare quelle esistenti senza dover riscrivere completamente il codice.
- Realizzazione di un'interfaccia grafica (GUI) intuitiva e user-friendly, che permetta agli utenti di interagire con il sistema in modo semplice e immediato.

## Opzionali
- Permette al giocatore il suggerimento della carta ottima da giocare e conseguente logica del bot. 
- Sviluppo della logica del bot, per garantire scelte strategiche di prese e previsioni.

---

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/1-deployment.md) |
[Next Chapter](/docs/sections/3-architectural.md)
