/// src/pages/Home.jsx
import React from "react";
import { motion } from "framer-motion";
import Auth from "./Auth";

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">

      {/* Hero Section */}
      <section
        id="home"
        className="relative h-screen flex flex-col items-center justify-center overflow-hidden"
        style={{ paddingTop: "160px" }}
      >

        {/* Darker Gradient Overlay */}
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-600/95 to-purple-700/95" />

        {/* Background Image */}
        <div
          className="absolute inset-0 bg-cover bg-center opacity-30"
          style={{
            backgroundImage:
              "url('https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80')",
          }}
        />

        {/* Content Wrapper */}
        <div className="relative z-10 text-center px-6 max-w-5xl mx-auto mt-20 md:mt-32">

          {/* Main Title */}
          <motion.h1
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-5xl md:text-7xl font-extrabold text-white mb-6 leading-tight"
          >
            Raahi
          </motion.h1>

          {/* Tagline */}
          <motion.h2
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.1 }}
            className="text-2xl md:text-4xl text-indigo-200 font-semibold mt-4 mb-8"
          >
            Plan • Travel • Explore
          </motion.h2>

          {/* Subtitle */}
          <motion.p
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="text-xl md:text-2xl text-indigo-100 font-light max-w-3xl mx-auto mb-12"
          >
            AI-powered itineraries in seconds
          </motion.p>

          {/* CTA Button */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.4 }}
            className="flex justify-center"
          >
            <a
              href="#auth"
              className="px-12 py-5 bg-white text-indigo-600 font-bold text-xl 
                         rounded-xl hover:bg-indigo-50 transition shadow-xl hover:shadow-2xl"
            >
              Sign In or Register to Start
            </a>
          </motion.div>
        </div>

        {/* Scroll indicator */}
        <motion.div
          animate={{ y: [0, 10, 0] }}
          transition={{ duration: 2, repeat: Infinity }}
          className="absolute bottom-10 left-1/2 -translate-x-1/2 text-white/70"
        >
          <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 14l-7 7m0 0l-7-7m7 7V3"
            />
          </svg>
        </motion.div>
      </section>

      {/* About Section */}
      <section id="about" className="py-24 px-6">
        <div className="max-w-6xl mx-auto text-center">
          <motion.h2
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="text-4xl md:text-5xl font-bold text-gray-900 mb-8"
          >
            Plan Smarter, Travel Better
          </motion.h2>

          <motion.p
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ delay: 0.2 }}
            className="text-xl text-gray-600 max-w-3xl mx-auto mb-16 leading-relaxed"
          >
            Raahi leverages AI to understand your request and create a customized travel plan instantly.
          </motion.p>

          <div className="grid md:grid-cols-3 gap-10">
            {[
              { title: "Personalized Itineraries", desc: "Tailored to your interests", icon: "🎯" },
              { title: "Smart Scheduling", desc: "Optimized daily plans", icon: "⏰" },
              { title: "Local Tips", desc: "Hidden gems & real suggestions", icon: "🗺️" },
            ].map((feature, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: i * 0.1 }}
                className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition border border-gray-100"
              >
                <div className="text-5xl mb-4">{feature.icon}</div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">{feature.title}</h3>
                <p className="text-gray-600 leading-relaxed">{feature.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Auth Section */}
      <section id="auth" className="py-24 px-6 bg-gradient-to-b from-transparent to-indigo-50">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-12"
          >
            <h2 className="text-4xl font-bold text-gray-900 mb-4">Join Raahi Today</h2>
            <p className="text-xl text-gray-600">Create an account or sign in to start planning</p>
          </motion.div>

          <div className="flex justify-center">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              className="w-full max-w-2xl"
            >
              <Auth />
            </motion.div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 text-center text-gray-500 border-t bg-white/50 backdrop-blur">
        <p className="text-sm">Built with React • Tailwind • Spring Boot</p>
        <p className="text-xs mt-2">© 2025 Raahi • Made with ❤️ for travelers</p>
      </footer>
    </div>
  );
}
