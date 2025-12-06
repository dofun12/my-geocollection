import { useState, useEffect } from 'react';
import { MapPin, Plus, Loader } from 'lucide-react';
import MapComponent from './components/MapComponent';
import POIModal from './components/POIModal';
import { poiApi } from './services/api';
import { POI, CreatePOIRequest, UpdatePOIRequest } from './types/types';
import './index.css';

function App() {
  const [pois, setPois] = useState<POI[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPOI, setSelectedPOI] = useState<POI | null>(null);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [clickedCoords, setClickedCoords] = useState<{ lat: number; lon: number } | null>(null);

  // Load POIs on mount
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

  const handleModalSubmit = async (data: CreatePOIRequest | UpdatePOIRequest) => {
    if (modalMode === 'create') {
      const newPOI = await poiApi.create(data as CreatePOIRequest);
      setPois((prev) => [...prev, newPOI]);
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

  return (
    <div className="relative h-screen w-screen overflow-hidden bg-gray-100">
      {/* Header */}
      <header className="absolute top-0 left-0 right-0 z-[1000] bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-lg">
        <div className="container mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <MapPin size={32} className="drop-shadow-lg" />
            <div>
              <h1 className="text-2xl font-bold">My Geo Collection</h1>
              <p className="text-xs text-blue-100">Manage your Points of Interest</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-semibold">{pois.length} POIs</p>
              <p className="text-xs text-blue-100">Total locations</p>
            </div>
            <button
              onClick={handleAddNewClick}
              className="flex items-center gap-2 px-5 py-2.5 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-all hover:shadow-xl font-semibold"
            >
              <Plus size={20} />
              <span className="hidden sm:inline">Add New POI</span>
              <span className="sm:hidden">Add</span>
            </button>
          </div>
        </div>
      </header>

      {/* Instructions Banner */}
      <div className="absolute top-20 left-1/2 transform -translate-x-1/2 z-[999] bg-blue-50 border border-blue-200 text-blue-800 px-6 py-3 rounded-lg shadow-lg max-w-xl mx-auto">
        <p className="text-sm text-center">
          <strong>💡 Tip:</strong> Click anywhere on the map to add a new POI at that location!
        </p>
      </div>

      {/* Loading Overlay */}
      {isLoading && (
        <div className="absolute inset-0 z-[1001] bg-white bg-opacity-90 flex items-center justify-center">
          <div className="text-center">
            <Loader size={48} className="animate-spin text-blue-600 mx-auto mb-4" />
            <p className="text-gray-600 font-medium">Loading POIs...</p>
          </div>
        </div>
      )}

      {/* Error Message */}
      {error && !isLoading && (
        <div className="absolute top-32 left-1/2 transform -translate-x-1/2 z-[999] bg-red-50 border border-red-200 text-red-700 px-6 py-3 rounded-lg shadow-lg">
          <p className="text-sm">{error}</p>
          <button
            onClick={loadPOIs}
            className="mt-2 px-4 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
          >
            Retry
          </button>
        </div>
      )}

      {/* Map */}
      <div className="h-full w-full">
        <MapComponent
          pois={pois}
          onMapClick={handleMapClick}
          onEditPOI={handleEditPOI}
          onDeletePOI={handleDeletePOI}
        />
      </div>

      {/* POI Modal */}
      <POIModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleModalSubmit}
        poi={selectedPOI}
        initialLat={clickedCoords?.lat}
        initialLon={clickedCoords?.lon}
        mode={modalMode}
      />
    </div>
  );
}

export default App;
