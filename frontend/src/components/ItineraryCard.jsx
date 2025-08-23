import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

function ItineraryCard({ day, details }) {
  const [open, setOpen] = useState(false);

  const renderPeriod = (label, period) => {
    if (!period) return null;

    const icon = label === "Morning" ? "🌅" : label === "Afternoon" ? "🌞" : "🌙";

    if (typeof period === "string") {
      return (
        <div className="mb-3">
          <h4 className="font-semibold text-indigo-600 flex items-center gap-2">
            {icon} {label}
          </h4>
          <p className="text-gray-700 ml-6">{period}</p>
        </div>
      );
    }

    return (
      <div className="mb-4">
        <h4 className="font-semibold text-indigo-600 flex items-center gap-2">
          {icon} {label}
        </h4>
        <p className="text-gray-800 font-medium ml-6">{period.activity}</p>
        <p className="text-gray-700 ml-6 mt-1">{period.description}</p>
      </div>
    );
  };

  return (
    <motion.div
      className="border border-indigo-200 rounded-2xl p-5 mb-4 shadow-lg bg-white hover:shadow-2xl transition-all duration-300"
      layout
    >
      {/* Header */}
      <button
        onClick={() => setOpen(!open)}
        className="w-full text-left flex justify-between items-center font-semibold text-indigo-700 text-lg"
      >
        <span>{day}</span>
        <motion.span
          animate={{ rotate: open ? 180 : 0 }}
          transition={{ duration: 0.3 }}
        >
          ▼
        </motion.span>
      </button>

      {/* Expandable Content */}
      <AnimatePresence>
        {open && (
          <motion.div
            className="mt-4 text-gray-700"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.4 }}
          >
            {renderPeriod("Morning", details.morning)}
            {renderPeriod("Afternoon", details.afternoon)}
            {renderPeriod("Evening", details.evening)}

            {/* Tips */}
            {details.foodTip && (
              <p className="text-sm text-green-600 mt-2 flex items-center gap-1">
                🍴 {details.foodTip}
              </p>
            )}
            {details.transportTip && (
              <p className="text-sm text-blue-600 flex items-center gap-1">
                🚌 {details.transportTip}
              </p>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

export default ItineraryCard;
