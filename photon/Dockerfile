FROM eclipse-temurin:17-jre

WORKDIR /photon

# Install wget to download data
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

# Download Photon JAR (v0.5.0)
RUN wget -O photon.jar https://github.com/komoot/photon/releases/download/0.5.0/photon-0.5.0.jar

# Copy entrypoint script
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Create data directory volume mount point
VOLUME /photon/photon_data

EXPOSE 2322

ENTRYPOINT ["./entrypoint.sh"]
