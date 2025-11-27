import axios from "axios";

const API_BASE = "http://localhost:8080/api/tour";

export const getItinerary = async ({ city, days, interests }) => {
  try {
    console.log("📡 Sending request to:", `${API_BASE}/plan`);

    const response = await axios.post(`${API_BASE}/plan`, {
      city,
      days,
      interests
    });

    console.log("📥 API Response:", response.data);

    return response.data;
  } catch (error) {
    console.error("❌ API ERROR:", error);
    throw error;
  }
};
