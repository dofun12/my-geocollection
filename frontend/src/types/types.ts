/**
 * Point of Interest interface.
 */
export interface POI {
    id: number;
    title: string;
    description?: string;
    latitude: number;
    longitude: number;
    imageBase64?: string;
    isHome?: boolean;
    distanceFromHome?: number | null; // Distance in kilometers, null for home POI
    createdAt: string;
    updatedAt: string;
}

/**
 * Request for creating a new POI.
 */
export interface CreatePOIRequest {
    title: string;
    description?: string;
    latitude: number;
    longitude: number;
    imageBase64?: string;
    isHome?: boolean;
}

/**
 * Request for updating an existing POI.
 */
export interface UpdatePOIRequest {
    title: string;
    description?: string;
    latitude: number;
    longitude: number;
    imageBase64?: string;
    isHome?: boolean;
}

/**
 * Nominatim search result from OpenStreetMap.
 */
export interface NominatimResult {
    place_id: number;
    licence: string;
    osm_type: string;
    osm_id: number;
    lat: string;
    lon: string;
    display_name: string;
    address: {
        road?: string;
        suburb?: string;
        city?: string;
        state?: string;
        country?: string;
        postcode?: string;
    };
    boundingbox: string[];
}

/**
 * Map state for localStorage persistence.
 */
export interface MapState {
    center: [number, number];
    zoom: number;
}
