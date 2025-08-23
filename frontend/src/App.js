import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import TourPlanner from "./components/TourPlanner";

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-indigo-100">
        {/* Navbar always visible */}
        <Navbar />

        {/* Page Content */}
        <Routes>
          {/* Home page with scrollable sections: Home, About, Auth */}
          <Route path="/" element={<Home />} />

          {/* Protected Tour Planner page */}
          <Route path="/plan" element={<TourPlanner />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
