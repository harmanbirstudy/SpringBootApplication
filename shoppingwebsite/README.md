# shoppingwebsite

Spring Boot shopping website backed by PostgreSQL (previously DynamoDB).

## Google OAuth2 credentials (required)

`src/main/resources/application.yml` has Google login configured:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            clientId:
            clientSecret:
```

**`clientId` and `clientSecret` must not be empty.**
- An empty `clientId` stops the app from starting, with
  `Client id must not be empty.` This happens even if you only use email/password login.
- An empty `clientSecret` lets the app start, but "Sign in with Google" fails when Google
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

### Supplying the credentials

Don't commit real secrets. Pass them as environment variables when running the app:

```bash
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTID=<your-client-id>.apps.googleusercontent.com
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTSECRET=<your-client-secret>
./mvnw spring-boot:run
```

In IntelliJ, set the same variables in **Run → Edit Configurations → Environment variables**.

Or, for local use only, fill in `clientId` / `clientSecret` in `application.yml` and
don't commit that change.

### JWT token secret (required)

`app.auth.tokenSecret` in `application.yml` is also empty. It's the key `TokenProvider`
uses to sign the app's JWT login tokens. If it's empty, the app still starts, but every
login fails when a token is issued, with `java.lang.IllegalArgumentException: Empty key`.

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

Then pass it as an environment variable (or set `tokenSecret` locally, without committing it):

```bash
export APP_AUTH_TOKENSECRET=<output-of-openssl>
```

Notes:
- The same secret is used to sign tokens and to check them on later requests. If you change
  it, tokens already issued stop working and users must log in again.
- For local testing it doesn't matter much if the value leaks. In any shared or deployed
  environment, keep it secret like a password, because anyone who has it can create valid
  login tokens for any user.

## Local database

PostgreSQL 16 runs in Docker via `docker-compose.yml`.

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

The `seed` service waits for Postgres to be healthy, runs `scripts/seed.sql`, and exits.
It only runs when `--profile seed` is passed.

`seed.sql` is safe to re-run: tables use `CREATE TABLE IF NOT EXISTS` and inserts use
`ON CONFLICT DO NOTHING`, so existing rows (including data you added through the app)
are left alone and only missing seed rows are inserted.

Check the seed output with:

```bash
docker logs shoppingwebsite-seed
```

### Stop / reset

```bash
docker compose down                   # stop, keep data
docker compose --profile seed down -v # stop and delete all data (fresh database next time)
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
docker exec -it shoppingwebsite-postgres psql -U shopping -d shoppingwebsite
```

```sql
\dt                         -- list tables
SELECT * FROM orders LIMIT 10;
```

In IntelliJ/DataGrip, make sure the data source's database is `shoppingwebsite`
(not the default `postgres`).
