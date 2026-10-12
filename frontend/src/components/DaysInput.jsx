import React from "react";

function DaysInput({ value, onChange, disabled }) {
  const min = 1;
  const max = 30;
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        Number of Days
      </label>
      <input
        type="number"
        min={min}
        max={max}
        value={value}
        onChange={(e) => {
          const num = Number(e.target.value);
          if (!isNaN(num)) onChange(num);
        }}
        disabled={disabled}
        className="w-full mt-1 p-2 border rounded-lg disabled:bg-gray-100"
      />
      {value !== "" && (
        <p className={`text-xs mt-1 ${value < min || value > max ? "text-red-500" : "text-gray-400"}`}>
          {value < min ? "Minimum 1 day" : value > max ? "Maximum 30 days" : `Range: ${min}-${max} days`}
        </p>
      )}
    </div>
  );
}

export default DaysInput;