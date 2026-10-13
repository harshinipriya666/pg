import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api, { aiAPI } from "../services/api";

function Rooms() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [activeTab, setActiveTab] = useState("search"); // 'search' | 'recommend' | 'all'

  // Admin form state
  const [roomNumber, setRoomNumber] = useState("");
  const [floor, setFloor] = useState("");
  const [capacity, setCapacity] = useState("");
  const [rent, setRent] = useState("");
  const [facilities, setFacilities] = useState("");

  // Natural Language Search State
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState(null);
  const [searchLoading, setSearchLoading] = useState(false);

  // AI Recommendation State
  const [recBudget, setRecBudget] = useState(7000);
  const [recSharing, setRecSharing] = useState("Any");
  const [recFloor, setRecFloor] = useState("Any");
  const [selectedFacilities, setSelectedFacilities] = useState(["Wi-Fi"]);
  const [recommendations, setRecommendations] = useState(null);
  const [recLoading, setRecLoading] = useState(false);

  // Get logged-in user
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const isAdmin = user?.role === "ADMIN";

  // Load all rooms from MySQL
  const loadRooms = async () => {
    try {
      const response = await api.get("/rooms");
      setRooms(response.data);
    } catch (error) {
      console.error("Error loading rooms:", error);
      alert("Failed to load rooms");
    }
  };

  useEffect(() => {
    loadRooms();
  }, []);

  // Natural language room search handler
  const handleNLSearch = async (e, sampleText) => {
    if (e) e.preventDefault();
    const query = sampleText || searchQuery;
    if (!query.trim()) return;

    setSearchLoading(true);
    try {
      const res = await aiAPI.searchRooms(query);
      setSearchResults(res.data);
    } catch (err) {
      console.error("Room search error:", err);
      alert("Search request failed. Please try again.");
    } finally {
      setSearchLoading(false);
    }
  };

  // AI Recommendation handler
  const handleGetRecommendations = async (e) => {
    e.preventDefault();
    setRecLoading(true);
    try {
      const payload = {
        maxBudget: Number(recBudget),
        sharingPreference: recSharing,
        floorPreference: recFloor === "Any" ? null : Number(recFloor),
        requiredFacilities: selectedFacilities,
      };
      const res = await aiAPI.getRoomRecommendations(payload);
      setRecommendations(res.data);
    } catch (err) {
      console.error("Recommendation error:", err);
      alert("Failed to generate recommendations. Please try again.");
    } finally {
      setRecLoading(false);
    }
  };

  const toggleFacility = (fac) => {
    if (selectedFacilities.includes(fac)) {
      setSelectedFacilities(selectedFacilities.filter((f) => f !== fac));
    } else {
      setSelectedFacilities([...selectedFacilities, fac]);
    }
  };

  // Add room - ADMIN ONLY
  const addRoom = async (e) => {
    e.preventDefault();
    if (!isAdmin) {
      alert("Only admin can add rooms");
      return;
    }

    try {
      await api.post("/rooms", {
        roomNumber: roomNumber,
        floor: Number(floor),
        capacity: Number(capacity),
        rent: rent ? Number(rent) : null,
        facilities: facilities || null,
      });

      alert("Room added successfully");
      setRoomNumber("");
      setFloor("");
      setCapacity("");
      setRent("");
      setFacilities("");
      loadRooms();
    } catch (error) {
      console.error(error);
      alert("Failed to add room");
    }
  };

  // Delete room - ADMIN ONLY
  const deleteRoom = async (id) => {
    if (!isAdmin) {
      alert("Only admin can delete rooms");
      return;
    }

    if (!window.confirm("Are you sure you want to delete this room?")) {
      return;
    }

    try {
      await api.delete(`/rooms/${id}`);
      alert("Room deleted successfully");
      loadRooms();
    } catch (error) {
      console.error(error);
      alert("Failed to delete room");
    }
  };

  return (
    <>
      <Navbar />

      <div className="container">
        {/* Header Banner */}
        <div className="page-header-banner">
          <div>
            <h1>PG Rooms & Smart Allocation</h1>
            <p>
              Discover available rooms with AI natural-language search and smart personalized recommendations.
            </p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="ai-tabs-container">
          <button
            className={`ai-tab-btn ${activeTab === "search" ? "active" : ""}`}
            onClick={() => setActiveTab("search")}
          >
            🔍 Natural-Language Search
          </button>
          <button
            className={`ai-tab-btn ${activeTab === "recommend" ? "active" : ""}`}
            onClick={() => setActiveTab("recommend")}
          >
            ✨ AI Room Recommendations
          </button>
          <button
            className={`ai-tab-btn ${activeTab === "all" ? "active" : ""}`}
            onClick={() => setActiveTab("all")}
          >
            📋 All Rooms Catalog ({rooms.length})
          </button>
        </div>

        {/* =========================================================
            TAB 1: NATURAL LANGUAGE ROOM SEARCH
        ========================================================= */}
        {activeTab === "search" && (
          <div className="ai-feature-card">
            <div className="ai-section-title">
              <h2>Ask in Plain English</h2>
              <p>Type your ideal room preference and our AI will retrieve verified matching rooms from MySQL.</p>
            </div>

            <form onSubmit={handleNLSearch} className="nl-search-bar-form">
              <input
                type="text"
                placeholder="e.g. Find double sharing room under ₹7,000 with Wi-Fi on 1st floor..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button type="submit" disabled={searchLoading || !searchQuery.trim()} className="ai-primary-btn">
                {searchLoading ? "Searching..." : "🔍 Search Rooms"}
              </button>
            </form>

            {/* Quick Prompt Pills */}
            <div className="sample-query-chips">
              <span>Try asking:</span>
              {[
                "Double sharing under ₹6,000",
                "Available room with AC and Wi-Fi",
                "Triple sharing on 3rd floor",
                "Budget friendly room under ₹5,000",
              ].map((queryText, idx) => (
                <button
                  key={idx}
                  type="button"
                  className="query-pill"
                  onClick={() => {
                    setSearchQuery(queryText);
                    handleNLSearch(null, queryText);
                  }}
                >
                  "{queryText}"
                </button>
              ))}
            </div>

            {/* Search Results */}
            {searchResults && (
              <div className="search-results-section">
                <div className="search-summary-alert">
                  <strong>{searchResults.searchExplanation}</strong>
                  {searchResults.extractedCriteria && (
                    <div className="extracted-criteria-chips">
                      {searchResults.extractedCriteria.maxBudget && (
                        <span className="criteria-tag">
                          💰 Max Budget: ₹{searchResults.extractedCriteria.maxBudget}
                        </span>
                      )}
                      {searchResults.extractedCriteria.capacity && (
                        <span className="criteria-tag">
                          👥 Capacity: {searchResults.extractedCriteria.capacity} Sharing
                        </span>
                      )}
                      {searchResults.extractedCriteria.floor && (
                        <span className="criteria-tag">
                          🏢 Floor: {searchResults.extractedCriteria.floor}
                        </span>
                      )}
                      {searchResults.extractedCriteria.facilities?.map((f, i) => (
                        <span key={i} className="criteria-tag">
                          ✓ {f}
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                <div className="room-cards-grid">
                  {searchResults.matchingRooms?.length === 0 ? (
                    <p className="no-records-msg">No available rooms matched these criteria.</p>
                  ) : (
                    searchResults.matchingRooms.map((room) => (
                      <div key={room.id} className="room-card">
                        <div className="room-card-header">
                          <h3>Room {room.roomNumber}</h3>
                          <span
                            className={
                              room.status === "AVAILABLE" ? "status-available" : "status-full"
                            }
                          >
                            {room.status}
                          </span>
                        </div>
                        <div className="room-card-body">
                          <div className="room-rent-highlight">
                            <span className="rent-amount">₹{room.rent || 4500}</span>
                            <span className="rent-period">/ month</span>
                          </div>
                          <p className="room-type-text">
                            <strong>Type:</strong> {room.roomType || `${room.capacity}-Sharing`} (Floor {room.floor})
                          </p>
                          <p className="room-capacity-text">
                            <strong>Capacity:</strong> {room.occupied} / {room.capacity} beds filled
                          </p>
                          <div className="room-facilities-list">
                            <strong>Amenities:</strong>
                            <p>{room.facilities || "High-speed Wi-Fi, Geyser, Cupboard, Study Desk"}</p>
                          </div>
                        </div>
                        <div className="room-card-footer">
                          <button
                            className="allocate-btn"
                            onClick={() => navigate("/allocations")}
                          >
                            Book / Allocate Room →
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* =========================================================
            TAB 2: AI ROOM RECOMMENDATIONS
        ========================================================= */}
        {activeTab === "recommend" && (
          <div className="ai-feature-card">
            <div className="ai-section-title">
              <h2>Smart Room Recommendation Engine</h2>
              <p>
                Set your budget, room sharing, and must-have facilities. Our algorithm ranks matching rooms and AI explains why each room fits your needs.
              </p>
            </div>

            <form onSubmit={handleGetRecommendations} className="recommendation-form">
              <div className="form-row-grid">
                {/* Budget */}
                <div className="form-group">
                  <label>
                    Maximum Monthly Budget: <strong>₹{recBudget}</strong>
                  </label>
                  <input
                    type="range"
                    min="3000"
                    max="12000"
                    step="500"
                    value={recBudget}
                    onChange={(e) => setRecBudget(e.target.value)}
                  />
                  <div className="range-labels">
                    <span>₹3,000</span>
                    <span>₹7,500</span>
                    <span>₹12,000</span>
                  </div>
                </div>

                {/* Sharing Preference */}
                <div className="form-group">
                  <label>Room Sharing Preference</label>
                  <select
                    value={recSharing}
                    onChange={(e) => setRecSharing(e.target.value)}
                  >
                    <option value="Any">Any Sharing Type</option>
                    <option value="Single">Single Sharing (Private)</option>
                    <option value="Double">Double Sharing (2 Beds)</option>
                    <option value="Triple">Triple Sharing (3 Beds)</option>
                    <option value="4-Sharing">4-Sharing (Budget)</option>
                  </select>
                </div>

                {/* Floor Preference */}
                <div className="form-group">
                  <label>Floor Preference</label>
                  <select
                    value={recFloor}
                    onChange={(e) => setRecFloor(e.target.value)}
                  >
                    <option value="Any">Any Floor</option>
                    <option value="1">1st Floor</option>
                    <option value="2">2nd Floor</option>
                    <option value="3">3rd Floor</option>
                  </select>
                </div>
              </div>

              {/* Facilities Checklist */}
              <div className="facilities-selection-section">
                <label>Preferred Amenities & Facilities:</label>
                <div className="facilities-chips-group">
                  {[
                    "Wi-Fi",
                    "AC",
                    "Attached Bathroom",
                    "Study Desk",
                    "Geyser",
                    "Balcony",
                    "Daily Housekeeping",
                  ].map((fac) => (
                    <button
                      key={fac}
                      type="button"
                      className={`facility-chip-btn ${
                        selectedFacilities.includes(fac) ? "selected" : ""
                      }`}
                      onClick={() => toggleFacility(fac)}
                    >
                      {selectedFacilities.includes(fac) ? "✓ " : "+ "}
                      {fac}
                    </button>
                  ))}
                </div>
              </div>

              <button
                type="submit"
                disabled={recLoading}
                className="ai-primary-btn rec-submit-btn"
              >
                {recLoading ? "Analyzing PG Rooms..." : "✨ Get AI Recommendations"}
              </button>
            </form>

            {/* Recommendations Display */}
            {recommendations && (
              <div className="rec-results-container">
                <div className="ai-summary-card">
                  <div className="ai-badge-row">
                    <span className="ai-highlight-badge">
                      {recommendations.aiPowered ? "✨ AI Generated Insight" : "🎯 Deterministic Ranking"}
                    </span>
                  </div>
                  <p className="ai-summary-text">{recommendations.summary}</p>
                </div>

                <div className="room-cards-grid">
                  {recommendations.recommendations?.length === 0 ? (
                    <p className="no-records-msg">No rooms match these specific filters right now.</p>
                  ) : (
                    recommendations.recommendations.map((item, idx) => (
                      <div key={item.room.id} className="room-card ranked-card">
                        <div className="rank-badge-ribbon">
                          #{idx + 1} • {item.matchScore}% Match
                        </div>
                        <div className="room-card-header">
                          <h3>Room {item.room.roomNumber}</h3>
                          <span className="status-available">
                            {item.room.status}
                          </span>
                        </div>
                        <div className="room-card-body">
                          <div className="room-rent-highlight">
                            <span className="rent-amount">₹{item.room.rent}</span>
                            <span className="rent-period">/ month</span>
                          </div>
                          <p className="room-type-text">
                            <strong>Type:</strong> {item.room.roomType || `${item.room.capacity}-Sharing`} (Floor {item.room.floor})
                          </p>
                          <div className="ai-room-reason-box">
                            <strong>💡 Why it matches:</strong>
                            <p>{item.reason}</p>
                          </div>
                          <div className="room-facilities-list">
                            <strong>Facilities:</strong>
                            <p>{item.room.facilities}</p>
                          </div>
                        </div>
                        <div className="room-card-footer">
                          <button
                            className="allocate-btn"
                            onClick={() => navigate("/allocations")}
                          >
                            Book Room {item.room.roomNumber} →
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* =========================================================
            TAB 3: COMPLETE ROOM CATALOG & ADMIN CONTROLS
        ========================================================= */}
        {activeTab === "all" && (
          <div>
            {/* ADMIN ONLY - ADD ROOM FORM */}
            {isAdmin && (
              <div className="form-card">
                <h2>Add New PG Room</h2>
                <form onSubmit={addRoom} className="admin-room-form">
                  <input
                    type="text"
                    placeholder="Room Number (e.g. 103)"
                    value={roomNumber}
                    onChange={(e) => setRoomNumber(e.target.value)}
                    required
                  />

                  <input
                    type="number"
                    placeholder="Floor (e.g. 1)"
                    value={floor}
                    onChange={(e) => setFloor(e.target.value)}
                    required
                  />

                  <input
                    type="number"
                    placeholder="Capacity (e.g. 2)"
                    value={capacity}
                    onChange={(e) => setCapacity(e.target.value)}
                    required
                  />

                  <input
                    type="number"
                    placeholder="Monthly Rent (₹, e.g. 6500)"
                    value={rent}
                    onChange={(e) => setRent(e.target.value)}
                  />

                  <input
                    type="text"
                    placeholder="Facilities (e.g. Wi-Fi, AC, Attached Bath)"
                    value={facilities}
                    onChange={(e) => setFacilities(e.target.value)}
                  />

                  <button type="submit" className="ai-primary-btn">
                    + Add Room
                  </button>
                </form>
              </div>
            )}

            {!isAdmin && (
              <div className="student-info">
                <p>
                  You can browse all rooms. Administrators can add, modify, or delete room inventory.
                </p>
              </div>
            )}

            {/* ROOMS TABLE */}
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Room</th>
                    <th>Floor</th>
                    <th>Sharing Type</th>
                    <th>Capacity</th>
                    <th>Occupied</th>
                    <th>Monthly Rent</th>
                    <th>Facilities</th>
                    <th>Status</th>
                    {isAdmin && <th>Action</th>}
                  </tr>
                </thead>
                <tbody>
                  {rooms.length === 0 ? (
                    <tr>
                      <td colSpan={isAdmin ? "10" : "9"}>No rooms available</td>
                    </tr>
                  ) : (
                    rooms.map((room) => (
                      <tr key={room.id}>
                        <td>{room.id}</td>
                        <td>
                          <strong>{room.roomNumber}</strong>
                        </td>
                        <td>{room.floor}</td>
                        <td>{room.roomType || `${room.capacity}-Sharing`}</td>
                        <td>{room.capacity}</td>
                        <td>{room.occupied}</td>
                        <td>
                          <strong>₹{room.rent || 4500}</strong>
                        </td>
                        <td style={{ fontSize: "12px", maxWidth: "250px" }}>
                          {room.facilities || "Wi-Fi, Geyser, Cupboard"}
                        </td>
                        <td>
                          <span
                            className={
                              room.status === "AVAILABLE"
                                ? "status-available"
                                : "status-full"
                            }
                          >
                            {room.status}
                          </span>
                        </td>
                        {isAdmin && (
                          <td>
                            <button
                              className="delete-btn"
                              onClick={() => deleteRoom(room.id)}
                            >
                              Delete
                            </button>
                          </td>
                        )}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default Rooms;