# Obpf4J Lobby Server

Obpf4J Lobby Server is a backend server designed for managing multiplayer lobbies
for [Obpf4J Tetris Clients](https://github.com/nimazzo/obpf-tetris-4j). The Obpf4J Lobby Server uses the Tetris game
server implementation provided by
the [OpenBrickProtocolFoundation Simulator](https://github.com/OpenBrickProtocolFoundation/simulator) project in order
to launch and manage game instances. Built with Spring Boot, it handles lobby creation, game instance management, and
user authentication.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Docker Containers](#Docker-Containers)
- [Usage](#usage)
- [Tests](#tests)

## Requirements

In order to run the Obpf4J Lobby Server, you need to have the following installed:

- JDK 22
- Docker (Including Docker Compose)
- CMake (only for building the game server executable)
- C++ compiler (only for building the game server executable)

### Game Server Executable

As the Obpf4J Lobby Server will be responsible for launching the actual game instances upon lobby creation, you need to
have a compiled game server executable available on your system.
You can download the source code for the game server
at [OpenBrickProtocolFoundation/simulator](https://github.com/OpenBrickProtocolFoundation/simulator) (Latest known
working commit: 43f8401f87815a722a9e45b893e4604d85881ec9).

```sh
$ git clone --no-checkout https://github.com/OpenBrickProtocolFoundation/simulator.git
$ cd simulator
$ git checkout 43f8401f87815a722a9e45b893e4604d85881ec9
$ mkdir build
$ cd build
$ cmake ..
$ cmake --build . --target server
```

After compiling, the created executable can be found at `simulator/build/bin/server/server` (or
`simulator/build/bin/server/Debug/server.exe` on Windows using MSVC)

#### Tested with

| OS           | CMake Version | Compiler                                  |
|--------------|---------------|-------------------------------------------|
| Windows 10   | 3.30.0        | Visual Studio 17 2022, MSVC 19.40.33808.0 |
| Ubuntu 24.04 | 3.30.4        | g++ 13.2.0                                |

## Installation

To build and run the Obpf4J Lobby Server locally, follow these steps:

1. **Clone the repository:**
   ```sh
   $ git clone https://github.com/nimazzo/lobby-server.git
   $ cd lobby-server
   ```
2. **Set the path to the game server executable:**
   Open the [
   `application.properties`](https://github.com/nimazzo/lobby-server/blob/main/src/main/resources/application.properties)
   file found under `lobby-server/src/main/resources` and set the file path (relative to your working directory or as an
   absolute file path) to the previously created `server` (`server.exe` on Windows) executable as the value of the
   `game.server.executable-name` property.


4. **Run the Obpf4J Lobby Server:**
   This will automatically create and run the required docker containers as defined in the [
   `compose.yaml`](https://github.com/nimazzo/lobby-server/blob/main/compose.yaml) file and start the spring boot
   application.
   ```sh
   $ ./mvnw spring-boot:run
   ```

6. *(Optional)* **Package the Obpf4J Lobby Server as a standalone JAR:**
   ```sh
   $ ./mvnw clean verify
   ```

7. *(Optional)* **Run the Obpf4J Lobby Server as a standalone JAR:**

    - Start the Docker containers:
      ```sh
      $ docker compose up -d
      ```

    - Configure the datasource:
      **Important**: Set the datasource URL, username, and password before running the standalone JAR. You can add these
      properties to the [
      `application.properties`](https://github.com/nimazzo/lobby-server/blob/main/src/main/resources/application.properties)
      file before building the project, or you can create an `application.properties` file at your working directory
      with the following properties:
      ```properties
      spring.datasource.url=YOUR_JDBC_URL_GOES_HERE
      spring.datasource.username=YOUR_DB_USERNAME_GOES_HERE
      spring.datasource.password=YOUR_DB_PASSWORD_GOES_HERE
      ```
      Or define them as environment variables before running your jar file:
      ```sh
      export SPRING_DATASOURCE_URL=YOUR_JDBC_URL_GOES_HERE
      export SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME_GOES_HERE
      export SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD_GOES_HERE
      ```

    - Run the Obpf4J Lobby Server:
      ```sh
      $ java -jar ./target/lobbyserver-0.0.1-SNAPSHOT.jar
      ```

## Docker Containers

This project uses Docker Compose to manage the following containers:

1. **PostgreSQL**
    - **Image**: `postgres:latest`
    - **Container Name**: `postgres`
    - **Environment Variables**:
        - `POSTGRES_DB`: Name of the database (default: `mydatabase`)
        - `POSTGRES_USER`: Database user (default: `myuser`)
        - `POSTGRES_PASSWORD`: Password for the database user (default: `secret`)
    - **Ports**: Exposes port `5432` for database access.

2. **pgAdmin**
    - **Image**: `dpage/pgadmin4`
    - **Container Name**: `pgadmin`
    - **Environment Variables**:
        - `PGADMIN_DEFAULT_EMAIL`: Default email for pgAdmin login (default: `admin@admin.com`)
        - `PGADMIN_DEFAULT_PASSWORD`: Default password for pgAdmin login (default: `admin`)
    - **Ports**: Exposes port `8081` for the web interface.

3. **MailHog**
    - **Image**: `mailhog/mailhog:latest`
    - **Container Name**: `mailhog`
    - **Ports**:
        - `1025`: SMTP server
        - `8025`: Web UI for viewing emails.

## Usage

Once the server is running, it will be accessible at `http://localhost:8080`. You can interact with the server using the
various API endpoints and through the web interface it provides.

### Web Interface

Frontend interfaces using Thymeleaf templates are available for the routes `/`, `/login` and `/register`. If enabled,
actuator endpoints can be found under `/actuator`.

### REST API

#### Lobby API

+ `/lobby` (GET)
+ `/lobby/create` (POST)
+ `/lobby/join/{lobbyId}` (POST)
+ `/lobby/leave/{lobbyId}` (POST)

#### User Account API

+ `/login` (GET)
+ `/login` (POST)
+ `/register` (GET)
+ `/register` (POST)
+ `/user` (GET)
+ `/user/register` (POST)
+ `/verify` (GET)

#### Game API

Provides an endpoint to retrieve stored game results. Supports paging and sorting. An optional query parameter
`username` can be passed to filter for game results belonging to a specific user.

+ `/game-results` (GET)

#### CSRF

All unsafe endpoints (e.g. POST requests) are CSRF protected. For interaction through the web interface (e.g. creating
new account, login, logout) this is handled automatically. However, for requests performed through API clients, it is
necessary to first aquire a valid CSRF token. For this purpose the `/csrf` endpoint provides a way for the API clients
to manually request a CSRF token.

+ `/csrf` (GET)

### Custom Actuator Endpoints

This Spring Boot application includes custom actuator endpoints to provide additional monitoring and management
capabilities. Below is a description of the custom endpoints available:

#### Enabling Custom Endpoints

Actuator endpoints are configured and enabled in the `application-dev.properties` properties file. The default
`application.properties` file activates this profile (`dev`) and thus enables and exposes the actuator endpoints at
`/actuator`. All actuator endpoints are secured and require an authenticated user with `ROLE_ADMIN` to be accessed.

#### `lobbiesReset` Endpoint

*URL:* `/actuator/lobbies/reset`

*Description:* Resets all active lobbies and terminates all game instances.

*Example Response*:

```json
{
  "deletedGameInstances": 3,
  "deletedLobbies": 3
}
```

#### `serverLogs` and `serverLogs-lobbyId` Endpoints

*URL:* `/actuator/server/logs`

*Description:* Provides access to the generated game server logs. Returns a list of all available logs and their URL.

*Example Response*:

```json
{
  "logs": [
    {
      "lobbyId": 1,
      "logFileUri": "http://localhost:8080/actuator/server/logs/1"
    },
    {
      "lobbyId": 2,
      "logFileUri": "http://localhost:8080/actuator/server/logs/2"
    },
    {
      "lobbyId": 3,
      "logFileUri": "http://localhost:8080/actuator/server/logs/3"
    }
  ],
  "count": 3
}
```

*URL:* `/actuator/server/logs/{lobbyId}`

*Description:* Returns the current contents of the log file for the specified `lobbyId`.

*Example Response*:

```plaintext
[2024-09-29 15:54:10.801] [info] lobby port = 53302
[2024-09-29 15:54:10.802] [info] starting gameserver
[2024-09-29 15:54:10.806] [info] not all clients have connected yet, number of clients: 0
[2024-09-29 15:54:10.806] [info] expected player count: 3
...
```

## Tests

This project provides various test classes including unit tests, Spring Boot WebMvc tests slices for controllers and
DataJpa test slices for database repositories using Testcontainers.
