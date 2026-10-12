import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { askAssistant } from "../services/api";

export default function AssistantPanel({ city, interests, itinerary }) {
  const [open, setOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState([]); // {role: 'user'|'assistant', text}
  const [loading, setLoading] = useState(false);

  const handleAsk = async (e) => {
    e.preventDefault();
    const q = question.trim();
    if (!q) return;
    if (loading) return; // Prevent duplicate submissions

    setMessages((prev) => [...prev, { role: "user", text: q }]);
    setQuestion("");
    setLoading(true);

    try {
      const data = await askAssistant({
        city,
        interests,
        question: q,
        currentItinerary: itinerary,
      });
      setMessages((prev) => [...prev, { role: "assistant", text: data?.answer || "..." }]);
    } catch (err) {
      console.error("Assistant call failed:", err);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", text: "Sorry, I couldn't answer that right now." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white/90 backdrop-blur-xl rounded-2xl shadow-xl border border-white/50 mb-6 overflow-hidden">
      <button
        onClick={() => setOpen(!open)}
        className="w-full px-5 py-4 flex items-center justify-between text-left"
        disabled={loading}
      >
        <span className="text-sm font-semibold text-gray-600">💬 Ask the travel assistant</span>
        <span className="text-xl">{open ? "−" : "+"}</span>
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ height: 0 }}
            animate={{ height: "auto" }}
            exit={{ height: 0 }}
            className="border-t border-gray-100"
          >
            <div className="p-5">
              {messages.length > 0 && (
                <div className="space-y-3 mb-4 max-h-64 overflow-y-auto">
                  {messages.map((m, i) => (
                    <div
                      key={i}
                      className={`text-sm rounded-xl px-4 py-2 max-w-[90%] ${
                        m.role === "user"
                          ? "bg-indigo-100 text-indigo-800 ml-auto"
                          : "bg-purple-50 text-gray-700"
                      }`}
                    >
                      {m.text}
                    </div>
                  ))}
                  {loading && (
                    <div className="text-sm text-gray-400 italic">Thinking...</div>
                  )}
                </div>
              )}

              <form onSubmit={handleAsk} className="flex gap-3">
                <input
                  type="text"
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  disabled={loading}
                  placeholder={`e.g. "What should I eat near today's places?"`}
                  className="flex-1 px-4 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-purple-400 disabled:bg-gray-100 text-sm"
                  maxLength={500}
                />
                <button
                  type="submit"
                  disabled={loading || !question.trim()}
                  className={`px-5 py-2 rounded-xl font-semibold text-white text-sm transition-all ${
                    loading || !question.trim()
                      ? "bg-gray-400 cursor-not-allowed"
                      : "bg-gradient-to-r from-purple-600 to-indigo-600 hover:scale-105 shadow-lg"
                  }`}
                >
                  Ask
                </button>
              </form>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}