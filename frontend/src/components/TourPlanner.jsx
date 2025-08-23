import React, { useState } from "react";
import { motion } from "framer-motion";
import CityInput from "./CityInput";
import DaysInput from "./DaysInput";
import InterestsInput from "./InterestsInput";
import ItineraryCard from "./ItineraryCard";
import { getItinerary } from "../services/api";

export default function TourPlanner() {
  const [city, setCity] = useState("");
  const [days, setDays] = useState(1);
  const [interests, setInterests] = useState([]);
  const [itinerary, setItinerary] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handlePlanTrip = async () => {
    if (!city || !days) {
      setError("Please provide both city and days.");
      return;
    }
    setError("");
    setLoading(true);

    try {
      const data = await getItinerary({ city, days, interests });
      setItinerary(data.itinerary || {});
    } catch (err) {
      console.error(err);
      setError("Failed to fetch itinerary. Please try again.");
    }

    setLoading(false);
  };

  return (
    <motion.div
      className="min-h-screen bg-gradient-to-br from-indigo-100 via-purple-50 to-pink-50 flex flex-col items-center py-12 px-4"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
    >
      <motion.h1
        className="text-4xl md:text-5xl font-extrabold text-indigo-700 mb-6 drop-shadow-lg"
        initial={{ y: -50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.8 }}
      >
        Plan Your Dream Trip ✈️
      </motion.h1>

      {/* Inputs Card */}
      <motion.div
        className="w-full max-w-3xl bg-white shadow-2xl rounded-3xl p-8 space-y-6"
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.8 }}
      >
        <CityInput value={city} onChange={setCity} />
        <DaysInput value={days} onChange={setDays} />
        <InterestsInput value={interests} onChange={setInterests} />

        <motion.button
          onClick={handlePlanTrip}
          disabled={loading}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          className="w-full bg-indigo-600 text-white py-3 rounded-xl font-semibold shadow-lg hover:bg-indigo-700 transition disabled:opacity-50"
        >
          {loading ? "Planning..." : "Plan My Trip"}
        </motion.button>

        {error && <p className="text-red-500 text-center">{error}</p>}
      </motion.div>

      {/* Itinerary Cards */}
      <div className="w-full max-w-3xl mt-10 space-y-4">
        {Object.keys(itinerary).length > 0 &&
          Object.entries(itinerary).map(([day, details], idx) => (
            <motion.div
              key={idx}
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.2 }}
            >
              <ItineraryCard day={day} details={details} />
            </motion.div>
          ))}
      </div>

      {/* Empty state */}
      {Object.keys(itinerary).length === 0 && !loading && (
        <motion.p
          className="text-gray-500 mt-6 text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          Enter your trip details and click <b>Plan My Trip</b> to see the itinerary!
        </motion.p>
      )}
    </motion.div>
  );
}
