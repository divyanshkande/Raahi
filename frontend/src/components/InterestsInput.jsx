import React from "react";

const INTEREST_OPTIONS = [
  "Food",
  "History",
  "Culture",
  "Heritage",
  "Adventure",
  "Shopping",
  "Nature",
];

const MAX_INTERESTS = 10;

function InterestsInput({ value, onChange, disabled }) {
  const toggleInterest = (interest) => {
    if (value.includes(interest)) {
      onChange(value.filter((i) => i !== interest));
    } else {
      if (value.length >= MAX_INTERESTS) return;
      onChange([...value, interest]);
    }
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        Interests <span className="text-gray-400">({value.length}/{MAX_INTERESTS})</span>
      </label>
      <div className="flex flex-wrap gap-2 mt-1">
        {INTEREST_OPTIONS.map((interest) => (
          <button
            key={interest}
            type="button"
            onClick={() => toggleInterest(interest)}
            disabled={disabled || (value.length >= MAX_INTERESTS && !value.includes(interest))}
            className={`px-3 py-1 rounded-full border transition ${
              value.includes(interest)
                ? "bg-indigo-600 text-white border-indigo-600"
                : value.length >= MAX_INTERESTS && !value.includes(interest)
                ? "bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed"
                : "bg-white text-gray-700 border-gray-300 hover:bg-indigo-50"
            }`}
          >
            {interest}
          </button>
        ))}
      </div>
      {value.length > 0 && value.length < MAX_INTERESTS && (
        <p className="text-xs text-gray-400 mt-1">Click to add/remove interests</p>
      )}
    </div>
  );
}

export default InterestsInput;