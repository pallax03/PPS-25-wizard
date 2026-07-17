# Processo di Sviluppo

## Scrum

Per la gestione di questo progetto è stata adottata la metodologia Agile, declinata nello specifico attraverso il
framework Scrum.

Questo modello si basa su un approccio iterativo e incrementale: il lavoro procede per cicli ripetuti in cui, di volta
in volta, vengono introdotte nuove funzionalità o perfezionate quelle già esistenti.

### Pianificazione Sprint e Meeting di Allineamento

La fase di avvio prevede la definizione del Product Backlog, ovvero il documento programmatico che raccoglie e ordina
l'elenco completo dei requisiti e delle funzionalità del sistema (definiti item).

Il processo è scandito da iterazioni della durata di una settimana, denominate sprint. Ciascun ciclo prevede una serie
di attività strutturate:

- Sprint Planning: È l'incontro preliminare in cui il team seleziona gli item del Product Backlog da sviluppare durante
  la settimana. Queste funzionalità vengono suddivise in sotto-attività più semplici, di cui gli sviluppatori stimano la
  complessità e delineano l'architettura tecnica di massima. L'incontro si conclude con la redazione dello Sprint
  Backlog, che assegna formalmente i task operativi ai singoli componenti del team.
- Daily Scrum: Un breve confronto giornaliero in cui il team fa il punto sullo stato di avanzamento delle attività e, se
  necessario, rimodula lo Sprint Backlog per superare eventuali ostacoli.

Al termine della settimana si svolgono tre sessioni conclusive:

- Product Backlog Refinement: Una riunione dedicata alla manutenzione e all'ottimizzazione del Product Backlog, per
  pianificare al meglio i cicli successivi.

- Sprint Review: Il momento in cui si analizza quanto realizzato durante lo sprint per accertarsi che il lavoro produca
  un incremento di prodotto potenzialmente rilasciabile (PSPI - Potentially Shippable Product Increment).

- Sprint Retrospective: Un'analisi retrospettiva focalizzata sul flusso di lavoro e sulle dinamiche del team, mirata a
  individuare margini di miglioramento organizzativo e operativo per lo sprint successivo.

## Continuous Integration & Delivery

Per mantenere un buon livello di qualità e affidabilità del codice sono stati sviluppati workflow che eseguono i test
del sorgente e controllando che lo stile sia conforme in tutte le porzioni di codice.

## Versioning

Si è scelto di adottare di utilizzare la struttura dei **Conventional Commits** per mantenere uniformità nella parte di
commitment del codice.

Per il versionamento del sistema si adotta lo standard **Semantic Versioning**: dato un numero di versione nel formato
*MAJOR.MINOR.PATCH*, si incrementi:

- la versione **MAJOR** quando si apportano modifiche che rendono il codice non-retrocompatibile
- la versione **MINOR** quando si aggiungono funzionalità, mantenendo il sistema retro compatibile
- la versione **PATCH** quando si correggono bug, mantenendo il sistema retro compatibile


---

[Back to index](/index.md) |
[Next Chapter](/docs/sections/2-requirements.md) |
