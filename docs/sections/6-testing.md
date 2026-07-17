# Testing

In questa sezione vengono descritte le metodologie di testing adottate durante lo sviluppo del sistema, con particolare attenzione al _Test-Driven Development (TDD)_ e al processo di _Red-Green-Refactor (RGR)_.

## Metodologie di Testing

### Test-Driven Development

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

[Back to index](/index.md) |
[Previous Chapter](/docs/sections/5-implementation/index.md) |
[Next Chapter](/docs/sections/7-application.md)
