# SnapTale

Aplikacja internetowa z krótkimi filmami.

Link: https://snaptale.vertyll.dev

## Struktura repozytorium

| Katalog                    | Opis                                                                                                 |
|----------------------------|------------------------------------------------------------------------------------------------------|
| `backend/`                 | Spring Boot 4.1, Java 25 - API                                                                       |
| `frontend/`                | Nuxt 4 - interfejs; proxy'uje `/api`, `/media` i logowanie (`/oauth2`, `/login/oauth2`) do back-endu |
| `docker-compose.local.yml` | Lokalnie: MySQL, Keycloak, maildev, opcjonalnie cały system (profil `app`)                           |
| `keycloak/`                | Realm `snaptale` importowany przez lokalny Keycloak                                                  |
| `.github/workflows/`       | CI: weryfikacja i obrazy Docker (`backend`, `frontend`)                                              |

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
- Spring Session.
- Keycloak.

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

| Pakiet                         | Odpowiedzialność                                                               |
|--------------------------------|--------------------------------------------------------------------------------|
| `auth`                         | Konto Snaptale zakładane przy pierwszym logowaniu przez Keycloak               |
| `user`                         | Konto zalogowanego użytkownika (`/api/me`), awatar, sugerowane konta           |
| `post`                         | Filmy, polubienia i komentarze                                                 |
| `follow`                       | Obserwowanie użytkowników                                                      |
| `profile`                      | Profil użytkownika złożony z modułów `user`, `follow` i `post`                 |
| `media`                        | Zapis i serwowanie plików (`/media/**`)                                        |
| `messages`                     | Komunikaty ICU po polsku i angielsku (`/api/messages?lang=`, tylko do odczytu) |
| `security`, `common`, `config` | Spring Security, błędy (RFC 9457), walidacja, zegar                            |

Błędy API zwracają kod komunikatu (`code`) i argumenty (`args`); treści są w
`backend/src/main/resources/messages/{pl,en}.json`, a testy pilnują, żeby każdy kod miał poprawny tekst ICU i żeby oba
języki miały te same klucze.

### Logowanie

Rejestracją, logowaniem, weryfikacją e-maila, resetem hasła, 2FA i akceptacją regulaminu zajmuje się Keycloak (realm
`snaptale`). Back-end loguje użytkownika przepływem authorization code z PKCE (`oauth2Login` ze Spring Security),
trzyma tokeny w sesji w MySQL (Spring Session) i daje przeglądarce tylko ciasteczko sesji `SNAPTALE_SESSION`. Przy
pierwszym logowaniu zakłada konto Snaptale (profil, filmy, obserwacje) powiązane z identyfikatorem użytkownika
w Keycloaku i przy każdym logowaniu odświeża jego e-mail. Wylogowanie (`POST /api/auth/logout`) kończy sesję i zwraca
adres wylogowania z Keycloaka. Język wybrany w Snaptale trafia na strony Keycloaka jako `ui_locales`.

Realm lokalny jest w `keycloak/realm-export.json`; realm produkcyjny utrzymuje
[`k8s-infra`](https://github.com/vertyll/k8s-infra) (`apps/keycloak-realms/snaptale.json`).

### Profile i konfiguracja

Domyślny profil to `local`; obraz Dockera ustawia `prod` (`SPRING_PROFILES_ACTIVE=prod`). Back-end nie używa plików `.env`.

| Plik                           | Zawartość                                                            |
|--------------------------------|----------------------------------------------------------------------|
| `application.properties`       | wspólna konfiguracja, bez zmiennych środowiskowych                   |
| `application-local.properties` | pełna konfiguracja lokalna (usługi z `docker-compose.local.yml`)     |
| `application-prod.properties`  | same odwołania `${...}` do zmiennych środowiskowych (tabela poniżej) |

Zmienne środowiskowe profilu `prod`:

| Zmienna                      | Opis                                                                 |
|------------------------------|----------------------------------------------------------------------|
| `DB_URL`                     | JDBC URL MySQL, np. z `sslMode=VERIFY_IDENTITY` i truststore klastra |
| `DB_USERNAME`, `DB_PASSWORD` | dane logowania do bazy                                               |
| `DB_TRUSTSTORE_PASSWORD`     | hasło truststore'a z CA klastra (TLS do MySQL)                       |
| `FRONTEND_URL`               | publiczny adres front-endu (adres powrotu z Keycloaka)               |
| `KEYCLOAK_REALM_URL`         | adres realmu, np. `https://keycloak.vertyll.dev/realms/snaptale`     |
| `KEYCLOAK_CLIENT_SECRET`     | sekret klienta `snaptale-backend`                                    |
| `MEDIA_DIRECTORY`            | katalog na wideo i awatary (trwały wolumen)                          |

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
> Adres back-endu i konsoli konta Keycloaka dla `nuxt dev` znajdują się w `.env.development`.

## Uruchomienie lokalne

> [!IMPORTANT]
>
> **Wymagania**: Docker, Java 25, Node.js 24.

```bash
docker compose -f docker-compose.local.yml up -d   # MySQL :3306, Keycloak :9000 (admin/admin), maildev :1025/:1080 (podgląd e-maili)

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
