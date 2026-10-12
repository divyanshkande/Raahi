import React from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import { motion } from "framer-motion";
import L from "leaflet";
import "leaflet/dist/leaflet.css";


delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",

});

// Beautiful custom icon (safe fallback)
const createIcon = () => {
  return L.divIcon({
    className: "custom-marker",
    html: `
      <div style="position: relative; width: 40px; height: 40px;">
        <div style="position: absolute; inset: 0; background: white; border-radius: 50%; box-shadow: 0 4px 15px rgba(0,0,0,0.2); border: 4px solid #6366f1; display: flex; align-items: center; justify-content: center;">
          <span style="font-size: 20px;">📍</span>
        </div>
      </div>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 40],
    popupAnchor: [0, -40],
  });
};


function FlyToPlace({ place }) {
  const map = useMap();

  React.useEffect(() => {
    if (!place?.lat || !place?.lng) return;

    const lat = parseFloat(place.lat);
    const lng = parseFloat(place.lng);

    if (isNaN(lat) || isNaN(lng)) return;

    map.flyTo([lat, lng], 16, { duration: 1.8 });
  }, [place, map]);

  return null;
}

export default function MapView({ itinerary, selectedPlace }) {

  const { places, unverifiedCount } = React.useMemo(() => {
    if (!itinerary || typeof itinerary !== "object") return { places: [], unverifiedCount: 0 };

    const allPlaces = [];
    let unverifiedCount = 0;

    Object.values(itinerary).forEach(day => {
      if (!day || typeof day !== "object") return;

      Object.values(day).forEach(period => {
        if (Array.isArray(period)) {
          period.forEach(p => {
            if (p && p.name && p.lat !== undefined && p.lng !== undefined) {
              const lat = parseFloat(p.lat);
              const lng = parseFloat(p.lng);
              if (!isNaN(lat) && !isNaN(lng)) {
                const verified = p.coordinatesVerified !== false;
                if (!verified) unverifiedCount++;
                allPlaces.push({ ...p, lat, lng, coordinatesVerified: verified });
              }
            }
          });
        } else if (period && period.name && period.lat !== undefined && period.lng !== undefined) {
          const lat = parseFloat(period.lat);
          const lng = parseFloat(period.lng);
          if (!isNaN(lat) && !isNaN(lng)) {
            const verified = period.coordinatesVerified !== false;
            if (!verified) unverifiedCount++;
            allPlaces.push({ ...period, lat, lng, coordinatesVerified: verified });
          }
        }
      });
    });

    return { places: allPlaces, unverifiedCount };
  }, [itinerary]);

  const center = places.length > 0
    ? [places[0].lat, places[0].lng]
    : [26.9124, 75.7873];

  if (places.length === 0) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="w-full h-[500px] bg-white rounded-3xl shadow-2xl border border-gray-200 flex items-center justify-center"
      >
        <div className="text-center">
          <div className="text-9xl mb-6">🗺️</div>
          <h3 className="text-3xl font-bold text-gray-800">No Locations Yet</h3>
          <p className="text-gray-500 mt-4">Generate your trip to see the map!</p>
        </div>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      className="relative w-full h-[500px] rounded-3xl shadow-2xl overflow-hidden border border-gray-200 bg-white"
    >
      <MapContainer center={center} zoom={11} className="h-full w-full" zoomControl={false}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />

        {places.map((place, idx) => (
          place.coordinatesVerified ? (
            <Marker
              key={`${place.name}-${idx}`}
              position={[place.lat, place.lng]}
              icon={createIcon()}
            >
              <Popup>
                <div className="p-5 bg-white rounded-2xl min-w-[280px]">
                  <h3 className="text-xl font-bold text-indigo-700 mb-2">📍 {place.name}</h3>
                  <p className="text-gray-600 text-sm mb-4">{place.description || "Amazing place to visit!"}</p>
                  {place.mapsUrl && (
                    <a
                      href={place.mapsUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-center bg-indigo-600 text-white font-bold py-3 rounded-xl hover:bg-indigo-700 transition"
                    >
                      🧭 Directions
                    </a>
                  )}
                  {!place.mapsUrl && (
                    <div className="block text-center bg-amber-100 text-amber-800 font-medium py-3 rounded-xl border border-amber-200">
                      📍 Location approximate - verify before traveling
                    </div>
                  )}
                </div>
              </Popup>
            </Marker>
          ) : null
        ))}

        <FlyToPlace place={selectedPlace} />
      </MapContainer>

      {unverifiedCount > 0 && (
        <div className="absolute bottom-4 left-4 right-4 z-10">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 mx-4 shadow-lg"
          >
            <div className="flex items-start gap-3">
              <span className="text-amber-600 text-xl flex-shrink-0 mt-0.5">⚠️</span>
              <div className="text-sm text-amber-800 flex-1">
                <p className="font-semibold">{unverifiedCount} location{unverifiedCount === 1 ? '' : 's'} have approximate coordinates</p>
                <p className="mt-1">Multiple stops share the same coordinates (likely city center). Map markers hidden for these stops. Please verify locations independently.</p>
              </div>
            </div>
          </motion.div>
        </div>
      )}

      <div className="absolute top-4 left-4 z-10 bg-white/95 backdrop-blur rounded-2xl px-6 py-4 shadow-xl border border-gray-200">
        <h2 className="text-2xl font-bold text-indigo-700">Your Trip</h2>
        <p className="text-sm text-gray-600">{places.length} Destinations</p>
      </div>
    </motion.div>
  );
}