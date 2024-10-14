# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIcDcuj3ZfF5vD6L9sgwr5iWw63O+nxPF+SwfgC5wFrKaooOUCAHjysL7oeqLorE2IJoYLphm65TTuYhB6gatJvqMJpEuGFoctyvIGoKwowKK4qutKSaXvB5SMdojrOlxTryjAmxgCAqRVPoqiwhB7zYXKuEEvhLIerqmSxv604hux5qRhy0YwBp8YKZ2gFpmhPLZrmmBmZ2xZpsBFbjq2n5Sd+C5tvZnbZD2MD9oOvSviOowea5CyQc5v7Lqu3h+IEXgoOge4Hr4zDHukmSYD5F5FNQ17SAAoruBX1AVzQtA+qhPt006zugf5smZhFBnVaA2TBHZwUJCEwEh9ipQGLWNug8nwRqymkhwKDcOpQaDXWw1oFRTJurRpTSNNFKGBprVCmEtWLfxibJh15kpb6VkIHmp12VeDkHF5nFdjkYB9gOQ5LpwMXroEkK2ru0IwAA4qOrLpaeWXnswXXXkDJXlfYo41UNc4NSdQIlgdqNmV14K9dCIOjNJGEYqN3XjdRBEwOSYAafNM6LctZoRoUlr0TGQZMftKP1TpEa48JRl8ThFMrSp+OxITaiwkzNF6eU7OGcqUtc4ZPNtXzt3cXuBOjsZY1PU1OuS6Dl3XRjIIww5UyI0T4yVP0tsoAAktI9sAIy9gAzAALE8J6ZAaFYTF8OgIKADZB6BfRfE7AByo4h3sMCNA9d3eVDb0BT0Nug-bFSO6Orse97ftTAH+oUfcMdPGHEdRyFIdPPHicx8nqemNFnixRu2A+FA2DcPAamGFLKQZWeL1svZ5Q3g0CNI8E6tDi3oxp3l-5nBbVx9FjzZgX0q-V22tkCz1np6lL8IoGipNHYpMgTe61MUnTe9LaGlMcazdE8hzcbaD2jAd+n8xa6TPuUIW8h77TwUuUC+mQVbaFhEfMmAlH5f1JDABBKAr5IPkLLVa8sDL4OADAw2p1yhwBHlLM27ULZaxKDvJ2xdyie19uvY4T1sqvT8u9QKLC3ZsNLp9Fc3cfoBEsNNJCyQYAACkIA8mBqOQIdcQANkhlPK2s8qiUjvC0J2yMFpziHIPYAUioBwAgEhKAsxBGcM6gBShpZnjhwsVYmxH5piHyLkI6su91ZQXoamWB3EABWii0BXxJlhchSlMHPxpm-dWhDv7sgVn-NWAD5BAJAZrJ6OFIGc2FiZPCCSyQUivoI1Jukf4ZN5KQoBgjQHM0YXjUhcTcrdXgdY6AccIBgHqMAOcMk3GUA8dANBiZ4lgOfsAG0ZFWIoDsb42YZj3G9KgDUlm6SDJLJWaMV2ayxmWM2TAHkkBlGjH1uTCh28YARJ5LQtQ1lT7aNLA4zePCs4fTMF9cRcUAheHMV2L0sBgDYEHiRAgiRx4Qx4aEphlRCrFVKuVYwaMnH3JANwPAjI9AGBlsE2Cgk8Y4rBQoSFyAQDxESESkWZTZnwNxVABQyoiX5LqRtGahheLQM5ek7lW0snBj4gKy0QrMhXJQDc506NUxUJZZSqFNKYVoDoW8wSQFPkZxej83oojMBAA
