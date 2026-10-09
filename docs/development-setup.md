# Development Setup

## Prerequisites

- Docker or Podman, with Compose
- JDK 25
- Node.js and npm

## Start the infrastructure

```bash
docker compose -f docker-compose.local.yml up -d
```

Every `docker compose` command here works verbatim as `podman compose`.

| Service      | Address                                     | Purpose                                         |
|--------------|---------------------------------------------|-------------------------------------------------|
| MySQL        | `localhost:3306` (`root` / `root`)          | the `snaptale` database                         |
| Redis        | `localhost:6379`                            | sessions and the shared refresh lock            |
| Keycloak     | `http://localhost:9000` (`admin` / `admin`) | realm `snaptale`, imported on start             |
| MailDev      | `http://localhost:1080`                     | catches Keycloak's verification and reset mails |
| RedisInsight | `http://localhost:5540`                     | browsing the sessions in Redis                  |

The realm comes with no accounts: sign up through the application, and confirm the address from the mail MailDev
catches.

> [!NOTE]
>
> The realm lives in the `keycloak-data` volume, so a change to `keycloak/realm-export.json` only takes effect after
> `docker compose -f docker-compose.local.yml down -v`.

## Run the applications

Start the back-end, then the front-end; each README says how, and lists its checks and production settings:
[back-end](../backend/README.md), [front-end](../frontend/README.md). The application is at `http://localhost:3000`.

## Everything in containers

The `app` profile adds the back-end and the front-end to the compose file:

```bash
docker compose -f docker-compose.local.yml --profile app up -d --build
```

## Documentation checks

The Markdown is formatted and linted with [mdtools](https://github.com/vertyll/mdtools), at the version the
[Docs workflow](../.github/workflows/docs.yml) pins:

```bash
go run github.com/vertyll/mdtools/cmd/mdtools@VERSION fmt
go run github.com/vertyll/mdtools/cmd/mdtools@VERSION run
```

`fmt` rewrites what it can; `run` reports the rest and fails on any issue, as the workflow does on every push and pull
request. [`.mdtools.yaml`](../.mdtools.yaml) chooses the formatters and linters and what each one skips.
