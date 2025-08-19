import React, { useState } from "react";

function ItineraryCard({ day, details }) {
  const [open, setOpen] = useState(false);

  const renderPeriod = (label, period) => {
    if (!period) return null;

    if (typeof period === "string") {
      return (
        <div className="mb-3">
          <h4 className="font-semibold text-indigo-600">{label}</h4>
          <p className="text-gray-700">{period}</p>
        </div>
      );
    }

    return (
      <div className="mb-4">
        <h4 className="font-semibold text-indigo-600">{label}</h4>
        <p className="text-gray-800 font-medium">{period.activity}</p>
        <p className="text-gray-700 mt-1">{period.description}</p>
      </div>
    );
  };

  return (
    <div className="border rounded-xl p-4 mb-3 shadow-sm">
      <button
        onClick={() => setOpen(!open)}
        className="w-full text-left flex justify-between items-center"
      >
        <span className="font-semibold text-indigo-700">{day}</span>
        <span>{open ? "▲" : "▼"}</span>
      </button>

      {open && (
        <div className="mt-2 text-gray-700">
          {renderPeriod("Morning", details.morning)}
          {renderPeriod("Afternoon", details.afternoon)}
          {renderPeriod("Evening", details.evening)}

          {/* Day-level tips */}
          {details.foodTip && (
            <p className="text-sm text-green-600 mt-2">🍴 {details.foodTip}</p>
          )}
          {details.transportTip && (
            <p className="text-sm text-blue-600">🚌 {details.transportTip}</p>
          )}
        </div>
      )}
    </div>
  );
}

export default ItineraryCard;
