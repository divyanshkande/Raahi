import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function ItineraryCard({ day, details, onPlaceClick }) {
  const [open, setOpen] = useState(false);

  const getPeriodStyle = (period) => {
    switch (period) {
      case "morning":
        return { gradient: "from-yellow-400 to-orange-500", bg: "bg-yellow-50", border: "border-yellow-200", emoji: "🌅" };
      case "afternoon":
        return { gradient: "from-orange-400 to-pink-500", bg: "bg-orange-50", border: "border-orange-200", emoji: "☀️" };
      case "evening":
        return { gradient: "from-purple-500 to-indigo-700", bg: "bg-purple-50", border: "border-purple-300", emoji: "🌙" };
      default:
        return { gradient: "from-indigo-400 to-purple-600", bg: "bg-indigo-50", border: "border-indigo-200", emoji: "⭐" };
    }
  };

  const renderPlaces = () => {
    if (!details || (Array.isArray(details) && details.length === 0)) {
      return <p className="text-gray-400 italic">No places planned yet...</p>;
    }

    
    if (Array.isArray(details)) {
      return details.map((place, i) => (
        <PlaceCard key={i} place={place} onClick={onPlaceClick} style={getPeriodStyle("afternoon")} />
      ));
    }

    
    return ["morning", "afternoon", "evening"].map((period) => {
      const places = details[period] || [];
      if (places.length === 0) return null;

      const style = getPeriodStyle(period);

      return (
        <div key={period} className="mb-10">
          <div className="flex items-center gap-4 mb-5">
            <div className={`text-5xl`}>{style.emoji}</div>
            <h3
  className={`text-2xl font-bold capitalize bg-gradient-to-r ${style.gradient} bg-clip-text text-transparent`}
>

              {period}
            </h3>
          </div>

          <div className="space-y-5 ml-4">
            {places.map((place, i) => (
              <PlaceCard key={i} place={place} onClick={onPlaceClick} style={style} />
            ))}
          </div>
        </div>
      );
    });
  };

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 40 }}
      animate={{ opacity: 1, y: 0 }}
      className="relative mb-8 overflow-hidden rounded-3xl"
    >
      {/* Outer Glow */}
      <div className="absolute -inset-1 bg-gradient-to-r from-purple-600 via-pink-600 to-indigo-600 rounded-3xl blur-xl opacity-60 group-hover:opacity-90 transition duration-1000" />

      <motion.div
        whileHover={{ scale: 1.02 }}
        className="relative bg-white/90 backdrop-blur-2xl border border-white/50 rounded-3xl shadow-2xl overflow-hidden"
      >
        {/* Header */}
        <button
          onClick={() => setOpen(!open)}
          className="w-full px-8 py-7 flex items-center justify-between text-left group"
        >
          <div className="flex items-center gap-5">
            <div className="relative">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-2xl font-bold shadow-xl">
                {day.split(" ")[1]}
              </div>
              <span className="absolute -top-2 -right-2 text-2xl animate-pulse">✨</span>
            </div>
            <div>
              <h3 className="text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-700 bg-clip-text text-transparent">
                {day}
              </h3>
              <p className="text-sm text-gray-500 mt-1">Tap to reveal the magic</p>
            </div>
          </div>

          <motion.div
            animate={{ rotate: open ? 180 : 0 }}
            className="p-4 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl text-white shadow-lg"
          >
            <span className="text-2xl">{open ? "−" : "+"}</span>
          </motion.div>
        </button>

        {/* Expandable Body */}
        <AnimatePresence>
          {open && (
            <motion.div
              initial={{ height: 0 }}
              animate={{ height: "auto" }}
              exit={{ height: 0 }}
              transition={{ duration: 0.5, ease: "easeInOut" }}
              className="px-8 pb-8 border-t border-gray-100/50"
            >
              <div className="relative">
                {/* Timeline Line */}
                <div className="absolute left-12 top-0 bottom-0 w-1 bg-gradient-to-b from-yellow-400 via-pink-400 to-purple-600 opacity-30 rounded-full" />
                {renderPlaces()}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </motion.div>
  );
}


function PlaceCard({ place, onClick, style }) {
  return (
    <motion.div
      whileHover={{ x: 12, scale: 1.03 }}
      onClick={() => onClick?.(place)}
      className={`group relative bg-white/80 backdrop-blur-xl rounded-2xl p-6 shadow-lg hover:shadow-2xl border ${style.border} cursor-pointer transition-all duration-400`}
    >
      <div className="flex gap-5">
        <div className="text-4xl flex-shrink-0">{style.emoji}</div>
        <div className="flex-1">
          <h4 className="text-xl font-bold text-gray-800 group-hover:text-indigo-600 transition">
            📍 {place.name || "Hidden Gem"}
          </h4>
          <p className="text-gray-600 mt-2 leading-relaxed">
            {place.description || "An unforgettable experience awaits!"}
          </p>
          {place.duration && (
            <div className="mt-3 inline-flex items-center gap-2 text-sm text-gray-500">
              <span>⏱</span> {place.duration}
            </div>
          )}
        </div>
      </div>

      {/* Shine Effect on Hover */}
      <div className="absolute inset-0 rounded-2xl overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-40 transition-opacity duration-1000 translate-x-[-100%] group-hover:translate-x-[100%]" 
          style={{ transform: "skewX(-20deg)" }}
        />
      </div>
    </motion.div>
  );
}