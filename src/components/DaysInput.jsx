import React from "react";

function DaysInput({ value, onChange }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700">
        Number of Days
      </label>
      <input
        type="number"
        min="1"
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full mt-1 p-2 border rounded-lg"
      />
    </div>
  );
}

export default DaysInput;
