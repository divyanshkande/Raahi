import axios from "axios";

const API_BASE = "http://localhost:8080/api/tour";

export const getItinerary = async ({ city, days, interests }) => {
  const response = await axios.post(`${API_BASE}/plan`, {
    city,
    days,
    interests,
  });
  return response.data;
};
