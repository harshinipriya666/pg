import Navbar from "../components/Navbar";
import { Link } from "react-router-dom";

function StudentDashboard() {
  const user = JSON.parse(localStorage.getItem("user") || "null");

  return (
    <>
      <Navbar />

      <div className="container">
        <h1>Student Dashboard</h1>

        <div className="welcome-card">
          <h2>Welcome, {user?.name || "Student"}! 👋</h2>
          <p>
            Welcome to your PG accommodation portal. Browse available rooms, verify your fees, report maintenance issues, and ask questions to our AI Assistant.
          </p>
        </div>

        {/* AI Assistant Banner */}
        <div className="ai-welcome-banner">
          <div className="ai-banner-content">
            <span className="ai-badge-indicator">🤖 Grounded AI Assistant Active</span>
            <h3>Have questions about PG rooms, rent dues, or meal timings?</h3>
            <p>
              Click the <strong>AI Assistant</strong> button in the bottom right corner of your screen anytime to ask questions, check your pending fees, or get room recommendations!
            </p>
          </div>
        </div>

        <div className="dashboard-grid">
          <Link to="/rooms" className="card">
            <h3>🛏️ Rooms & AI Search</h3>
            <p>Explore available rooms with AI recommendations & natural-language search</p>
          </Link>

          <Link to="/fees" className="card">
            <h3>💵 Fees & Dues</h3>
            <p>Check your payment history and pending rent dues</p>
          </Link>

          <Link to="/complaints" className="card">
            <h3>🛠️ Complaints</h3>
            <p>Submit and track maintenance complaints with AI categorization</p>
          </Link>

          <Link to="/reviews" className="card">
            <h3>⭐ Reviews & Ratings</h3>
            <p>Share your feedback about food, accommodation, and facilities</p>
          </Link>

          <Link to="/meals" className="card">
            <h3>🍽️ Meal Schedule</h3>
            <p>View breakfast, lunch, and dinner notifications</p>
          </Link>
        </div>
      </div>
    </>
  );
}

export default StudentDashboard;