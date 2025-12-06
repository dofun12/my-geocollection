import React, { useState, useEffect } from 'react';
import { nominatimApi, type NominatimResult } from '../services/api';

interface AddressAutocompleteProps {
    onLocationSelect: (lat: number, lon: number, address: string) => void;
    initialValue?: string;
}

/**
 * Address autocomplete component using Nominatim API.
 */
const AddressAutocomplete: React.FC<AddressAutocompleteProps> = ({
    onLocationSelect,
    initialValue = '',
}) => {
    const [query, setQuery] = useState(initialValue);
    const [results, setResults] = useState<NominatimResult[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [showResults, setShowResults] = useState(false);
    const [debounceTimeout, setDebounceTimeout] = useState<number | null>(null);

    useEffect(() => {
        // Cleanup timeout on unmount
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
            }, 500);
            setDebounceTimeout(timeout);
        } else {
            setResults([]);
            setShowResults(false);
        }
    };

    const searchAddress = async (searchQuery: string) => {
        setIsLoading(true);
        try {
            const data = await nominatimApi.search(searchQuery);
            setResults(data);
            setShowResults(true);
        } catch (error) {
            console.error('Error searching address:', error);
            setResults([]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSelectResult = (result: NominatimResult) => {
        setQuery(result.display_name);
        setShowResults(false);
        onLocationSelect(
            parseFloat(result.lat),
            parseFloat(result.lon),
            result.display_name
        );
    };

    return (
        <div className="relative">
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                onFocus={() => results.length > 0 && setShowResults(true)}
                placeholder="Search for an address (e.g., Avenida Paulista, São Paulo)"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />

            {isLoading && (
                <div className="absolute right-3 top-3">
                    <div className="animate-spin h-5 w-5 border-2 border-blue-500 border-t-transparent rounded-full"></div>
                </div>
            )}

            {showResults && results.length > 0 && (
                <div className="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto custom-scrollbar">
                    {results.map((result) => (
                        <button
                            key={result.place_id}
                            onClick={() => handleSelectResult(result)}
                            className="w-full px-4 py-3 text-left hover:bg-blue-50 transition-colors border-b border-gray-100 last:border-b-0"
                        >
                            <div className="font-medium text-gray-900 text-sm">
                                {result.display_name}
                            </div>
                        </button>
                    ))}
                </div>
            )}

            {showResults && results.length === 0 && !isLoading && query.length >= 3 && (
                <div className="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg px-4 py-3 text-gray-500 text-sm">
                    No results found. Try a different search.
                </div>
            )}
        </div>
    );
};

export default AddressAutocomplete;
