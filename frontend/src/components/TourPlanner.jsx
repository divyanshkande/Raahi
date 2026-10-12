import React, { useState, lazy, Suspense, useCallback } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import CityInput from "./CityInput";
import DaysInput from "./DaysInput";
import InterestsInput from "./InterestsInput";
import ItineraryCard from "./ItineraryCard";
import ModifyItineraryBar from "./ModifyItineraryBar";
import AssistantPanel from "./AssistantPanel";
import HistoryPanel from "./HistoryPanel";
import HistoryDetailView from "./HistoryDetailView";


import { getItinerary } from "../services/api";
import { clearSession, getUsername, getEmail } from "../utils/auth";
import { extractErrorMessage } from "../services/apiClient";


const MapView = lazy(() => import("./MapView"));

export default function TourPlanner() {
  const navigate = useNavigate();
  const displayName = getUsername() || getEmail();

  const handleLogout = () => {
    clearSession();
    navigate("/auth");
  };

  const [city, setCity] = useState("");
  const [days, setDays] = useState(3);
  const [interests, setInterests] = useState([]);
  const [itinerary, setItinerary] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedPlace, setSelectedPlace] = useState(null);
  const [view, setView] = useState("planner"); // 'planner' | 'history' | 'historyDetail'
  const [selectedHistoryId, setSelectedHistoryId] = useState(null);

  // Prevent multiple simultaneous submissions
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateForm = useCallback(() => {
    if (!city.trim()) {
      setError("Please enter a city.");
      return false;
    }
    if (city.trim().length > 100) {
      setError("City name is too long (max 100 characters).");
      return false;
    }
    if (days < 1 || days > 30) {
      setError("Number of days must be between 1 and 30.");
      return false;
    }
    if (interests.length > 10) {
      setError("Maximum 10 interests allowed.");
      return false;
    }
    return true;
  }, [city, days, interests]);

  const handlePlanTrip = async () => {
    if (!validateForm()) return;
    if (isSubmitting) return; // Prevent duplicate submissions

    setIsSubmitting(true);
    setError("");
    setLoading(true);
    setItinerary({});
    setSelectedPlace(null);

    try {
      const data = await getItinerary({ city: city.trim(), days, interests });

      // Backend returns consistent shape: { itinerary: {...} }
      setItinerary(data.itinerary || {});
    } catch (err) {
      console.error("API failed:", err);
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
      setIsSubmitting(false);
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
      <div className="max-w-7xl mx-auto flex justify-between items-center mb-8 bg-white/90 backdrop-blur-xl rounded-2xl shadow-lg border border-white/50 px-6 py-4">
        <span className="text-gray-600 text-sm font-medium">
          {displayName ? `Signed in as ${displayName}` : ""}
        </span>
        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              if (view === "planner") {
                setView("history");
              } else {
                setView("planner");
                setSelectedHistoryId(null);
              }
            }}
            disabled={loading}
            className="px-5 py-2.5 rounded-full font-semibold text-sm bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md hover:shadow-lg hover:scale-105 transition-all disabled:opacity-50"
          >
            {view === "planner" ? "🕓 History" : "✈️ New Trip"}
          </button>
          <button
            onClick={handleLogout}
            className="px-5 py-2.5 rounded-full font-semibold text-sm bg-gray-100 text-gray-700 hover:bg-gray-200 hover:shadow-md transition-all"
          >
            Sign Out
          </button>
        </div>
      </div>

      {view === "history" && (
        <HistoryPanel
          onSelect={(id) => {
            setSelectedHistoryId(id);
            setView("historyDetail");
          }}
        />
      )}

      {view === "historyDetail" && (
        <HistoryDetailView historyId={selectedHistoryId} onBack={() => setView("history")} />
      )}

      {view === "planner" && (
        <>
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
          <CityInput value={city} onChange={setCity} disabled={loading || isSubmitting} />
          <DaysInput value={days} onChange={setDays} disabled={loading || isSubmitting} />
          <InterestsInput value={interests} onChange={setInterests} disabled={loading || isSubmitting} />

          <button
            onClick={handlePlanTrip}
            disabled={loading || isSubmitting}
            className={`w-full py-5 rounded-2xl font-bold text-xl transition-all transform ${
              loading || isSubmitting
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 hover:scale-105 shadow-xl hover:shadow-purple-500/50"
            } text-white`}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-3">
                <span className="animate-spin text-2xl">⚡</span>
                Creating your itinerary...
              </span>
            ) : (
              "🚀 Generate My Itinerary"
            )}
          </button>

          {/* AI Disclosure Banner */}
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mt-4">
            <div className="flex items-start gap-3">
              <span className="text-amber-600 text-xl flex-shrink-0">⚠️</span>
              <div className="text-sm text-amber-800">
                <p className="font-semibold">AI-generated itinerary</p>
                <p className="mt-1">
                  Recommendations, locations and travel estimates may contain inaccuracies.
                  Please verify important details before traveling.
                </p>
              </div>
            </div>
          </div>

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
              <div className="bg-white/70 rounded-3xl p-6 animate-pulse shadow-xl">
                <div className="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div className="bg-white/70 rounded-3xl p-6 animate-pulse shadow-xl">
                <div className="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div className="bg-white/70 rounded-3xl p-6 animate-pulse shadow-xl">
                <div className="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
            <div className="h-[500px] bg-white/70 rounded-3xl animate-pulse shadow-2xl flex items-center justify-center">
              <div className="text-center text-gray-500">
                <div className="text-4xl mb-2">⚡</div>
                <p className="text-lg font-medium">Generating your itinerary...</p>
                <p className="text-sm text-gray-400 mt-1">This may take up to a minute</p>
              </div>
            </div>
          </div>
        ) : Object.keys(itinerary).length > 0 ? (
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} className="grid lg:grid-cols-2 gap-8">
            <div className="space-y-6 max-h-screen overflow-y-auto pr-4">
              <ModifyItineraryBar
                city={city}
                days={days}
                interests={interests}
                itinerary={itinerary}
                onUpdated={(updatedItinerary) => setItinerary(updatedItinerary || {})}
              />
              <AssistantPanel city={city} interests={interests} itinerary={itinerary} />
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
        </>
      )}
    </div>
  );
}