FROM docker-release.registry-ci.delta.sbrf.ru/ci04309835/ci04309835/sngx/sbel9@sha256:b7bdaccdf63cc39f40db8ad7b546b90b4dcf7796410408efd1fed481ad8d2390
COPY build/ /opt/syngx/share
WORKDIR /opt/syngx/share
USER root
RUN chown -R syngx:syngx /opt/syngx/share
EXPOSE 8085
USER syngx
ENTRYPOINT ["syngx", "-g", "daemon off;"]
