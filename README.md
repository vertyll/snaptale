## Założenia projektu 

Aplikacja internetowa SnapTale wzorująca się na założeniach aplikacji TikTok

## Link: https://snaptale.vertyll.dev

## Stos technologiczny

### Back-end:
- Laravel
- PHP
- MySQL

### Front-end:
- Nuxt.js
- Vue.js
- Axios
- Tailwind CSS do szybkiego stylowania komponentów
- Pinia do zarządzania stanem aplikacji

### Uwierzytelnianie:
- uwierzytelnianie za pomocą sesji
- Laravel Sanctum jako system do uwierzytelniania

### Dodatkowe narzędzia:
- intervention/image jako biblioteka przetwarzania obrazów PHP

### Inne:
- nuxt-icon dla ikon w aplikacji

## Zdjęcia poglądowe

![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale4.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale2.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale5.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale1.png)
![Widok projektu](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale3.png)

## Instrukcja instalacji projektu – back-end

Pobieramy projekt na lokalne środowisko

Instalujemy zależności:

```bash
composer install 

cp .env.example .env 

php artisan cache:clear

composer dump-autoload

php artisan key:generate

composer require laravel/breeze --dev

php artisan serve
```

Tworzymy bazę danych. Upewniamy się że DB_DATABASE w pliku .env jest taka sama i uruchamiamy migrację

```bash
php artisan migrate
```

## Instrukcja instalacji projektu – front-end

Pobieramy projekt na lokalne środowisko

Instalujemy zależności:

```bash
# npm
npm install

# pnpm
pnpm install

# yarn
yarn install

# bun
bun install
```

### Środowisko deweloperskie

Wystartuj serwer deweloperski na `http://localhost:3000`:

```bash
# npm
npm run dev

# pnpm
pnpm run dev

# yarn
yarn dev

# bun
bun run dev
```

### Środowisko produkcyjne

Budowanie aplikacji na produkcję:

```bash
# npm
npm run build

# pnpm
pnpm run build

# yarn
yarn build

# bun
bun run build
```

Lokalny podgląd zbudowanej aplikacji produkcyjnej:

```bash
# npm
npm run preview

# pnpm
pnpm run preview

# yarn
yarn preview

# bun
bun run preview
```
