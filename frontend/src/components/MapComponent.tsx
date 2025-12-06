import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, useMapEvents } from 'react-leaflet';
import { LatLng } from 'leaflet';
import { POI, MapState } from '../types/types';
import POIMarker from './POIMarker';

interface MapComponentProps {
    pois: POI[];
    onMapClick: (lat: number, lon: number) => void;
    onEditPOI: (poi: POI) => void;
    onDeletePOI: (id: number) => void;
}

const MAP_STATE_KEY = 'geocollection_map_state';
const DEFAULT_CENTER: [number, number] = [-23.5505, -46.6333]; // São Paulo
const DEFAULT_ZOOM = 12;

/**
 * Component to handle map events.
 */
const MapEventHandler: React.FC<{
    onMapClick: (lat: number, lon: number) => void;
    onMapMove: (center: [number, number], zoom: number) => void;
}> = ({ onMapClick, onMapMove }) => {
    const map = useMapEvents({
        click: (e: { latlng: LatLng }) => {
            onMapClick(e.latlng.lat, e.latlng.lng);
        },
        moveend: () => {
            const center = map.getCenter();
            const zoom = map.getZoom();
            onMapMove([center.lat, center.lng], zoom);
        },
        zoomend: () => {
            const center = map.getCenter();
            const zoom = map.getZoom();
            onMapMove([center.lat, center.lng], zoom);
        },
    });

    return null;
};

/**
 * Main map component with POI markers and state persistence.
 */
const MapComponent: React.FC<MapComponentProps> = ({
    pois,
    onMapClick,
    onEditPOI,
    onDeletePOI,
}) => {
    const [mapState, setMapState] = useState<MapState>(() => {
        // Load initial map state from localStorage
        try {
            const saved = localStorage.getItem(MAP_STATE_KEY);
            if (saved) {
                const parsed = JSON.parse(saved);
                return {
                    center: parsed.center || DEFAULT_CENTER,
                    zoom: parsed.zoom || DEFAULT_ZOOM,
                };
            }
        } catch (error) {
            console.error('Error loading map state:', error);
        }
        return {
            center: DEFAULT_CENTER,
            zoom: DEFAULT_ZOOM,
        };
    });

    const saveMapState = useCallback((center: [number, number], zoom: number) => {
        const newState: MapState = { center, zoom };
        setMapState(newState);
        try {
            localStorage.setItem(MAP_STATE_KEY, JSON.stringify(newState));
        } catch (error) {
            console.error('Error saving map state:', error);
        }
    }, []);

    return (
        <div className="map-container">
            <MapContainer
                center={mapState.center}
                zoom={mapState.zoom}
                className="h-full w-full"
                zoomControl={true}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    maxZoom={19}
                />

                <MapEventHandler
                    onMapClick={onMapClick}
                    onMapMove={saveMapState}
                />

                {pois.map((poi) => (
                    <POIMarker
                        key={poi.id}
                        poi={poi}
                        onEdit={onEditPOI}
                        onDelete={onDeletePOI}
                    />
                ))}
            </MapContainer>
        </div>
    );
};

export default MapComponent;
