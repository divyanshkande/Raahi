import { useNavigate, useLocation } from "react-router-dom";

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleScrollOrNavigate = (id) => {
    if (location.pathname !== "/") {
      navigate("/"); // go to home first
      setTimeout(() => {
        document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
      }, 200);
    } else {
      document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <nav className="fixed top-0 left-0 w-full bg-white/60 backdrop-blur-md shadow-md z-50">
      <div className="max-w-6xl mx-auto px-6 py-4 flex justify-between items-center">
        <h1 className="text-2xl font-extrabold text-blue-700 cursor-pointer" onClick={() => handleScrollOrNavigate("home")}>
          Raahi ✈️
        </h1>
        <div className="space-x-6">
          <span
            onClick={() => handleScrollOrNavigate("home")}
            className="cursor-pointer text-lg font-medium hover:text-blue-600 transition"
          >
            Home
          </span>
          <span
            onClick={() => handleScrollOrNavigate("about")}
            className="cursor-pointer text-lg font-medium hover:text-blue-600 transition"
          >
            About
          </span>
          <span
            onClick={() => handleScrollOrNavigate("auth")}
            className="cursor-pointer text-lg font-medium hover:text-blue-600 transition"
          >
            Login
          </span>
        </div>
      </div>
    </nav>
  );
}
