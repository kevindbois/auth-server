# Auth Server

Autentiseringstjänst för **TechShop**, grupp 2:s webbshop. Hanterar registrering och inloggning och skapar JWT-tokens som resten av systemet använder för att veta vem användaren är.

**Tekniker:** Java 17, Spring Boot 4, Spring Security, PostgreSQL, jjwt

## Hur den passar in

```
Webbläsare → Gateway → Auth Server → PostgreSQL
```

Frontend anropar aldrig auth-servern direkt. Gatewayen skickar vidare inloggningen, sparar token i en HttpOnly-cookie och skickar den som `Authorization: Bearer <token>` vid senare anrop.

## Endpoints

| Metod | Sökväg | Kräver token | Beskrivning |
|-------|--------|--------------|-------------|
| POST | `/auth/register` | Nej | Skapar en användare med rollen `USER` |
| POST | `/auth/login` | Nej | Loggar in och returnerar en JWT |
| GET | `/auth/me` | Ja | Returnerar inloggad användare och tokenens återstående tid |
| GET | `/auth/jwks` | Nej | Publik nyckel för att verifiera tokens |

Swagger UI: `/swagger-ui/index.html`

**Exempel – login-svar:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "subject": "anna@example.com",
  "roles": ["USER"],
  "expiresIn": 86400
}
```

## Säkerhet

- Tokens signeras med **RS256**. Bara auth-servern har den privata nyckeln och kan skapa tokens.
- Tokens gäller i **24 timmar**.
- Lösenord sparas som **BCrypt**-hashar, aldrig i klartext.
- Ett admin-konto skapas automatiskt vid start om det inte redan finns.

## Miljövariabler

| Variabel | Beskrivning |
|----------|-------------|
| `DB_URL` | JDBC-URL, t.ex. `jdbc:postgresql://host:5432/db?sslmode=require` |
| `DB_USERNAME` / `DB_PASSWORD` | Inloggning till databasen |
| `RSA_PRIVATE_KEY_VALUE` | Privat RSA-nyckel (PEM), signerar tokens |
| `RSA_PUBLIC_KEY_VALUE` | Publik RSA-nyckel (PEM), verifierar tokens |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Admin-kontot som skapas vid start |

Skapa RSA-nycklar:
```bash
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in private.pem -pubout -out public.pem
```
Committa aldrig nycklarna.

## Köra lokalt

1. Sätt miljövariablerna, t.ex. i IntelliJ under *Run → Edit Configurations*.
2. Starta med `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Tjänsten körs på `http://localhost:8080`.

Tester körs med `./mvnw test` och använder en H2-databas i minnet. RSA-nycklarna måste ändå vara satta.

## CI/CD

Vid push och PR mot `main` bygger och testar GitHub Actions projektet. På `main` byggs även en Docker-image som pushas till Docker Hub. Tjänsten körs på **Render**.

> Render-databaser på gratisnivån går ut efter 30 dagar. Skapa då en ny och uppdatera `DB_*`-variablerna.

## Kända begränsningar

- Fel lösenord eller upptaget användarnamn ger HTTP 500 i stället för 401/409.
- Utloggning sköts av gatewayen genom att cookien tas bort. Själva token gäller tills den går ut.
