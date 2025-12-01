
import React, { useState, lazy, Suspense } from "react";
import { motion } from "framer-motion";
import CityInput from "./CityInput";
import DaysInput from "./DaysInput";
import InterestsInput from "./InterestsInput";
import ItineraryCard from "./ItineraryCard";


import { getItinerary } from "../services/api";


const MapView = lazy(() => import("./MapView"));

export default function TourPlanner() {
  const [city, setCity] = useState("");
  const [days, setDays] = useState(3);
  const [interests, setInterests] = useState([]);
  const [itinerary, setItinerary] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedPlace, setSelectedPlace] = useState(null);

  const handlePlanTrip = async () => {
    if (!city.trim() || days < 1) {
      setError("Please enter a city and valid number of days.");
      return;
    }

    setError("");
    setLoading(true);
    setItinerary({});
    setSelectedPlace(null);

    try {
      const data = await getItinerary({ city: city.trim(), days, interests });

      const rawItinerary =
        data?.itinerary ||
        data?.itineraryMap ||
        data?.data?.itinerary ||
        data?.data?.itineraryMap ||
        data;

      setItinerary(rawItinerary || {});
    } catch (err) {
      console.error("API failed:", err);
      setError("Oops! Our AI is taking a coffee break ☕ Try again in a few seconds.");
    } finally {
      setLoading(false);
    }
  };

  const handlePlaceClick = (place) => {
    if (place?.lat && place?.lng) {
      setSelectedPlace({
        lat: parseFloat(place.lat),
        lng: parseFloat(place.lng),
        name: place.name,
      });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 py-12 px-4">
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="text-center mb-10">
        <h1 className="text-5xl md:text-6xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          Plan Your Dream Trip ✈️
        </h1>
        <p className="text-xl text-gray-600 mt-4">AI-powered itineraries in seconds</p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-2xl mx-auto bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl p-8 border border-white/50"
      >
        <div className="space-y-6">
          <CityInput value={city} onChange={setCity} disabled={loading} />
          <DaysInput value={days} onChange={setDays} disabled={loading} />
          <InterestsInput value={interests} onChange={setInterests} disabled={loading} />

          <button
            onClick={handlePlanTrip}
            disabled={loading}
            className={`w-full py-5 rounded-2xl font-bold text-xl transition-all transform ${
              loading
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover020:to-purple-700 hover:scale-105 shadow-xl hover:shadow-purple-500/50"
            } text-white`}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-3">
                <span className="animate-spin text-2xl">⚡</span>
                Creating Magic...
              </span>
            ) : (
              "🚀 Generate My Itinerary"
            )}
          </button>

          {error && (
            <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-red-500 text-center font-medium bg-red-50 py-3 rounded-xl">
              {error}
            </motion.p>
          )}
        </div>
      </motion.div>

      <div className="max-w-7xl mx-auto mt-12">
        {loading ? (
          <div className="grid lg:grid-cols-2 gap-8">
            <div className="space-y-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-48 bg-white/70 rounded-3xl animate-pulse shadow-xl" />
              ))}
            </div>
            <div className="h-[500px] bg-white/70 rounded-3xl animate-pulse shadow-2xl" />
          </div>
        ) : Object.keys(itinerary).length > 0 ? (
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} className="grid lg:grid-cols-2 gap-8">
            <div className="space-y-6 max-h-screen overflow-y-auto pr-4">
              {Object.entries(itinerary).map(([day, details]) => (
                <ItineraryCard key={day} day={day} details={details} onPlaceClick={handlePlaceClick} />
              ))}
            </div>

            <div className="sticky top-6">
              <Suspense fallback={<div className="h-[500px] bg-white rounded-3xl shadow-2xl animate-pulse flex items-center justify-center"><span className="text-6xl">🗺️</span></div>}>
                <MapView itinerary={itinerary} selectedPlace={selectedPlace} />
              </Suspense>
            </div>
          </motion.div>
        ) : (
          <div className="text-center py-20">
            <p className="text-2xl text-gray-500">
              Fill the form above and click <span className="font-bold text-indigo-600">"Generate My Itinerary"</span> to begin your journey ✨
            </p>
          </div>
        )}
      </div>
    </div>
  );
}