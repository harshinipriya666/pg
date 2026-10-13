import { Link, useNavigate } from "react-router-dom";

function Navbar() {

  const navigate = useNavigate();

  const user = JSON.parse(localStorage.getItem("user"));

  const logout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="navbar">

      <h2>PG Management</h2>

      <div className="nav-links">

        {user?.role === "ADMIN" ? (
          <>
            <Link to="/admin-dashboard">Dashboard</Link>
            <Link to="/students">Students</Link>
            <Link to="/rooms">Rooms</Link>
            <Link to="/allocations">Allocations</Link>
            <Link to="/fees">Fees</Link>
            <Link to="/complaints">Complaints</Link>
            <Link to="/reviews">Reviews</Link>
            <Link to="/meals">Meals</Link>
          </>
        ) : (
          <>
            <Link to="/student-dashboard">Dashboard</Link>
            <Link to="/rooms">Rooms</Link>
            <Link to="/fees">Fees</Link>
            <Link to="/complaints">Complaints</Link>
            <Link to="/reviews">Reviews</Link>
            <Link to="/meals">Meals</Link>
          </>
        )}

        <button onClick={logout}>Logout</button>

      </div>

    </nav>
  );
}

export default Navbar;