import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Login() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("STUDENT");

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {

      const response = await api.post("/auth/login", {
        email: email,
        password: password
      });

      const user = response.data;

      // Check selected role
      if (user.role !== role) {
        alert("Selected role does not match this account.");
        return;
      }

      localStorage.setItem("user", JSON.stringify(user));

      if (role === "ADMIN") {
        navigate("/admin-dashboard");
      } else {
        navigate("/student-dashboard");
      }

    } catch (error) {

      console.error(error);

      alert("Invalid email or password");

    }
  };

  return (
    <div className="login-page">

      <div className="login-box">

        <h1>PG Management</h1>

        <h2>Login</h2>

        <form onSubmit={handleLogin}>

          {/* Email */}
          <input
            type="email"
            placeholder="Enter Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          {/* Password */}
          <input
            type="password"
            placeholder="Enter Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {/* Role Selection */}
          <div className="role-selection">

            <label>
              <input
                type="radio"
                name="role"
                value="STUDENT"
                checked={role === "STUDENT"}
                onChange={(e) => setRole(e.target.value)}
              />
              Student
            </label>

            <label>
              <input
                type="radio"
                name="role"
                value="ADMIN"
                checked={role === "ADMIN"}
                onChange={(e) => setRole(e.target.value)}
              />
              Admin
            </label>

          </div>

          <button type="submit">
            Login
          </button>

        </form>

        <p>
          Don't have an account?{" "}
          <span
            onClick={() => navigate("/register")}
            className="register-link"
          >
            Register
          </span>
        </p>

      </div>

    </div>
  );
}

export default Login;