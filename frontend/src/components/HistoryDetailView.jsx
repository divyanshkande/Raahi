import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import ItineraryCard from "./ItineraryCard";
import { getHistoryDetail } from "../services/api";

function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
  } catch {
    return iso;
  }
}

export default function HistoryDetailView({ historyId, onBack }) {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");

    getHistoryDetail(historyId)
      .then((data) => {
        if (!cancelled) setDetail(data);
      })
      .catch((err) => {
        console.error("Failed to load history detail:", err);
        if (!cancelled) {
          // Check if it's a 404 error
          if (err.response?.status === 404) {
            setError("Trip not found. It may have been deleted.");
          } else {
            setError("Couldn't load this trip. Please try again.");
          }
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [historyId]);

  const itinerary = detail?.itinerary?.itinerary || {};

  return (
    <div className="max-w-4xl mx-auto">
      <button
        onClick={onBack}
        className="mb-6 inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-white/90 backdrop-blur-xl border border-white/50 shadow-md text-indigo-600 font-semibold hover:bg-white hover:shadow-lg transition"
      >
        <span aria-hidden>←</span> Back to History
      </button>

      {loading && (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-white/70 rounded-2xl animate-pulse shadow-lg" />
          ))}
        </div>
      )}

      {!loading && error && (
        <p className="text-red-500 text-center bg-red-50 py-3 rounded-xl">{error}</p>
      )}

      {!loading && !error && detail && (
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
          <div className="mb-6">
            <h2 className="text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              {detail.itinerary?.city}
            </h2>
            <p className="text-gray-500 mt-1">
              {detail.itinerary?.days} day{detail.itinerary?.days === 1 ? "" : "s"} · Saved {formatDate(detail.createdAt)}
            </p>
            {detail.itinerary?.interests?.length > 0 && (
              <p className="text-sm text-gray-400 mt-1">{detail.itinerary.interests.join(", ")}</p>
            )}
          </div>

          <div className="space-y-6">
            {Object.entries(itinerary).map(([day, details]) => (
              <ItineraryCard key={day} day={day} details={details} onPlaceClick={() => {}} />
            ))}
          </div>
        </motion.div>
      )}
    </div>
  );
}