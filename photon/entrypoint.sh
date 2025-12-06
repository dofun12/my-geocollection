#!/bin/bash
set -e

DATA_DIR="/photon/photon_data"

# Check if the index already exists (is not empty)
if [ "$(ls -A $DATA_DIR)" ]; then
    echo "Photon data directory found and not empty at $DATA_DIR. Starting Photon..."
else
    echo "Photon data directory is empty. Starting import process..."
    
    # Download Brazil PBF if not present
    if [ ! -f "brazil.pbf" ]; then
        echo "Downloading Brazil map data from Geofabrik..."
        wget -O brazil.pbf https://download.geofabrik.de/south-america/brazil-latest.osm.pbf
    fi
    
    echo "Importing Brazil data (this may take a while)..."
    java -jar photon.jar -import brazil.pbf
    
    echo "Import complete. Cleaning up PBF file..."
    rm brazil.pbf
fi

echo "Starting Photon Server..."
java -jar photon.jar
