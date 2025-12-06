import axios from 'axios';
import type { POI, CreatePOIRequest, UpdatePOIRequest, NominatimResult } from '../types/types';

// Re-export types for convenience
export type { NominatimResult };

/**
 * Base URL for API requests.
 * In development, Vite proxy will forward /api requests to http://localhost:8080
 */
const API_BASE_URL = '/api/pois';

/**
 * Axios instance for API calls.
 */
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('auth_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor to handle 401 unauthorized
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Clear auth data and redirect to login
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

/**
 * POI API service.
 */
export const poiApi = {
    /**
     * Get all POIs.
     */
    getAll: async (): Promise<POI[]> => {
        const response = await apiClient.get<POI[]>('');
        return response.data;
    },

    /**
     * Get a POI by ID.
     */
    getById: async (id: number): Promise<POI> => {
        const response = await apiClient.get<POI>(`/${id}`);
        return response.data;
    },

    /**
     * Create a new POI.
     */
    create: async (data: CreatePOIRequest): Promise<POI> => {
        const response = await apiClient.post<POI>('', data);
        return response.data;
    },

    /**
     * Update an existing POI.
     */
    update: async (id: number, data: UpdatePOIRequest): Promise<POI> => {
        const response = await apiClient.put<POI>(`/${id}`, data);
        return response.data;
    },

    /**
     * Delete a POI.
     */
    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/${id}`);
    },

    /**
     * Set a POI as home.
     */
    setAsHome: async (id: number): Promise<POI> => {
        const response = await apiClient.post<POI>(`/${id}/set-home`);
        return response.data;
    },

    /**
     * Unset a POI as home.
     */
    unsetAsHome: async (id: number): Promise<POI> => {
        const response = await apiClient.delete<POI>(`/${id}/unset-home`);
        return response.data;
    },
};

/**
 * Nominatim Geocoding API for address search.
 */
export const nominatimApi = {
    /**
     * Search for addresses using Nominatim.
     */
    search: async (query: string): Promise<NominatimResult[]> => {
        const response = await axios.get<NominatimResult[]>(
            'https://nominatim.openstreetmap.org/search',
            {
                params: {
                    q: query,
                    format: 'json',
                    addressdetails: 1,
                    limit: 5,
                    countrycodes: 'br', // Focus on Brazil for better results
                },
                headers: {
                    'User-Agent': 'MyGeoCollection/1.0', // Required by Nominatim
                },
            }
        );
        return response.data;
    },
};

/**
 * Utility function to convert file to Base64.
 */
export const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            if (typeof reader.result === 'string') {
                resolve(reader.result);
            } else {
                reject(new Error('Failed to convert file to Base64'));
            }
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
};

/**
 * Validate image file.
 */
export const validateImageFile = (file: File): { valid: boolean; error?: string } => {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!validTypes.includes(file.type)) {
        return {
            valid: false,
            error: 'Invalid file type. Only JPEG, PNG, and GIF are allowed.',
        };
    }

    if (file.size > maxSize) {
        return {
            valid: false,
            error: 'File size exceeds 5MB limit.',
        };
    }

    return { valid: true };
};
