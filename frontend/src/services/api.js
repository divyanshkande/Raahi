import apiClient from "./apiClient";

export const getItinerary = async ({ city, days, interests }) => {
  const response = await apiClient.post("/api/tour/plan", {
    city,
    days,
    interests,
  });
  return response.data;
};

export const modifyItinerary = async ({ city, days, interests, instruction, currentItinerary }) => {
  const response = await apiClient.post("/api/tour/modify", {
    city,
    days,
    interests,
    instruction,
    currentItinerary,
  });
  return response.data;
};

export const askAssistant = async ({ city, interests, question, currentItinerary }) => {
  const response = await apiClient.post("/api/tour/assistant", {
    city,
    interests,
    question,
    currentItinerary,
  });
  return response.data;
};

export const getHistory = async (page = 0, size = 10) => {
  const response = await apiClient.get("/api/history", {
    params: { page, size },
  });
  return response.data;
};

export const getHistoryDetail = async (id) => {
  const response = await apiClient.get(`/api/history/${id}`);
  return response.data;
};

export const deleteHistoryEntry = async (id) => {
  await apiClient.delete(`/api/history/${id}`);
};