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