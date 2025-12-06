import React from 'react';
import { Marker, Popup } from 'react-leaflet';
import { Icon } from 'leaflet';
import { Edit, Trash2 } from 'lucide-react';
import type { POI } from '../types/types';

interface POIMarkerProps {
    poi: POI;
    onEdit: (poi: POI) => void;
    onDelete: (id: number) => void;
}

// Create custom marker icon
const customIcon = new Icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41],
});

/**
 * Component for displaying a POI marker on the map.
 */
const POIMarker: React.FC<POIMarkerProps> = ({ poi, onEdit, onDelete }) => {
    const handleDelete = () => {
        if (window.confirm(`Are you sure you want to delete "${poi.title}"?`)) {
            onDelete(poi.id);
        }
    };

    return (
        <Marker
            position={[poi.latitude, poi.longitude]}
            icon={customIcon}
            className="custom-poi-marker"
        >
            <Popup maxWidth={300} className="poi-popup">
                <div className="space-y-3">
                    {/* Title */}
                    <h3 className="text-lg font-bold text-gray-800 border-b border-gray-200 pb-2">
                        {poi.title}
                    </h3>

                    {/* Image */}
                    {poi.imageBase64 && (
                        <div className="w-full">
                            <img
                                src={poi.imageBase64}
                                alt={poi.title}
                                className="w-full h-48 object-cover rounded-lg shadow-md"
                            />
                        </div>
                    )}

                    {/* Description */}
                    {poi.description && (
                        <div className="text-sm text-gray-600">
                            <p className="line-clamp-3">{poi.description}</p>
                        </div>
                    )}

                    {/* Coordinates */}
                    <div className="text-xs text-gray-500 font-mono bg-gray-50 p-2 rounded">
                        📍 {poi.latitude.toFixed(6)}, {poi.longitude.toFixed(6)}
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-2 pt-2">
                        <button
                            onClick={() => onEdit(poi)}
                            className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                        >
                            <Edit size={16} />
                            Edit
                        </button>
                        <button
                            onClick={handleDelete}
                            className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
                        >
                            <Trash2 size={16} />
                            Delete
                        </button>
                    </div>

                    {/* Timestamps */}
                    <div className="text-xs text-gray-400 border-t border-gray-100 pt-2">
                        <div>Created: {new Date(poi.createdAt).toLocaleString()}</div>
                        <div>Updated: {new Date(poi.updatedAt).toLocaleString()}</div>
                    </div>
                </div>
            </Popup>
        </Marker>
    );
};

export default POIMarker;
