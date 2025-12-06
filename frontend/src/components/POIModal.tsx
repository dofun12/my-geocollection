import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import type { POI, CreatePOIRequest, UpdatePOIRequest } from '../types/types';
import { fileToBase64, validateImageFile } from '../services/api';
import AddressAutocomplete from './AddressAutocomplete';

interface POIModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: CreatePOIRequest | UpdatePOIRequest) => Promise<void>;
    poi?: POI | null;
    initialLat?: number;
    initialLon?: number;
    mode: 'create' | 'edit';
}

/**
 * Modal for creating or editing a POI.
 */
const POIModal: React.FC<POIModalProps> = ({
    isOpen,
    onClose,
    onSubmit,
    poi,
    initialLat,
    initialLon,
    mode,
}) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [latitude, setLatitude] = useState<number | string>('');
    const [longitude, setLongitude] = useState<number | string>('');
    const [imageBase64, setImageBase64] = useState<string>('');
    const [imagePreview, setImagePreview] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string>('');

    useEffect(() => {
        if (mode === 'edit' && poi) {
            setTitle(poi.title);
            setDescription(poi.description || '');
            setLatitude(poi.latitude);
            setLongitude(poi.longitude);
            setImageBase64(poi.imageBase64 || '');
            setImagePreview(poi.imageBase64 || '');
        } else if (mode === 'create') {
            setTitle('');
            setDescription('');
            setLatitude(initialLat ?? '');
            setLongitude(initialLon ?? '');
            setImageBase64('');
            setImagePreview('');
        }
        setError('');
    }, [mode, poi, initialLat, initialLon, isOpen]);

    const handleLocationSelect = (lat: number, lon: number, _address: string) => {
        setLatitude(lat);
        setLongitude(lon);
    };

    const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        const validation = validateImageFile(file);
        if (!validation.valid) {
            setError(validation.error || 'Invalid image');
            return;
        }

        try {
            const base64 = await fileToBase64(file);
            setImageBase64(base64);
            setImagePreview(base64);
            setError('');
        } catch (err) {
            setError('Failed to load image');
            console.error(err);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (!title.trim()) {
            setError('Title is required');
            return;
        }

        if (!latitude || !longitude) {
            setError('Coordinates are required');
            return;
        }

        const lat = typeof latitude === 'string' ? parseFloat(latitude) : latitude;
        const lon = typeof longitude === 'string' ? parseFloat(longitude) : longitude;

        if (isNaN(lat) || isNaN(lon)) {
            setError('Invalid coordinates');
            return;
        }

        if (lat < -90 || lat > 90) {
            setError('Latitude must be between -90 and 90');
            return;
        }

        if (lon < -180 || lon > 180) {
            setError('Longitude must be between -180 and 180');
            return;
        }

        setIsSubmitting(true);

        try {
            await onSubmit({
                title: title.trim(),
                description: description.trim() || undefined,
                latitude: lat,
                longitude: lon,
                imageBase64: imageBase64 || undefined,
            });
            onClose();
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to save POI');
            console.error(err);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleRemoveImage = () => {
        setImageBase64('');
        setImagePreview('');
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center modal-overlay">
            <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto custom-scrollbar">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-800">
                        {mode === 'create' ? 'Add New Point of Interest' : 'Edit Point of Interest'}
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        disabled={isSubmitting}
                    >
                        <X size={24} />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    {/* Error message */}
                    {error && (
                        <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
                            {error}
                        </div>
                    )}

                    {/* Title */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Title <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            maxLength={200}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Enter POI title"
                            disabled={isSubmitting}
                        />
                    </div>

                    {/* Description */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Description
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={5000}
                            rows={4}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                            placeholder="Enter POI description"
                            disabled={isSubmitting}
                        />
                    </div>

                    {/* Address Search (only for create mode) */}
                    {mode === 'create' && (
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Search Address
                            </label>
                            <AddressAutocomplete onLocationSelect={handleLocationSelect} />
                            <p className="text-xs text-gray-500 mt-1">
                                Search for an address to automatically fill coordinates
                            </p>
                        </div>
                    )}

                    {/* Coordinates */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Latitude <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="number"
                                step="any"
                                value={latitude}
                                onChange={(e) => setLatitude(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                placeholder="-23.5505"
                                disabled={isSubmitting}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Longitude <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="number"
                                step="any"
                                value={longitude}
                                onChange={(e) => setLongitude(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                placeholder="-46.6333"
                                disabled={isSubmitting}
                            />
                        </div>
                    </div>

                    {/* Image Upload */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Image
                        </label>
                        {!imagePreview ? (
                            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
                                <input
                                    type="file"
                                    accept="image/jpeg,image/jpg,image/png,image/gif"
                                    onChange={handleImageChange}
                                    className="hidden"
                                    id="image-upload"
                                    disabled={isSubmitting}
                                />
                                <label
                                    htmlFor="image-upload"
                                    className="cursor-pointer text-blue-600 hover:text-blue-700 font-medium"
                                >
                                    Click to upload image
                                </label>
                                <p className="text-xs text-gray-500 mt-2">
                                    Max 5MB. JPG, PNG, or GIF.
                                </p>
                            </div>
                        ) : (
                            <div className="relative">
                                <img
                                    src={imagePreview}
                                    alt="Preview"
                                    className="w-full h-64 object-cover rounded-lg"
                                />
                                <button
                                    type="button"
                                    onClick={handleRemoveImage}
                                    className="absolute top-2 right-2 bg-red-500 text-white p-2 rounded-full hover:bg-red-600 transition-colors shadow-lg"
                                    disabled={isSubmitting}
                                >
                                    <X size={20} />
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Saving...' : mode === 'create' ? 'Create POI' : 'Update POI'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default POIModal;
