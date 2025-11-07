GUI_PORT=16686
DATA_PORT=14250

docker run -d \
    -p ${GUI_PORT}:${GUI_PORT} \
    -p ${DATA_PORT}:${DATA_PORT} \
    jaegertracing/all-in-one:latest
