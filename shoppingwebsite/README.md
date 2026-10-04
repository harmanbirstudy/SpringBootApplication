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

> `app.auth.tokenSecret` in `application.yml` is also empty. It's the key used to sign
> the app's JWT login tokens, so set it to a long random string (for example the output of
> `openssl rand -base64 64`) or logins will fail when a token is issued. You can pass it
> as the `APP_AUTH_TOKENSECRET` environment variable.

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

### Regenerating seed data

`scripts/seed.sql` holds the DDL (hand-written, mirrors the JPA entities in
`src/main/java/com/webapp/shoppingwebsite/dao`) followed by an auto-generated data block.
To change the data, edit `scripts/generate_seed.py` and run:

```bash
python3 scripts/generate_seed.py          # rewrites the data block in scripts/seed.sql
python3 scripts/generate_seed.py --print  # print SQL to stdout only
```

The output is deterministic (fixed random seed), so re-running produces an identical file.

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
