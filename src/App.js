import React from "react";
import TourPlanner from "./components/TourPlanner";

function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-indigo-100 p-6">
      <header className="mb-8 text-center">
        <h1 className="text-4xl font-extrabold text-indigo-700 drop-shadow-sm">
          AI Tour Planner
        </h1>
        <p className="text-gray-600 mt-2 text-lg">
          Plan your perfect trip with AI-powered recommendations ✨
        </p>
      </header>

      <main className="max-w-4xl mx-auto">
        <TourPlanner />
      </main>
    </div>
  );
}

export default App;

