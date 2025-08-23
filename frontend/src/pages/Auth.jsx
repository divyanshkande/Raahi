import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Auth = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLogin, setIsLogin] = useState(true);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const endpoint = isLogin ? "/auth/login" : "/auth/register";
      const res = await axios.post(
        `http://localhost:8080${endpoint}`,
        { email, password }
      );

      if (isLogin) {
        if (res.data.includes("Login successful")) {
          // ✅ Store user info in localStorage (acts like session)
          localStorage.setItem("userEmail", email);

          // ✅ Redirect to Tour Planner page
          navigate("/plan");
        } else {
          alert("Invalid email or password");
        }
      } else {
        alert("Registered successfully! Now you can log in.");
        setIsLogin(true);
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Something went wrong. Try again!");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <h1 className="text-2xl font-bold mb-4">
        {isLogin ? "Login" : "Register"}
      </h1>
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded-lg shadow-md w-80"
      >
        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 mb-3 border rounded"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 mb-3 border rounded"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button
          type="submit"
          className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600"
        >
          {isLogin ? "Login" : "Register"}
        </button>
      </form>

      <button
        onClick={() => setIsLogin(!isLogin)}
        className="mt-4 text-blue-600 hover:underline"
      >
        {isLogin
          ? "Don't have an account? Register"
          : "Already have an account? Login"}
      </button>
    </div>
  );
};

export default Auth;
