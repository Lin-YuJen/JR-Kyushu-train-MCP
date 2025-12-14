# JR-Kyushu-train-MCP

This MCP Server provides the function of searching the JR Kyushu train information.

There are two tools:

1. `search-kyushu-JR-stations`
    - You can find the stations names of JR Kyushu train by given keyword.
2. `search-kyushu-JR-train`
    - You can find the train information of JR Kyushu train by given conditions.
        - Departure station name
        - Departure time
        - Arrival station name
        - Arrival time
        - Train name
        - Ticket
            * type
            * price
            * Availability

# Installation

**You need to install Java 21 to use it.**

Download the jar file on the release page, then put it in any location you want.

This is an example of configuration in Claude Desktop:

```json
{
  "mcpServers": {
    "Kyushu-train-MCP": {
      "command": "java",
      "args": [
        "-jar",
        "D:\\the-path-of-jar-you-put\\Kyushu-train-MCP-all.jar"
      ]
    }
  }
}
```

* Change the path to your jar file.

# Tech stack

- **Language**: Kotlin 2.2.20 (JVM 21)
- **MCP Framework**: [Model Context Protocol Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk) 0.7.2
- **Server Framework**: [Ktor](https://ktor.io/) 3.3.1
  - Content Negotiation
  - Kotlinx Serialization (JSON)
  - Dependency Injection
  - YAML Configuration
- **HTTP Client**: [OkHttp](https://square.github.io/okhttp/) 5.3.0
- **HTML Parser**: [Ksoup](https://github.com/fleeksoft/ksoup) 0.2.5
- **Build Tool**: Gradle with Kotlin DSL
- **Logging**: SLF4J 2.0.17
- **Packaging**: Shadow JAR plugin for creating fat JARs

