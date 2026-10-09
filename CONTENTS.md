# Contents

Every document in this repository, the module it belongs to, and what it covers. Terms are defined in
[GLOSSARY.md](GLOSSARY.md), and the specifications behind them are in [STANDARDS.md](STANDARDS.md).

## Start here

| Document                  | Module | Kind              | Covers                                                         |
|---------------------------|--------|-------------------|----------------------------------------------------------------|
| [snaptale](README.md)     | —      | repository README | What the repository is, its stack and where to start.          |
| [Glossary](GLOSSARY.md)   | —      | reference         | Every term the docs use, and where it is explained.            |
| [Standards](STANDARDS.md) | —      | reference         | The RFCs and specifications the code implements or depends on. |

## Overview

| Document                                       | Module | Kind     | Covers                                            |
|------------------------------------------------|--------|----------|---------------------------------------------------|
| [Development Setup](docs/development-setup.md) | —      | overview | The infrastructure and starting the applications. |
| [Architecture](docs/architecture.md)           | —      | overview | The two applications and how they talk.           |
| [Authentication](docs/authentication.md)       | —      | overview | Sign-in, token authorization and sign-out.        |

## Mechanisms

| Document                                              | Module | Kind      | Covers                                                                                           |
|-------------------------------------------------------|--------|-----------|--------------------------------------------------------------------------------------------------|
| [CSRF](docs/mechanisms/csrf.md)                       | —      | mechanism | Why a forged request from another site cannot act with the user's session cookie.                |
| [Error responses](docs/mechanisms/error-responses.md) | —      | mechanism | What the back-end answers when it refuses a request, and how the front-end turns that into text. |

## Applications

| Document                                                  | Module    | Kind          | Covers                                                                                      |
|-----------------------------------------------------------|-----------|---------------|---------------------------------------------------------------------------------------------|
| [Back-end](backend/README.md)                             | back-end  | module README | Layout, access, accounts, messages, database, running and production.                       |
| [Media storage](backend/docs/mechanisms/media-storage.md) | back-end  | mechanism     | Where uploaded files are written, and how they reach the browser.                           |
| [Token refresh](backend/docs/mechanisms/token-refresh.md) | back-end  | mechanism     | How the session keeps a valid access token without signing the user out when requests race. |
| [Front-end](frontend/README.md)                           | front-end | module README | Layout, text, running and production.                                                       |
