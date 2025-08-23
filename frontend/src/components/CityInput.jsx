import React from "react";

function CityInput({ value, onChange }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700">City</label>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Enter city name (e.g., Jaipur)"
        className="w-full mt-1 p-2 border rounded-lg"
      />
    </div>
  );
}

export default CityInput;
