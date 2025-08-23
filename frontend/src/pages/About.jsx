export default function About() {
  return (
    <div
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
    </div>
  );
}
