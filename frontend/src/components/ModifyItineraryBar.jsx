import React, { useState } from "react";
import { motion } from "framer-motion";
import { modifyItinerary } from "../services/api";

export default function ModifyItineraryBar({ city, days, interests, itinerary, onUpdated }) {
  const [instruction, setInstruction] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!instruction.trim()) return;
    if (loading) return; // Prevent duplicate submissions

    setLoading(true);
    setError("");
    try {
      const data = await modifyItinerary({
        city,
        days,
        interests,
        instruction: instruction.trim(),
        currentItinerary: itinerary,
      });
      const updated = data?.itinerary || data;
      onUpdated?.(updated, data);
      setInstruction("");
    } catch (err) {
      console.error("Modify itinerary failed:", err);
      setError("Couldn't apply that change right now. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.form
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      onSubmit={handleSubmit}
      className="bg-white/90 backdrop-blur-xl rounded-2xl shadow-xl border border-white/50 p-5 mb-6"
    >
      <label className="text-sm font-semibold text-gray-600 mb-2 block">
        ✏️ Tweak this itinerary
      </label>
      <div className="flex gap-3">
        <input
          type="text"
          value={instruction}
          onChange={(e) => setInstruction(e.target.value)}
          disabled={loading}
          placeholder={'e.g. "Make Day 2 more food-focused" or "Remove museums"'}
          className="flex-1 px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400 disabled:bg-gray-100"
          maxLength={500}
        />
        <button
          type="submit"
          disabled={loading || !instruction.trim()}
          className={`px-6 py-3 rounded-xl font-semibold text-white transition-all ${
            loading || !instruction.trim()
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-gradient-to-r from-indigo-600 to-purple-600 hover:scale-105 shadow-lg"
          }`}
        >
          {loading ? "Updating..." : "Apply"}
        </button>
      </div>
      {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
    </motion.form>
  );
}