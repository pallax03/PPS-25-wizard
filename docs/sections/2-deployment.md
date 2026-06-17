# Processo di Sviluppo

## Scrum
La metodologia di sviluppo applicata su questo progetto è di tipo Agile, in particolare si è adottata la variante *Scrum* 

### Pianificazione Sprint e Meeting di Allineamento


## Test-Driven Development
Durante lo sviluppo del sistema è stato scelto di applicare il più possibile il _Test-Driven Development (TDD)_,
il cui scopo è quello di anticipare il prima possibile la fase di testing per minimizzare i costi di manutenzione e
il rischio di fallimento del progetto.

Il processo seguito durante il TDD è un processo iterativo chiamato _Red-Green-Refactor (RGR)_, che prevede a ogni
iterazione le seguenti fasi:
1. _Red_: scrivere un test che fallisca per una certa funzionalità da implementare
2. _Green_: scrivere il codice di produzione che soddisfi il test definito precedentemente
3. _Refactor_: ristrutturare sia il codice di testing che quello di produzione

A supporto di questo processo, sono stati adottati i seguenti strumenti:
- _ScalaTest_: framework per la definizione di unit test per scala.
- _Scoverage_: strumento per valutare la qualità dei test come percentuale di codice di produzione analizzato.


## Continuous Integration & Delivery
Per mantenere un buon livello di qualità e affidabilità del codice sono stati sviluppati workflow che eseguono i test del sorgente e controllando che lo stile sia conforme in tutte le porzioni di codice.

## Versioning
Si è scelto di adottare di utilizzare la struttura dei **Conventional Commits** per mantenere uniformità nella parte di commitment del codice.

Per il versionamento del sistema si adotta lo standard **Semantic Versioning**: dato un numero di versione nel formato *MAJOR.MINOR.PATCH*, si incrementi:

- la versione **MAJOR** quando si apportano modifiche che rendono il codice non-retrocompatibile
- la versione **MINOR** quando si aggiungono funzionalità, mantenendo il sistema retro compatibile
- la versione **PATCH** quando si correggono bug, mantenendo il sistema retro compatibile

## Quality Assurance

