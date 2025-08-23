import { motion } from "framer-motion";
import Auth from "./Auth"; 

export default function Home() {
  return (
    <div className="pt-16"> 
      {/* Home Section */}
      <section
        id="home"
        className="h-screen flex flex-col justify-center items-center text-center 
        bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1507525428034-b723cf961d3e')",
        }}
      >
        <motion.h1
          initial={{ opacity: 0, y: -50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1 }}
          className="text-5xl md:text-6xl font-extrabold drop-shadow-xl mb-4 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-pink-500 to-yellow-400 font-[Poppins]"
        >
          Welcome to <span className="italic">Raahi ✈️</span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5, duration: 1 }}
          className="text-lg md:text-2xl font-medium text-gray-100 bg-black/40 px-4 py-2 rounded-xl shadow-lg font-[Poppins]"
        >
          Your <span className="text-yellow-300">AI Tour Planner</span>
        </motion.p>
      </section>

      {/* About Section */}
      <section
        id="about"
        className="min-h-screen flex flex-col justify-center items-center text-center 
        bg-cover bg-center bg-no-repeat px-6"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1526772662000-3f88f10405ff')",
        }}
      >
        <h2 className="text-4xl md:text-5xl font-bold text-yellow-300 drop-shadow-lg mb-6 animate-bounce">
          About This Project 🌍
        </h2>

        <p className="text-lg md:text-xl text-white/90 max-w-3xl mb-8 animate-fade-in">
          AI Tour Planner helps travelers create personalized itineraries by 
          combining <span className="font-bold">AI recommendations</span> with 
          <span className="font-bold"> real-time travel insights</span>. 
          Whether you're planning a single-day escape or a week-long journey, 
          this tool makes trip planning effortless and fun.
        </p>

        <div className="bg-black/50 p-6 rounded-2xl shadow-lg max-w-3xl text-left text-white/90 space-y-3">
          <h3 className="text-2xl font-semibold text-yellow-300">✨ Key Features</h3>
          <ul className="list-disc pl-5 space-y-2">
            <li>📌 Personalized itineraries based on your <b>interests</b> – food, culture, history, nature, adventure & shopping.</li>
            <li>⏰ Smart recommendations for <b>morning, afternoon, and evening</b> activities.</li>
            <li>📅 Works for <b>any number of days</b> – from short trips to long vacations.</li>
            <li>🍴 Includes <b>local food tips</b> and 🚌 <b>transport suggestions</b>.</li>
            <li>⚡ Simple, <b>easy-to-use UI</b> with collapsible day cards.</li>
            <li>🛠️ Built with <b>React, TailwindCSS</b>, and <b>Spring Boot</b> backend.</li>
          </ul>
        </div>
      </section>

      {/* Login Section */}
      <section
        id="auth"
        className="h-screen flex flex-col justify-center items-center bg-gray-100"
      >
        <Auth />
      </section>
    </div>
  );
}
