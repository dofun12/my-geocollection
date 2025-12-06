import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';

interface AddressAutocompleteProps {
    onLocationSelect: (lat: number, lon: number, address: string) => void;
    initialValue?: string;
}

interface PhotonFeature {
    geometry: {
        coordinates: [number, number]; // [lon, lat]
        type: string;
    };
    properties: {
        name?: string;
        street?: string;
        housenumber?: string;
        city?: string;
        state?: string;
        country?: string;
        postcode?: string;
        osm_value?: string;
    };
}

interface PhotonResponse {
    features: PhotonFeature[];
}

/**
 * Address autocomplete component using Photon API (OpenStreetMap).
 * No API Key required.
 */
const AddressAutocomplete: React.FC<AddressAutocompleteProps> = ({
    onLocationSelect,
    initialValue = '',
}) => {
    const [query, setQuery] = useState(initialValue);
    const [results, setResults] = useState<PhotonFeature[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [showResults, setShowResults] = useState(false);
    const [debounceTimeout, setDebounceTimeout] = useState<number | null>(null);
    const wrapperRef = useRef<HTMLDivElement>(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setShowResults(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [wrapperRef]);

    // Cleanup timeout on unmount
    useEffect(() => {
        return () => {
            if (debounceTimeout) {
                clearTimeout(debounceTimeout);
            }
        };
    }, [debounceTimeout]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setQuery(value);

        // Clear previous timeout
        if (debounceTimeout) {
            clearTimeout(debounceTimeout);
        }

        // Set new timeout for debounced search
        if (value.trim().length >= 3) {
            const timeout = setTimeout(() => {
                searchAddress(value);
            }, 400); // 400ms debounce
            setDebounceTimeout(timeout);
        } else {
            setResults([]);
            setShowResults(false);
        }
    };

    const searchAddress = async (searchQuery: string) => {
        setIsLoading(true);
        try {
            const response = await axios.get<PhotonResponse>('https://photon.komoot.io/api/', {
                params: {
                    q: searchQuery,
                    limit: 5,
                    lang: 'en', // or 'pt' if preferred, but Photon support varies
                }
            });
            setResults(response.data.features);
            setShowResults(true);
        } catch (error) {
            console.error('Error searching address:', error);
            setResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    const formatAddress = (props: PhotonFeature['properties']): string => {
        const parts = [];
        if (props.name) parts.push(props.name);
        if (props.street) parts.push(props.street + (props.housenumber ? ` ${props.housenumber}` : ''));
        if (props.city) parts.push(props.city);
        if (props.state) parts.push(props.state);
        if (props.country) parts.push(props.country);

        // Deduplicate parts
        return [...new Set(parts)].join(', ');
    };

    const handleSelectResult = (feature: PhotonFeature) => {
        const address = formatAddress(feature.properties);
        const [lon, lat] = feature.geometry.coordinates;

        setQuery(address);
        setShowResults(false);
        onLocationSelect(lat, lon, address);
    };

    return (
        <div className="relative" ref={wrapperRef}>
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                onFocus={() => results.length > 0 && setShowResults(true)}
                placeholder="Search for an address (e.g., Avenida Paulista)"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />

            {isLoading && (
                <div className="absolute right-3 top-3">
                    <div className="animate-spin h-5 w-5 border-2 border-blue-500 border-t-transparent rounded-full"></div>
                </div>
            )}

            {showResults && results.length > 0 && (
                <div className="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto custom-scrollbar">
                    {results.map((feature, index) => (
                        <button
                            key={index} // Photon doesn't always provide unique IDs
                            onClick={() => handleSelectResult(feature)}
                            className="w-full px-4 py-3 text-left hover:bg-blue-50 transition-colors border-b border-gray-100 last:border-b-0"
                        >
                            <div className="font-medium text-gray-900 text-sm">
                                {feature.properties.name || feature.properties.street || 'Unknown Place'}
                            </div>
                            <div className="text-xs text-gray-500 truncate">
                                {formatAddress(feature.properties)}
                            </div>
                        </button>
                    ))}
                </div>
            )}

            {showResults && results.length === 0 && !isLoading && query.length >= 3 && (
                <div className="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg px-4 py-3 text-gray-500 text-sm">
                    No results found.
                </div>
            )}
        </div>
    );
};

export default AddressAutocomplete;
