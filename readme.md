# Clipystream Spring

[![Clipystream Spring Actions Status](https://github.com/trodix/clipystream/actions/workflows/maven.yml/badge.svg)](https://github.com/trodix/clipystream/actions)

> A short video clip sharing service

## Development setup

You can configure development properties in **src/resources/application-dev.properties** file.

### Database

By default, a H2 in memory database is used for development purposes.

### Rate limit

The rate limit feature uses [Redis](https://redis.io) as in-memory database.

By default, the redis connector will connect on `localhost:6379`

If you need to use a different configuration, you can define it in `application.properties` as bellow:

```properties
app.redis.database=0
app.redis.host=localhost
app.redis.port=5000
app.redis.password=password
app.redis.timeout=60000
```

### Run

Run the app with maven: `mvn spring-boot:run`

You can access the api at <http://localhost:8000/api>

### Swagger documentation

For specs, go to <http://localhost:8000/v2/api-docs>

For Swagger UI, go to <http://localhost:8000/swagger-ui/>

## Tests

Postman tests are available at [./src/test/resources](./src/test/resources)

## Installation

1. Build the project to generate the **.jar** file

   `mvn clean install`

2. Copy the **target/clipystream.jar** file to the serveur

    `scp -p target/clipystream.jar root@myserver:/opt/clipystream/clipystream.jar`

3. Create a **application.properties** file at **/opt/clipystream** with production credentials

    ```properties
    spring.profiles.active=prod

    # Driver Postgresql
    spring.datasource.driverClassName=org.postgresql.Driver

    # Allows Hibernate to generate SQL optimized for a particular DBMS
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

    # Connection url for the database (if different to default)
    spring.datasource.url=jdbc:postgresql://localhost:5432/<YOUR_DATABASE_NAME>

    # Username and password
    spring.datasource.username=<YOUR_USERNAME>
    spring.datasource.password=<YOUR_PASSWORD>

    # JWT secret
    app.jwt-secret=<YOUR_SECRET_KEY>
    ```

4. Create a new user for this application

    `useradd -r clipystream`

5. Set permissions to the app directory for the user

    `chown -R clipystream:clipystream /opt/clipystream`

6. Create a systemd service in **/etc/systemd/system/clipystream.service** file

    ```properties
    [Unit]
    Description=Todo SpringBoot Api
    After=syslog.target

    [Service]
    User=clipystream
    ExecStart=/opt/clipystream/clipystream.jar -Dspring.config.location=/opt/clipystream/application.properties
    SuccessExitStatus=143

    [Install]
    WantedBy=multi-user.target
    ```

7. Enable the service for booting the app at server boot

    `service clipystream enable`

8. Start the service

    `service clipystream start`

## Default User

When the application run for the first time, a default user will be generated.

The default user email is: `admin@example.com`

The default username is: `admin`

The default user roles are: `admin`, corresponding to the **ROLE_ADMIN** ERole enum.

You can override this values in your **application.properties** file:

```properties
# ===============================
# = Default admin credentials
# ===============================
app.default-user.username = admin
app.default-user.email = admin@exemple.com
app.default-user.roles = admin
```

The default password is auto-generated and availlable in the logs of the application:

Example of output:

```log
=============== Default credentials are: admin / 62e3766b-934f-4db8-a22a-245eafb220f4 ===============
```

In production environment, you can get the default credentials log by running this command:

`journalctl -u clipystream.service | grep "Default credentials"`

## Configuration

### Bucket configuration

All providers that are compient with the Amazon S3 protocol can be used as described bellow:

```properties
# ===============================
# = BUCKET
# ===============================

# Bucket storage implementation
app.storage.provider.class = com.trodix.clipystream.provider.S3StorageProvider

# S3 complient bucket storage config
app.storage.provider.s3.secret-key = ${S3_BUCKET_PROVIDER_SECRET_KEY:}
app.storage.provider.s3.access-key = <YOUR_ACCESS_KEY>
app.storage.provider.s3.region = fr-par
app.storage.provider.s3.endpoint-bucket-url = https://s3.fr-par.scw.cloud
app.storage.provider.s3.bucket-name = <YOUR_BUCKET_NAME>
```

Then you need to define the `S3_BUCKET_PROVIDER_SECRET_KEY` variable with your bucket secret key

```bash
export S3_BUCKET_PROVIDER_SECRET_KEY=<YOUR_BUCKET_SECRET_KEY>
```

### Rate limit configuration

By default, each user have **60 MB of upload quota per day** and **each uploaded file can not exceed a size of 20 MB**.

If the quota is reached, a `401 UNAUTHORIZED` error will be returned to the user.

The upload quota is reset **24 hours** after the first uploaded file.

You can change thoses values in the `application.properties` file as bellow:

```properties
# Both values need to be changed because max-file-size is limited by max-request-size
spring.servlet.multipart.max-request-size=20MB
spring.servlet.multipart.max-file-size=20MB

# File upload size quota per day
app.storage.max-quota-per-day-mb=60
```

### Allowed file extesions

By default, only `gif,webm,mp4` files are allowed.

If you want to allow more file extentions, you can change thoses values in the `application.properties` file as bellow:

```properties
app.storage.allowed-file-extentions=gif,webm,mp4,avi,mpeg
```

## Custom bucket provider

If you need to use a bucket provider that is not complient with Amazon S3 protocol, you can implement your own provider from the `com.trodix.clipystream.core.interfaces.StorageProvider` interface.

Then you need to load your provider in `application.properties` with `app.storage.provider.class = com.trodix.clipystream.provider.YourCustomProvider`
