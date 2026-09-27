# SnapTale

Aplikacja internetowa z krótkimi filmami.

Link: https://snaptale.vertyll.dev

## Struktura repozytorium

| Katalog                    | Opis                                                              |
|----------------------------|-------------------------------------------------------------------|
| `backend/`                 | Spring Boot 4.1, Java 25 - API                                    |
| `frontend/`                | Nuxt 4 - interfejs; proxy'uje `/api` i `/media` do back-endu      |
| `docker-compose.local.yml` | Lokalnie: MySQL + maildev, opcjonalnie cały system (profil `app`) |
| `.github/workflows/`       | CI: weryfikacja i obrazy Docker (`backend`, `frontend`)           |

## Back-end

### Stos technologiczny

- Spring Boot.
- Java.
- Maven.
- MySQL.
- Flyway.
- JUnit.
- Lombok.
- Spring Security.
- Spring Data JPA.
- Spring Web.
- Spring Mail.
- Thymeleaf.

### Budowanie i jakość

```bash
cd backend
./mvnw spotless:apply   # formatowanie
./mvnw verify           # kompilacja z Error Prone/NullAway, testy, Spotless, PMD, SpotBugs
```

> [!IMPORTANT]
>
> Polecenie `verify` wymaga środowiska skonteneryzowanego dla Testcontainers.

### Moduły

| Pakiet                         | Odpowiedzialność                                                                |
|--------------------------------|---------------------------------------------------------------------------------|
| `auth`                         | Rejestracja, logowanie, wylogowanie, weryfikacja e-maila, reset hasła, e-maile  |
| `user`                         | Konto zalogowanego użytkownika (`/api/me`), awatar, sugerowane konta            |
| `post`                         | Filmy, polubienia i komentarze                                                  |
| `follow`                       | Obserwowanie użytkowników                                                       |
| `profile`                      | Profil użytkownika złożony z modułów `user`, `follow` i `post`                  |
| `media`                        | Zapis i serwowanie plików (`/media/**`)                                         |
| `messages`                     | Polskie komunikaty ICU dla kodów błędów API (`/api/messages`, tylko do odczytu) |
| `security`, `common`, `config` | Spring Security, błędy (RFC 9457), walidacja, zegar                             |

Błędy API zwracają kod komunikatu (`code`) i argumenty (`args`); treść po polsku jest w
`backend/src/main/resources/messages/pl.json`, a test pilnuje, żeby każdy kod miał poprawny tekst ICU.

### Profile i konfiguracja

Domyślny profil to `local`; obraz Dockera ustawia `prod` (`SPRING_PROFILES_ACTIVE=prod`). Back-end nie używa plików `.env`.

| Plik                           | Zawartość                                                            |
|--------------------------------|----------------------------------------------------------------------|
| `application.properties`       | wspólna konfiguracja, bez zmiennych środowiskowych                   |
| `application-local.properties` | pełna konfiguracja lokalna (usługi z `docker-compose.local.yml`)       |
| `application-prod.properties`  | same odwołania `${...}` do zmiennych środowiskowych (tabela poniżej) |

Zmienne środowiskowe profilu `prod`:

| Zmienna                                                    | Opis                                                                 |
|------------------------------------------------------------|----------------------------------------------------------------------|
| `DB_URL`                                                   | JDBC URL MySQL, np. z `sslMode=VERIFY_IDENTITY` i truststore klastra |
| `DB_USERNAME`, `DB_PASSWORD`                               | dane logowania do bazy                                               |
| `DB_TRUSTSTORE_PASSWORD`                                   | hasło truststore'a z CA klastra (TLS do MySQL)                       |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | serwer SMTP                                                          |
| `MAIL_FROM`                                                | adres nadawcy e-maili                                                |
| `FRONTEND_URL`                                             | publiczny adres front-endu (linki w e-mailach)                       |
| `MEDIA_DIRECTORY`                                          | katalog na wideo i awatary (trwały wolumen)                          |

## Front-end

### Stos technologiczny

- Nuxt.
- Vue.js.
- Node.js.
- TypeScript.
- Tailwind CSS.

### Budowanie i jakość

```bash
cd frontend
npm ci
npm run dev             # http://localhost:3000
npm run lint && npm run typecheck && npm run format:check
```

> [!NOTE]
>
> Adres back-endu dla `nuxt dev` znajduje się w `.env.development`.

## Uruchomienie lokalne

> [!IMPORTANT]
>
> **Wymagania**: Docker, Java 25, Node.js 24.

```bash
docker compose -f docker-compose.local.yml up -d   # MySQL :3306, maildev :1025 (SMTP) i :1080 (podgląd e-maili)

cd backend && ./mvnw spring-boot:run                                # :8080
cd frontend && npm ci && npm run dev                                # http://localhost:3000
```

Albo cały system w kontenerach: `docker compose -f docker-compose.local.yml --profile app up -d --build`.

## Zrzuty ekranu

![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale4.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale2.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale5.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale1.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale3.png)
