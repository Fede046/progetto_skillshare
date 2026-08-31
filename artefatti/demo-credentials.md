# Utenti demo

Account creati da `DatabaseSeeder` per avere la piattaforma già navigabile
appena avviata. Tutti gli account condividono la stessa password.

**Password (valida per tutti):** `Demo1234!`

## Elenco account

| Email | Nome | Competenze | Annuncio pubblicato |
|---|---|---|---|
| `demo1@skillshare.it` | Giulia Ferrari | Programmazione Java, Spring Boot, Git | Ripetizioni di Java e Spring Boot |
| `demo2@skillshare.it` | Marco Bianchi | Lingua inglese, Traduzione, Public speaking | Conversazione in inglese B2/C1 |
| `demo3@skillshare.it` | Sofia Greco | Fotografia, Adobe Lightroom, Video editing | Ritratti fotografici e post-produzione |
| `demo4@skillshare.it` | Luca Moretti | Chitarra acustica, Teoria musicale, Produzione audio | Lezioni di chitarra acustica per principianti |

Ogni profilo ha bio e foto segnaposto già compilate.

## Cosa testare con quale account

### demo1@skillshare.it — Giulia Ferrari

L'account più completo: coinvolto in tre richieste su quattro.

- **Riceve** una richiesta `PENDING` da demo2 → si possono provare **Accetta** e **Rifiuta**
- **Riceve** una richiesta `ACCEPTED` da demo3 → chat attiva con tre messaggi
- **Ha inviato** la richiesta `COMPLETED` a demo3 → recensione già lasciata
- **Ha ricevuto** una recensione da demo3: **4 stelle**, visibile sul suo profilo pubblico

> Il suo annuncio ha uno scambio accettato in corso, quindi Modifica, Elimina e
> Sospendi risultano disabilitati: è il comportamento previsto dalla gestione annunci.

### demo2@skillshare.it — Marco Bianchi

- **Ha inviato** una richiesta `PENDING` a demo1 → in attesa di risposta
- **Ha rifiutato** la richiesta di demo4 sul proprio annuncio
- Nessuna recensione ricevuta: utile per vedere un profilo **senza rating**

### demo3@skillshare.it — Sofia Greco

Il profilo migliore per verificare le recensioni.

- **Ha inviato** la richiesta `ACCEPTED` a demo1 → chat attiva
- **Ha ricevuto** e completato la richiesta di demo1
- **Ha ricevuto** una recensione da demo1: **5 stelle**, visibile sul profilo pubblico

### demo4@skillshare.it — Luca Moretti

- **Ha inviato** una richiesta a demo2, che l'ha **rifiutata** (`REJECTED`)
- Il suo annuncio è libero da scambi: utile per provare **Modifica**, **Elimina** e **Sospendi**

## Riepilogo dei dati creati

| Entità | Quantità | Dettaglio |
|---|---|---|
| Utenti | 4 | con bio, foto e 3 tag ciascuno |
| Annunci | 4 | uno per utente, coerente con i suoi tag |
| Richieste di scambio | 4 | una per stato: `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED` |
| Messaggi | 5 | sulla richiesta accettata e su quella completata |
| Recensioni | 2 | reciproche sullo scambio completato (5 e 4 stelle) |

## Come avviare la demo

Le istruzioni valgono a partire da un progetto pulito. Tutti i comandi si
lanciano dalla cartella `app/`.

### Passo 1 — Popolare il database

Da eseguire **a server spenti**: MapDB tiene un lock esclusivo sul file del
database, quindi con Jetty o il container in esecuzione il comando fallisce.

```bash
cd app
mvn clean install -DskipTests
mvn exec:java -pl skillshare-server
```

Output atteso:

```
Seeding dei dati demo in corso...
Fatto: 4 utenti demo, 4 annunci, 4 richieste, messaggi e recensioni.
```

Viene creato `app/progetto_sweng.db`. È lo stesso file che il container Docker
monta come volume, quindi questo passo vale per entrambe le modalità di avvio.

Il comando è ripetibile: se gli utenti demo esistono già stampa
`Dati demo già presenti: nessuna modifica.` senza duplicare nulla.

### Passo 2 — Avvio in locale (sviluppo)

Servono due terminali, **in quest'ordine**: Jetty fallisce con
`Bad resourceBases` se il codeserver non ha ancora creato `launcherDir`.

```bash
# terminale 1 — attendere "The code server is ready"
cd app
mvn gwt:codeserver -pl *-client -am
```

```bash
# terminale 2
cd app
mvn jetty:run -pl *-server -am -Denv=dev
```

Applicazione su <http://localhost:8080>.

> Dopo ogni `mvn clean` va riavviato **anche** il codeserver, non solo Jetty:
> `clean` cancella `app/target/gwt/`, dove il codeserver tiene la propria
> directory di lavoro.

### Passo 2 (alternativo) — Avvio con Docker

```bash
cd app
docker compose up --build
```

Applicazione su <http://localhost:8080>.

> Il flag `--build` è **obbligatorio** quando il codice è cambiato dall'ultima
> costruzione dell'immagine. Un'immagine in cache contiene le classi vecchie:
> se i DTO sono stati modificati, non riesce a leggere il database popolato e
> l'applicazione risponde con errori di serializzazione.

Con Docker non servono codeserver e Jetty locali, e non vanno avviati insieme
al container: competerebbero per la porta 8080 e per il lock del database.

### Passo 3 — Accedere

Login con uno degli account della tabella sopra, password `Demo1234!`.

## Ricominciare da zero

```bash
cd app
rm -f progetto_sweng.db
rm -rf foto-profilo
mvn exec:java -pl skillshare-server
```

Per popolare un database in una posizione diversa da quella predefinita si usa
la stessa variabile d'ambiente che governa il database:

```bash
DATA_DIR=/percorso/dei/dati mvn exec:java -pl skillshare-server
```

## Problemi frequenti

| Sintomo | Causa | Rimedio |
|---|---|---|
| `Address already in use: 8080` | Jetty o il container sono già in esecuzione | `lsof -nP -iTCP:8080 -sTCP:LISTEN` per individuare il processo, poi fermarlo |
| `Bad resourceBases: ... launcherDir` | Jetty avviato prima del codeserver, oppure dopo un `mvn clean` | Avviare il codeserver e attendere che sia pronto |
| `File is already opened and is locked` | Seeding lanciato con un server attivo | Fermare Jetty o il container e ripetere |
| Avatar con le iniziali al posto delle foto | Le foto demo puntano a `placehold.co`, serve connessione | Comportamento previsto: il segnaposto è il fallback |
| Errori di serializzazione dopo modifiche ai DTO | Immagine Docker ricostruita senza `--build` | `docker compose up --build` |
