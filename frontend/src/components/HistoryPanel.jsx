import React, { useEffect, useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { getHistory, deleteHistoryEntry } from "../services/api";

const TrashIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
  </svg>
);

const ChevronLeftIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
  </svg>
);

const ChevronRightIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
  </svg>
);

function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return iso;
  }
}

export default function HistoryPanel({ onSelect }) {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getHistory(page, size);
      // Expect new paginated format: { content, page, size, totalElements, totalPages, first, last }
      if (data && typeof data === "object" && "content" in data) {
        setEntries(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } else if (Array.isArray(data)) {
        // Backward compatibility with old array format
        setEntries(data);
        setTotalPages(0);
        setTotalElements(data.length);
      } else {
        setEntries([]);
        setTotalPages(0);
        setTotalElements(0);
      }
    } catch (err) {
      console.error("Failed to load history:", err);
      setError("Couldn't load your history right now.");
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    load();
  }, [load]);

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    setDeletingId(id);
    try {
      await deleteHistoryEntry(id);
      setEntries((prev) => prev.filter((entry) => entry.id !== id));
      setTotalElements((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error("Failed to delete history entry:", err);
      setError("Couldn't delete that entry. Please try again.");
    } finally {
      setDeletingId(null);
    }
  };

  const goToPage = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h2 className="text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-6">
        🕓 Your Trip History
      </h2>

      {loading && (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-24 bg-white/70 rounded-2xl animate-pulse shadow-lg" />
          ))}
        </div>
      )}

      {!loading && error && (
        <p className="text-red-500 text-center bg-red-50 py-3 rounded-xl">{error}</p>
      )}

      {!loading && !error && entries.length === 0 && totalElements === 0 && (
        <div className="text-center py-16 bg-white/70 rounded-3xl shadow-lg border border-white/50">
          <p className="text-xl text-gray-500">
            No trips planned yet. Generate an itinerary to see it show up here ✨
          </p>
        </div>
      )}

      {!loading && !error && entries.length > 0 && (
        <>
          <div className="space-y-4">
            <AnimatePresence>
              {entries.map((entry) => (
                <motion.div
                  key={entry.id}
                  layout
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  onClick={() => onSelect(entry.id)}
                  className="cursor-pointer bg-white/90 backdrop-blur-xl rounded-2xl shadow-lg hover:shadow-2xl border border-white/50 p-5 flex items-center justify-between gap-4 transition-shadow"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-3 flex-wrap">
                      <h3 className="text-xl font-bold text-gray-800">{entry.city}</h3>
                      <span className="text-sm px-3 py-1 rounded-full bg-indigo-100 text-indigo-700 font-medium">
                        {entry.days} day{entry.days === 1 ? "" : "s"}
                      </span>
                    </div>
                    {entry.interests?.length > 0 && (
                      <p className="text-sm text-gray-500 mt-1 truncate">
                        {entry.interests.join(", ")}
                      </p>
                    )}
                    <p className="text-xs text-gray-400 mt-1">{formatDate(entry.createdAt)}</p>
                  </div>

                  <button
                    onClick={(e) => handleDelete(e, entry.id)}
                    disabled={deletingId === entry.id}
                    className="flex-shrink-0 p-2.5 rounded-xl text-red-500 hover:bg-red-50 hover:text-red-600 transition disabled:opacity-40"
                    aria-label="Delete this trip from history"
                  >
                    <TrashIcon />
                  </button>
                </motion.div>
              ))}
            </AnimatePresence>
          </div>

          {totalPages > 1 && (
            <div className="mt-6 flex items-center justify-center gap-2">
              <button
                onClick={() => goToPage(page - 1)}
                disabled={page === 0}
                className="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
                aria-label="Previous page"
              >
                <ChevronLeftIcon />
              </button>
              <span className="px-4 text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => goToPage(page + 1)}
                disabled={page >= totalPages - 1}
                className="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
                aria-label="Next page"
              >
                <ChevronRightIcon />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}