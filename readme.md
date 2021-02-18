# Spring Boot Api Boilerplate
Boilerplate project for a (REST) Api in Spring Boot.

## Table of Contents
- [1. Quick Rundown](#1-quick-rundown)
- [2. Package Structure](#2-package-structure)
- [3. Get Started](#3-get-started)
    * [3.1. Refactoring](#31-refactoring)
    * [3.2. Security](#32-security)
        + [3.2.1. Authorization Server](#321-authorization-server)
        + [3.2.2. Resource Server](#322-resource-server)
    * [3.3. Database](#33-database)
        + [3.3.1. Mysql](#331-mysql)
        + [3.3.2. Mongo](#332-mongo)
    * [3.4. Email](#34-email)
    * [3.5. Exception Handling](#35-exception-handling)
    * [3.6. HTTPS](#36-https)
    * [3.7. Additional Information](#37-additional-information)

## 1. Quick Rundown
- Global Exception Handler
- Authorization and Authentication with OAuth2 using JWK signed JWT.
- Mysql database connection (for mongodb navigate to the mongodb boilerplate branch).
- Bean validation
- Unit tests
- Lombok (requires Lombok plugin)
- User endpoint with the following features:
    - Save user
    - Get authenticated user
    - Update user
    - Delete user
    - change password
    - forgot password
    - reset password

## 2. Package Structure
```bash
├── boilerplateapi
│   ├── config
│   ├── exceptionhandler
│   │   ├── response
│   │   ├── exception
│   ├── features
│   │   ├── email
│   │   ├── user
│   │   │   ├── password
│   │   │   │   ├── change
│   │   │   │   ├── forgot
│   │   │   │   ├── reset
│   ├── security
│   │   ├── authorizationserver
│   │   │   ├── clientdetails
│   │   │   ├── userdetails
│   │   ├── resourceserver
│   ├── utils
│   ├── validators
```
## 3. Get Started
The required configuration changes to get started are also highlighted with to-dos in the project. More information is found in this section.

### 3.1. Refactoring
To refactor the application from the boilerplate naming to your project needs, do the following:
1. POM
    - Change `GroupId`
    - Change `artifactId`
    - Change `name`
    - Change `description`


2. Package and Module
    - Change packages `com/boilerplate/boilerplateapi` to reflect new `groupId/artifactId`.
    - Change `SpringBootApiBoilerplateApplication` and module to reflect the new `name`.
    - In Intellij edit configuration `name` to reflect the new `name`.

### 3.2. Security
Users are authenticated and authorized using the OAuth 2 protocol and JWT. To enable this the following components are configured:
- Authorization server
- Resource server

#### 3.2.1. Authorization Server
The Authorization server is in charge of creating and approving the authorization and the authentication data for the resource server.
Requesting user tokens first requires a client to be registered in the ClientRepository (will be done in database section).
Then an access token can be retrieved using the following request:

Note: Authorization header is a Basic Auth where `username=<CLIENT_ID>` and `password=<CLIENT_SECRET>`.

-   Get access token with user credentials
    ```
    curl --location --request POST 'http://localhost:8080/oauth/token' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --header 'Authorization: Basic ZGFydHMtbWF0Y2hlci13ZWI6c2VjcmV0' \
    --data-urlencode 'username=<USER_EMAIL>' \
    --data-urlencode 'password=<USER_PASSWORD>' \
    --data-urlencode 'grant_type=password'
    ```

-   Get access token with refresh token
    ```
    curl --location --request POST 'http://localhost:8080/oauth/token' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --header 'Authorization: Basic ZGFydHMtbWF0Y2hlci13ZWI6c2VjcmV0' \
    --data-urlencode 'refresh_token=-<REFRESH_TOKEN>' \
    --data-urlencode 'grant_type=refresh_token'
    ```

The Authorization server supports JWT-encoded JWK-signed authorization and exposes a JWK Set URI. Follow the following steps to configure
the keystore containing the KeyPair:

1.  Create keystore using the following command:
    ```
    keytool -genkey -keyalg RSA -storetype PKCS12 -keysize 2048 -alias <KEYSTORE_ALIAS> -keystore <KEYSTORE_FILE.jks> -storepass <KEYSTORE_PASSWORD>
    ```

    (optional) View Public Key:
    ```
    keytool -list -rfc -alias <KEYSTORE_ALIAS> -storepass <KEYSTORE_PASSWORD> -keystore <KEY_STORE_FILE.jks>
    ```
    (optional) View Public and Private Key:
    ```
    keytool -importkeystore -srckeystore <KEYSTORE_FILE.jks> -destkeystore <TEMP_P12.p12> -deststoretype PKCS12
       
    openssl pkcs12 -info -in <TEMP_P12.p12>
    ```

    **Note:** it's important that production keystores are **not** pushed to a public environment, these should stay private (add to gitignore).


2.  Copy generated `.jks` keystore into the spring project `resources/keystore` folder.


3. Add the following Environment Variables
    ```
    JWK_KEYSTORE_PATH=classpath:keystore/<KEYSTORE_FILE.jks>`
    JWK_KEYSTORE_PASS=<KEYSTORE_PASSWORD>
    JWK_KEYSTORE_ALIAS=<KEYSTORE_ALIAS>
   ```

#### 3.2.2. Resource Server
The resource server does not require additional configuration to work out of the box. If your needs require modifications they should be done in:
- `security/resourceserver/ResourceServerSecurityConfig.java`

### 3.3. Database
The boilerplate repo has two versions, one with mysql configured and one with mongo configurations. If you are using a
different database and need to configure the boilerplate to your needs (e.g. postgresql). Then you need to alter the following classes:

- Models
    - `features/user/User.java`
    - `features/user/password/reset/PasswordToken.java`
    - `security/authorizationserver/clientdetails/ClientDetails.java`


- Repositories
    - `features/user/UserRepository.java`
    - `features/user/password/reset/PasswordTokenRepository.java`
    - `security/authorizationserver/clientdetails/ClientDetailsRepository.java`


- Properties
    - `application.properties`

#### 3.3.1 Mysql
The mysql configuration depends on `spring-boot-starter-data-jpa`.

- Hibernate is not configured to create the database. Either implement the database mappings in the models and enable `spring.jpa.hibernate.ddl-auto=create`
  or use `resources/db/demo-db.sql` to create the database with some demo data.


- Add the environment variables:
    ```
    DB_URI=<DATABASE_URI>
    DB_USERNAME=<DATABASE_USERNAME>
    DB_PASSWORD=<DATABASE_PASSWORD>
    ```

#### 3.3.2. Mongo
The mongo configuration depends on `spring-boot-starter-data-mongodb`.

- Mongodb automatically creates collections as data is added.
  If you want the collections to have schema validation then load the script `run-all.js` in the mongo shell.
  For example, `load("C:/path/to/project/src/main/resources/db/demo-db.js")`


- By default, `spring.data.mongodb.authentication-database` is set to `none`. If you need to authenticate to your database,
  set this to your authentication method and configure accordingly.


### 3.4. Email
For sending emails add the following Environment Variables:
```
EMAIL_USERNAME=<COMPANY_EMAIL_LOGIN>
EMAIL_PASSWORD=<COMPANY_EMAIL_PASSWORD>
```

The **forgot password** functionality will use these credentials. In `features/email/EmailServiceImpl.java` set the following variables in `sendForgotPasswordEmail` method:
- `mailFrom=<EMAIL_FROM_ADDRESS>`
- `resetPasswordUrl=<FRONTEND_RESET_PASSWORD_URL>`
- (optional) add your own subject in `helper.setSubject()`
- (optional) add your own email template in `helper.setText()`

### 3.5. Exception Handling
The global configuration for returning exception to the client is done in `exceptionhandler/GlobalExceptionHandler.java`.
The app is configured to return all errors in a unified format specified in `exceptionhandler/response/ErrorResponse.java`. For example:

```
{
    "error": "INVALID_ARGUMENTS",
    "description": "Invalid arguments have been supplied.",
    "code": 400,
    "details": {
    "userName": "Length must be between 4 and 24."
    }
}

{
    "error": "URI_NOT_FOUND",
    "description": "Uri with path /user not found.",
    "code": 404
}
```

### 3.6. HTTPS
By default, https is disabled. Follow the steps to enable with a self-signed certificate:

1. `keytool -genkey -keyalg RSA -storetype PKCS12 -keysize 2048 -alias <KEYSTORE_ALIAS> -keystore <KEYSTORE_FILE.jks> -storepass <KEYSTORE_PASSWORD>`
2. Copy the generated keystore (.jks) to project resources.
3. Add the following properties in application.properties
    ```
    server.ssl.enabled=true
    server.ssl.key-store-type=JKS
    server.ssl.key-store=classpath:<KEYSTORE.jks>
    server.ssl.key-store-password=<KEYSTORE_PASSWORD>
    server.ssl.key-alias=<KEYSTORE_ALIAS>
    ```
### 3.7. Additional Information
- Endpoints are stored in utils/Endpoints.java
- An example of a custom bean validator is available in `validator/nowhitespace`.
- The JaCoCo maven plugin is added for extra test results run `mvn clean verify`. JacoCo report located in `target/site/jacoco/index.html`
- By default, the application supports **en** and **nl** locale. This can be configured in `config/LocaleConfig.java`. Validation messages are configured to use `resources/messages.properties`. The utility class `util/MessageResolver` can be used to retrieve messages.
- Validation of incoming requests is done using the `@Validated` annotation which allows grouping of validations so a bean (e.g. for creating and updating a user) can be validated differently for multiple use cases.
