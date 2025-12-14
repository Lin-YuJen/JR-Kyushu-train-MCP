# Set UTF-8
chcp 65001
# default port 6274
$env:CLIENT_PORT=8080;
# default port 6277
$env:SERVER_PORT=9000;

npx -y @modelcontextprotocol/inspector `
    java -jar `
    ./build/libs/Kyushu-train-MCP-all.jar
