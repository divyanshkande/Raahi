// src/App.jsx
import React, { useEffect, useState, useCallback } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import TourPlanner from "./components/TourPlanner";
import "leaflet/dist/leaflet.css";

// REDUCED to 8 particles (was 18) + optimized
const FloatingParticles = () => {
  return (
    <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10">
      {[...Array(8)].map((_, i) => (
        <motion.div
          key={i}
          className="absolute w-2 h-2 bg-purple-400/30 rounded-full blur-md"
          initial={{ y: window.innerHeight + 100 }}
          animate={{ y: -100 }}
          transition={{
            duration: 30 + i * 5,
            repeat: Infinity,
            ease: "linear",
          }}
          style={{
            left: `${(i + 1) * 12}%`,
          }}
        />
      ))}
    </div>
  );
};

// Page transition only
const PageTransition = ({ children }) => {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={location.pathname}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        transition={{ duration: 0.4 }}
      >
        {children}
      </motion.div>
    </AnimatePresence>
  );
};

function App() {
  const [mousePos, setMousePos] = useState({ x: 0, y: 0 });
  const location = useLocation();

  // DEBOUNCED mouse move — this is the #1 fix
  useEffect(() => {
    let raf;
    const handleMouse = (e) => {
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        setMousePos({ x: e.clientX, y: e.clientY });
      });
    };
    window.addEventListener("mousemove", handleMouse);
    return () => {
      window.removeEventListener("mousemove", handleMouse);
      cancelAnimationFrame(raf);
    };
  }, []);

  return (
    <div className="min-h-screen relative overflow-hidden bg-gray-50">
      {/* Simplified beautiful background */}
      <div className="fixed inset-0 -z-20">
        <div className="absolute inset-0 bg-gradient-to-br from-purple-900/20 via-blue-900/20 to-indigo-900/20" />
      </div>

      {/* Much lighter mouse glow */}
      <motion.div
        className="pointer-events-none fixed inset-0 -z-10"
        animate={{
          background: `radial-gradient(800px at ${mousePos.x}px ${mousePos.y}px, rgba(168, 85, 247, 0.15), transparent 80%)`,
        }}
        transition={{ type: "spring", damping: 50, stiffness: 100 }}
      />

      <FloatingParticles />

      {/* Navbar */}
      <motion.div
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        className="sticky top-0 z-50 backdrop-blur-xl bg-white/80 border-b border-white/20 shadow-lg"
      >
        <Navbar />
      </motion.div>

      {/* Hero sparkles only on home */}
      {location.pathname === "/" && (
        <>
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 100, repeat: Infinity, ease: "linear" }}
            className="fixed top-32 left-10 text-purple-300/20 text-8xl pointer-events-none"
          >
            ✦
          </motion.div>
          <motion.div
            animate={{ rotate: -360 }}
            transition={{ duration: 140, repeat: Infinity, ease: "linear" }}
            className="fixed bottom-40 right-10 text-cyan-300/20 text-8xl pointer-events-none"
          >
            ✦
          </motion.div>
        </>
      )}

      <PageTransition>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/plan" element={<TourPlanner />} />
        </Routes>
      </PageTransition>

      {/* CTA Button - only on home */}
      {location.pathname === "/" && (
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ delay: 0.8, type: "spring" }}
          className="fixed bottom-8 right-8 z-50"
        >
          <div className="relative group">
            <div className="absolute -inset-2 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full blur-xl opacity-70 group-hover:opacity-100 transition duration-500" />
            <button className="relative p-6 bg-white/90 backdrop-blur-xl rounded-full shadow-2xl hover:scale-110 transition">
              <span className="text-5xl">✈️</span>
            </button>
            <div className="absolute -top-12 right-0 bg-black/70 text-white text-sm px-4 py-2 rounded-xl opacity-0 group-hover:opacity-100 transition">
              Start Planning ✨
            </div>
          </div>
        </motion.div>
      )}
    </div>
  );
}

export default function WrappedApp() {
  return (
    <Router>
      <App />
    </Router>
  );
}