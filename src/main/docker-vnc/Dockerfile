# This image adds a VNC server on top of the base image (which is build with Jib)
FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
        openjdk-11-jre \
        # virtual display and VNC server
        x11vnc xvfb && \
        apt-get clean -y

RUN useradd -ms /bin/bash default
COPY /docker-entry.sh .
RUN chmod 775 /docker-entry.sh

EXPOSE 5900
USER default
WORKDIR /home/default
COPY --from=registry.hub.docker.com/tigersmannheim/auto-referee:latest /app /app
COPY --from=registry.hub.docker.com/tigersmannheim/auto-referee:latest /config /home/default/config
ENTRYPOINT ["/docker-entry.sh"]
