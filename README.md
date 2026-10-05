# shoppingwebsite

Spring Boot shopping website backed by PostgreSQL (previously DynamoDB).

## Secrets (required)

`shoppingwebsite/src/main/resources/application.yml` contains no secrets. It reads them from environment
variables when the app starts:

| Environment variable   | Used for                               | Where to get it |
|------------------------|----------------------------------------|-----------------|
| `GOOGLE_CLIENT_ID`     | `spring.security.oauth2...google.clientId` | [Google Cloud Console](#getting-a-client-id-and-secret-from-google-cloud-console) |
| `GOOGLE_CLIENT_SECRET` | `spring.security.oauth2...google.clientSecret` | [Google Cloud Console](#getting-a-client-id-and-secret-from-google-cloud-console) |
| `APP_TOKEN_SECRET`     | `app.auth.tokenSecret` (signs JWT login tokens) | [Generate one](#jwt-token-secret) |

```yaml
          google:
            clientId: ${GOOGLE_CLIENT_ID}
            clientSecret: ${GOOGLE_CLIENT_SECRET}
...
app:
  auth:
    tokenSecret: ${APP_TOKEN_SECRET}
```

If any of them isn't set, the app fails to start with
`Could not resolve placeholder 'GOOGLE_CLIENT_ID'` (or the name of the missing variable).
The database settings (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) work the same way, but have
local defaults, so you don't need to set them for the Docker database below.

### Setting the variables

**Option 1: a local `.env` file (recommended).** Create `.env` next to `shoppingwebsite/pom.xml`:

```bash
GOOGLE_CLIENT_ID=<your-client-id>.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=<your-client-secret>
APP_TOKEN_SECRET=<output-of-openssl-rand-base64-32>
```

`.env` is in `.gitignore`, so it is never committed. Spring Boot 2.2 doesn't read `.env` files
itself, so load it into your shell first:

```bash
set -a; source .env; set +a
./mvnw spring-boot:run
```

`set -a` exports every variable the file sets. Without it, the app can't see them.

**Option 2: export them directly** (add to `~/.zshrc` to keep them):

```bash
export GOOGLE_CLIENT_ID=<your-client-id>.apps.googleusercontent.com
export GOOGLE_CLIENT_SECRET=<your-client-secret>
export APP_TOKEN_SECRET=<output-of-openssl-rand-base64-32>
```

**IntelliJ:** open **Run → Edit Configurations →** your Spring Boot app **→ Environment variables**
and add `GOOGLE_CLIENT_ID=...;GOOGLE_CLIENT_SECRET=...;APP_TOKEN_SECRET=...`.
Or install the **EnvFile** plugin and point it at `.env`.

**Server, Docker or cloud host:** set the same three variables in its environment settings.

Never put real values back into `application.yml`, and don't use them as placeholder defaults
(`${GOOGLE_CLIENT_SECRET:real-value}`), because that file is committed.

### Google OAuth2 credentials

`GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are needed even if you only use email/password login.
- Without `GOOGLE_CLIENT_ID` the app doesn't start. If it is set to an empty value, startup fails with
  `Client id must not be empty.`
- With an empty `GOOGLE_CLIENT_SECRET` the app starts, but "Sign in with Google" fails when Google
  sends the user back to the app.

### Getting a client ID and secret from Google Cloud Console

1. Go to <https://console.cloud.google.com/> and create a project (or pick an existing one)
   from the project selector at the top.
2. Open **APIs & Services → OAuth consent screen** (shown as **Google Auth Platform**).
   If it isn't configured yet, click **Get started** and fill in:
   - **App name** and **User support email**
   - **Audience:** `External`
   - **Contact information:** your email
3. Under **Audience → Test users**, add the Google accounts you will log in with.
   While the app is in *Testing* mode, only these accounts can sign in.
4. Go to **Clients** (or **APIs & Services → Credentials**) → **Create client**
   (**Create credentials → OAuth client ID**):
   - **Application type:** `Web application`
   - **Authorized JavaScript origins:**
     - `http://localhost:8080`
     - `http://localhost:4200` (Angular frontend)
   - **Authorized redirect URIs:**
     - `http://localhost:8080/oauth2/callback/google`

     This must match `redirectUri: "{baseUrl}/oauth2/callback/{registrationId}"` exactly
     (same scheme, host, port and path), or Google returns `redirect_uri_mismatch`.
5. Click **Create**. Copy the **Client ID** and **Client secret** right away, or click
   **Download JSON**. Google may not show the secret again later. If you lose it, add a
   new secret to the client.

Put the two values in `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` (see
[Setting the variables](#setting-the-variables)).

### JWT token secret

`APP_TOKEN_SECRET` is the key `TokenProvider` uses to sign the app's JWT login tokens.
If it's set to an empty value, the app still starts, but every login fails when a token is
issued, with `java.lang.IllegalArgumentException: Empty key`.

It can be any random value, as long as it meets two rules:

1. **It must be base64.** `TokenProvider` base64-decodes it before using it as the key.
   Characters that aren't base64 (like `-` or `!`) are silently dropped, so the key ends up
   shorter than the string you typed.
2. **It must decode to at least 32 bytes (256 bits).** jjwt 0.11.2 rejects shorter keys for
   HS256 with a `WeakKeyException`. In practice, that's a base64 string of about 43+ characters.

| Value                                 | Works? | Why                                       |
|---------------------------------------|--------|-------------------------------------------|
| *(empty)*                             | No     | `IllegalArgumentException: Empty key`     |
| `mysecret`                            | No     | Decodes to 6 bytes, so too short (`WeakKeyException`) |
| `my-super-secret-password`            | No     | ~16 bytes once `-` is dropped, so too short |
| Output of `openssl rand -base64 32`   | Yes    | Exactly 32 random bytes, valid base64     |

The easiest option is to generate one:

```bash
openssl rand -base64 32
```

Use `32`, not `64`. openssl splits longer output across two lines, which makes it easy to
paste only half of the value.

Then put it in `APP_TOKEN_SECRET` (see [Setting the variables](#setting-the-variables)).

Notes:
- The same secret is used to sign tokens and to check them on later requests. If you change
  it, tokens already issued stop working and users must log in again.
- For local testing it doesn't matter much if the value leaks. In any shared or deployed
  environment, keep it secret like a password, because anyone who has it can create valid
  login tokens for any user.

## Local database

PostgreSQL 16 runs in Docker via `shoppingwebsite/docker-compose.yml`.

| Setting  | Value             |
|----------|-------------------|
| Host     | `localhost`       |
| Port     | `5432`            |
| Database | `shoppingwebsite` |
| User     | `shopping`        |
| Password | `shopping`        |

### Start the database (no seed data)

```bash
docker compose up -d
```

The tables are created by Hibernate (`ddl-auto: update`) when the app starts.

### Start the database and load seed data

```bash
docker compose --profile seed up -d
```

The `seed` service waits for Postgres to be healthy, runs `shoppingwebsite/scripts/seed.sql`, and exits.
It only runs when `--profile seed` is passed.

`seed.sql` is safe to re-run: tables use `CREATE TABLE IF NOT EXISTS` and inserts use
`ON CONFLICT DO NOTHING`, so existing rows (including data you added through the app)
are left alone and only missing seed rows are inserted.

Check the seed output with:

```bash
docker compose logs seed
```

### Stop / reset

```bash
docker compose down                   # stop, keep data
docker compose --profile seed down -v # stop and delete all data (fresh database next time)
```

### Sharing the database with DataSqlAnalysis

The data volume is named `shoppingwebsite_pgdata` and is shared with the
[DataSqlAnalysis](https://github.com/harmanbirstudy/DataSqlAnalysis) Text-to-SQL
project, which also keeps its Langfuse database on this server. `down -v`
deletes it for both projects.

DataSqlAnalysis can also start the same Postgres itself (`docker-compose --profile db up`),
on the same volume and port. Only one of the two can run at a time — port 5432
makes sure of that, which matters because two servers on one data volume would
corrupt it. If `docker compose up` fails with
`Bind for 0.0.0.0:5432 failed: port is already allocated`, the DataSqlAnalysis
Postgres is running. Either keep using it (same data), or stop it and start this one:

```bash
DSA=../../VSCodeRepository/DataSqlAnalysis   # where you cloned DataSqlAnalysis
(cd "$DSA" && docker-compose --profile db stop postgres)
docker compose up -d postgres
```

### Seed data

| Table                | Rows  |
|----------------------|-------|
| productcategory      | 6     |
| products             | 36    |
| users                | 301   |
| user_roles           | 302   |
| orders               | 1,500 |
| order_products       | 3,315 |
| shoppingcart         | 200   |
| shoppingcart_items   | 452   |

Logins (all users with provider `local`):

- Admin: `admin@shoppingwebsite.com` / `password123` (`ROLE_ADMIN`)
- Any seeded local user, e.g. from `SELECT email FROM users WHERE provider = 'local'` / `password123`

### Querying

```bash
docker compose exec postgres psql -U shopping -d shoppingwebsite
```

```sql
\dt                         -- list tables
SELECT * FROM orders LIMIT 10;
```

In IntelliJ/DataGrip, make sure the data source's database is `shoppingwebsite`
(not the default `postgres`).
