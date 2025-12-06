import { useState, useEffect } from 'react';
import { MapPin, Plus, Loader, LogOut, Home } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import MapComponent from '../components/MapComponent';
import POIModal from '../components/POIModal';
import { poiApi } from '../services/api';
import type { POI, CreatePOIRequest, UpdatePOIRequest } from '../types/types';

export default function HomePage() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [pois, setPois] = useState<POI[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string>('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedPOI, setSelectedPOI] = useState<POI | null>(null);
    const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
    const [clickedCoords, setClickedCoords] = useState<{ lat: number; lon: number } | null>(null);

    useEffect(() => {
        loadPOIs();
    }, []);

    const loadPOIs = async () => {
        try {
            setIsLoading(true);
            setError('');
            const data = await poiApi.getAll();
            setPois(data);
        } catch (err: any) {
            setError('Failed to load POIs');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    const handleMapClick = (lat: number, lon: number) => {
        setClickedCoords({ lat, lon });
        setSelectedPOI(null);
        setModalMode('create');
        setIsModalOpen(true);
    };

    const handleAddNewClick = () => {
        setClickedCoords(null);
        setSelectedPOI(null);
        setModalMode('create');
        setIsModalOpen(true);
    };

    const handleEditPOI = (poi: POI) => {
        setSelectedPOI(poi);
        setClickedCoords(null);
        setModalMode('edit');
        setIsModalOpen(true);
    };

    const handleDeletePOI = async (id: number) => {
        try {
            await poiApi.delete(id);
            setPois((prev) => prev.filter((poi) => poi.id !== id));
        } catch (err: any) {
            alert('Failed to delete POI');
            console.error(err);
        }
    };

    const handleSetAsHome = async (id: number) => {
        try {
            await poiApi.setAsHome(id);
            await loadPOIs(); // Reload to get updated distances
        } catch (err: any) {
            alert('Failed to set as home');
            console.error(err);
        }
    };

    const handleUnsetAsHome = async (id: number) => {
        try {
            await poiApi.unsetAsHome(id);
            await loadPOIs(); // Reload to get updated distances
        } catch (err: any) {
            alert('Failed to unset home');
            console.error(err);
        }
    };

    const handleModalSubmit = async (data: CreatePOIRequest | UpdatePOIRequest) => {
        if (modalMode === 'create') {
            const newPOI = await poiApi.create(data as CreatePOIRequest);
            setPois((prev) => [...prev, newPOI]);
            await loadPOIs(); // Reload to get updated distances if it was set as home
        } else if (selectedPOI) {
            const updatedPOI = await poiApi.update(selectedPOI.id, data as UpdatePOIRequest);
            setPois((prev) =>
                prev.map((poi) => (poi.id === updatedPOI.id ? updatedPOI : poi))
            );
        }
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedPOI(null);
        setClickedCoords(null);
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const homePOI = pois.find(poi => poi.isHome);

    return (
        <div className="relative h-screen w-screen overflow-hidden">
            {/* Header with user info and logout - hidden when modal is open */}
            {!isModalOpen && (
                <div className="absolute top-0 left-0 right-0 bg-gradient-to-r from-blue-600 to-blue-700 text-white px-6 py-4 shadow-lg z-20">
                    <div className="flex items-center justify-between max-w-7xl mx-auto">
                        <div className="flex items-center gap-3">
                            <MapPin className="w-8 h-8" />
                            <div>
                                <h1 className="text-2xl font-bold">My Geo Collection</h1>
                                <p className="text-sm text-blue-100">Welcome, {user?.username}!</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-4">
                            {homePOI && (
                                <div className="flex items-center gap-2 bg-blue-500 px-4 py-2 rounded-lg">
                                    <Home className="w-4 h-4" />
                                    <span className="text-sm">{homePOI.title}</span>
                                </div>
                            )}
                            <button
                                onClick={handleLogout}
                                className="flex items-center gap-2 bg-blue-500 hover:bg-blue-600 px-4 py-2 rounded-lg transition"
                            >
                                <LogOut className="w-4 h-4" />
                                <span className="hidden sm:inline">Logout</span>
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Instructions banner - hidden when modal is open */}
            {!isModalOpen && (
                <div className="absolute top-20 left-0 right-0 pointer-events-none z-10">
                    <div className="max-w-md mx-auto pointer-events-auto">
                        <div className="bg-white/95 backdrop-blur-sm shadow-lg rounded-b-xl px-4 py-2 text-center border-t-4 border-blue-600">
                            <p className="text-xs text-gray-700">
                                Click anywhere on the map to add a new Point of Interest
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Map Container */}
            <div className="absolute inset-0 z-10">
                <MapComponent
                    pois={pois}
                    onMapClick={handleMapClick}
                    onEditPOI={handleEditPOI}
                    onDeletePOI={handleDeletePOI}
                    onSetAsHome={handleSetAsHome}
                    onUnsetAsHome={handleUnsetAsHome}
                />
            </div>

            {/* Floating Add Button */}
            <button
                onClick={handleAddNewClick}
                className="absolute bottom-8 right-8 bg-gradient-to-r from-blue-600 to-blue-700 text-white p-4 rounded-full shadow-2xl hover:shadow-3xl hover:scale-110 transition-all duration-300 z-30 group"
                aria-label="Add new POI"
            >
                <Plus className="w-6 h-6 group-hover:rotate-90 transition-transform duration-300" />
            </button>

            {/* POI Modal */}
            <POIModal
                isOpen={isModalOpen}
                mode={modalMode}
                poi={selectedPOI}
                initialLat={clickedCoords?.lat}
                initialLon={clickedCoords?.lon}
                onClose={handleCloseModal}
                onSubmit={handleModalSubmit}
            />

            {/* Loading Overlay */}
            {isLoading && (
                <div className="absolute inset-0 bg-black/30 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 flex items-center gap-3">
                        <Loader className="w-6 h-6 animate-spin text-blue-600" />
                        <span className="text-gray-700 font-medium">Loading POIs...</span>
                    </div>
                </div>
            )}

            {/* Error Message */}
            {error && (
                <div className="absolute top-32 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50">
                    {error}
                </div>
            )}
        </div>
    );
}
