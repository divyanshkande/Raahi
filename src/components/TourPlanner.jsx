import React, { useState } from "react";
import CityInput from "./CityInput";
import DaysInput from "./DaysInput";
import InterestsInput from "./InterestsInput";
import ItineraryCard from "./ItineraryCard";
import { getItinerary } from "../services/api";

function TourPlanner() {
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
      console.log("Backend Response:", data);
      setItinerary(data.itinerary || {});
    } catch (err) {
      console.error("Error fetching itinerary:", err);
      setError("Failed to fetch itinerary. Please try again.");
    }

    setLoading(false);
  };

  return (
    <div className="max-w-3xl mx-auto bg-white shadow-lg rounded-2xl p-6">
      <div className="space-y-4">
        {/* Inputs */}
        <CityInput value={city} onChange={setCity} />
        <DaysInput value={days} onChange={setDays} />
        <InterestsInput value={interests} onChange={setInterests} />

        {/* Action Button */}
        <button
          onClick={handlePlanTrip}
          disabled={loading}
          className="w-full bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
        >
          {loading ? "Planning..." : "Plan My Trip"}
        </button>

        {/* Error message */}
        {error && <p className="text-red-500 text-center">{error}</p>}
      </div>

      {/* Results */}
      <div className="mt-6">
        {Object.keys(itinerary).length > 0 ? (
          Object.entries(itinerary).map(([day, details], index) => (
            <ItineraryCard key={index} day={day} details={details} />
          ))
        ) : (
          !loading && (
            <p className="text-gray-500 text-center mt-4">
              Enter details and click <b>Plan My Trip</b>.
            </p>
          )
        )}
      </div>
    </div>
  );
}

export default TourPlanner;
