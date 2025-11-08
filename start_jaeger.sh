GUI_PORT=16686
DATA_PORT=4317

docker run -d \
    -e COLLECTOR_OTLP_ENABLED=true \
    -p ${GUI_PORT}:${GUI_PORT} \
    -p ${DATA_PORT}:${DATA_PORT} \
    jaegertracing/all-in-one:latest

echo "http://localhost:${GUI_PORT}"
