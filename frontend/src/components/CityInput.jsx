import React from "react";

function CityInput({ value, onChange, disabled }) {
  const maxLength = 100;
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Enter city name (e.g., Jaipur)"
        maxLength={maxLength}
        disabled={disabled}
        className="w-full mt-1 p-2 border rounded-lg disabled:bg-gray-100"
      />
      {value.length > 0 && (
        <p className={`text-xs mt-1 ${value.length > maxLength ? "text-red-500" : "text-gray-400"}`}>
          {value.length}/{maxLength}
        </p>
      )}
    </div>
  );
}

export default CityInput;