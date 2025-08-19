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

function InterestsInput({ value, onChange }) {
  const toggleInterest = (interest) => {
    if (value.includes(interest)) {
      onChange(value.filter((i) => i !== interest));
    } else {
      onChange([...value, interest]);
    }
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700">
        Interests
      </label>
      <div className="flex flex-wrap gap-2 mt-1">
        {INTEREST_OPTIONS.map((interest) => (
          <button
            key={interest}
            type="button"
            onClick={() => toggleInterest(interest)}
            className={`px-3 py-1 rounded-full border transition ${
              value.includes(interest)
                ? "bg-indigo-600 text-white border-indigo-600"
                : "bg-white text-gray-700 border-gray-300"
            }`}
          >
            {interest}
          </button>
        ))}
      </div>
    </div>
  );
}

export default InterestsInput;
