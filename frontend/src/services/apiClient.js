import axios from "axios";
import { getToken, clearSession } from "../utils/auth";

// Set REACT_APP_API_URL in .env (or your host's env vars) to your deployed
// backend URL, e.g. https://raahi-backend.onrender.com
export const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Attach the JWT to every outgoing request, if we have one.
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Standardized error response format from backend:
// {
//   "timestamp": "...",
//   "status": 400,
//   "error": "VALIDATION_ERROR",
//   "message": "...",
//   "path": "..."
// }

// If the token is missing/expired, the backend responds 401 - clear the
// stale session and send the user back to the login page.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const status = error.response.status;

      // Handle 401 - clear session and redirect to login
      if (status === 401) {
        clearSession();
        if (window.location.pathname !== "/auth") {
          window.location.href = "/auth";
        }
      }

      // Don't automatically redirect for 403 - let the UI handle it
      // Don't redirect for other statuses - let components handle them
    }
    return Promise.reject(error);
  }
);

// Helper to extract user-friendly error message from standardized error response
export function extractErrorMessage(error) {
  if (error.response?.data) {
    const data = error.response.data;
    // New standardized format: { timestamp, status, error, message, path }
    if (data.message) {
      return data.message;
    }
    // Fallback for old format
    if (data.error) {
      return data.error;
    }
  }
  if (error.message) {
    return error.message;
  }
  return "Something went wrong. Please try again.";
}

export default apiClient;