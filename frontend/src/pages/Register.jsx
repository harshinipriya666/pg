import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import API from "../services/api";

function Register() {

  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "STUDENT"
  });

  const [message, setMessage] = useState("");

  const handleChange = (e) => {

    setForm({
      ...form,
      [e.target.name]: e.target.value
    });

  };

  const register = async (e) => {

    e.preventDefault();

    try {

      const response = await API.post(
        "/auth/register",
        form
      );

      setMessage(response.data.message);

      setTimeout(() => {
        navigate("/login");
      }, 1000);

    } catch (error) {

      setMessage(
        error.response?.data?.message ||
        "Registration failed"
      );

    }
  };

  return (
    <div className="auth-container">

      <form className="auth-box" onSubmit={register}>

        <h1>PG Management</h1>

        <h2>Create Account</h2>

        <input
          type="text"
          name="name"
          placeholder="Full Name"
          value={form.name}
          onChange={handleChange}
          required
        />

        <input
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
        />

        <input
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          required
        />

        <select
          name="role"
          value={form.role}
          onChange={handleChange}
        >
          <option value="STUDENT">Student</option>
          <option value="ADMIN">Admin</option>
        </select>

        <button type="submit">
          Register
        </button>

        {message && <p>{message}</p>}

        <p>
          Already have an account?
          <Link to="/login"> Login</Link>
        </p>

      </form>

    </div>
  );
}

export default Register;