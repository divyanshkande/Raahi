import React from "react";
import { Link, useLocation } from "react-router-dom";

export default function Navbar() {
  const location = useLocation();

  
  const isOnDashboard = location.pathname === "/plan" || location.pathname.startsWith("/plan");

  
  const isLoggedIn = localStorage.getItem("userEmail") !== null;

  if (isLoggedIn && isOnDashboard) {
    return null; 
  }

  
  if (location.pathname !== "/" && !isOnDashboard) {
    return null; 
  }

  return (
    <nav className="fixed top-0 left-0 w-full bg-white/90 backdrop-blur-lg shadow-sm border-b border-gray-100 z-50">
      <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
        {/* Logo */}
        <Link
          to="/"
          className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent hover:opacity-80 transition"
        >
          Raahi
        </Link>

        {/* Nav Links */}
        <div className="hidden md:flex items-center space-x-10">
          <a href="#home" className="text-gray-700 font-medium hover:text-indigo-600 transition">
            Home
          </a>
          <a href="#about" className="text-gray-700 font-medium hover:text-indigo-600 transition">
            About
          </a>
          <a
            href="#auth"
            className="px-6 py-2.5 bg-indigo-600 text-white font-medium rounded-full hover:bg-indigo-700 hover:shadow-lg transition"
          >
            Sign In
          </a>
        </div>

        <div className="md:hidden">
          <button className="text-gray-700 text-2xl">Menu</button>
        </div>
      </div>
    </nav>
  );
}