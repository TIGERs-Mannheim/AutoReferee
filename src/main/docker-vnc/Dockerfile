ARG base_version=latest
FROM registry.hub.docker.com/tigersmannheim/auto-referee:$base_version as jib

FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
        openjdk-17-jre \
        # virtual display and VNC server
        x11vnc xvfb && \
        apt-get clean -y

RUN useradd -ms /bin/bash default
COPY /docker-entry.sh .
RUN chmod 775 /docker-entry.sh

EXPOSE 5900
USER default
WORKDIR /home/default
COPY --from=jib /app /app
COPY --from=jib --chown=default /config /home/default/config
ENTRYPOINT ["/docker-entry.sh"]
