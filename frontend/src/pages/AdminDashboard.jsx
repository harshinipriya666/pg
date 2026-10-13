import React, { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import { Link } from "react-router-dom";
import { aiAPI } from "../services/api";

function AdminDashboard() {
  const [insights, setInsights] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadInsights = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await aiAPI.getAdminInsights();
      setInsights(res.data);
    } catch (err) {
      console.error("Failed to load admin insights:", err);
      setError("Unable to load AI analytics. Please check your backend connection.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInsights();
  }, []);

  const metrics = insights?.metrics;

  return (
    <>
      <Navbar />

      <div className="container">
        <div className="page-header-banner">
          <div>
            <h1>Admin Executive Dashboard</h1>
            <p>Real-time PG operational metrics, verified financial records, and AI-powered strategic insights.</p>
          </div>
          <button
            className="ai-refresh-btn"
            onClick={loadInsights}
            disabled={loading}
          >
            {loading ? "Refreshing..." : "🔄 Refresh AI Insights"}
          </button>
        </div>

        {/* =========================================================
            KPI SUMMARY METRIC CARDS
        ========================================================= */}
        {metrics && (
          <div className="kpi-grid">
            <div className="kpi-card kpi-blue">
              <div className="kpi-icon">👥</div>
              <div className="kpi-info">
                <h3>Total Tenants</h3>
                <span className="kpi-value">{metrics.totalStudents}</span>
                <span className="kpi-subtext">Active registered students</span>
              </div>
            </div>

            <div className="kpi-card kpi-green">
              <div className="kpi-icon">🛏️</div>
              <div className="kpi-info">
                <h3>Occupancy Rate</h3>
                <span className="kpi-value">{metrics.occupancyRate}%</span>
                <span className="kpi-subtext">
                  {metrics.availableRooms} of {metrics.totalRooms} rooms vacant
                </span>
              </div>
            </div>

            <div className="kpi-card kpi-purple">
              <div className="kpi-icon">💵</div>
              <div className="kpi-info">
                <h3>Rent Collected</h3>
                <span className="kpi-value">₹{metrics.totalRentCollected.toLocaleString()}</span>
                <span className="kpi-subtext">Verified paid fees</span>
              </div>
            </div>

            <div className="kpi-card kpi-amber">
              <div className="kpi-icon">⚠️</div>
              <div className="kpi-info">
                <h3>Outstanding Rent</h3>
                <span className="kpi-value">₹{metrics.outstandingRent.toLocaleString()}</span>
                <span className="kpi-subtext">Pending student dues</span>
              </div>
            </div>

            <div className="kpi-card kpi-red">
              <div className="kpi-icon">🛠️</div>
              <div className="kpi-info">
                <h3>Open Complaints</h3>
                <span className="kpi-value">{metrics.pendingComplaints}</span>
                <span className="kpi-subtext">
                  {metrics.resolvedComplaints} resolved tickets
                </span>
              </div>
            </div>

            <div className="kpi-card kpi-teal">
              <div className="kpi-icon">⭐</div>
              <div className="kpi-info">
                <h3>Resident Rating</h3>
                <span className="kpi-value">{metrics.averageRating} / 5.0</span>
                <span className="kpi-subtext">{metrics.totalReviews} tenant reviews</span>
              </div>
            </div>
          </div>
        )}

        {/* =========================================================
            AI STRATEGIC INSIGHTS & EXECUTIVE REPORT
        ========================================================= */}
        <div className="ai-insights-panel">
          <div className="ai-insights-header">
            <div className="ai-insights-title">
              <h2>✨ AI Operational Insights & Strategic Actions</h2>
              <span className="ai-badge-indicator">
                {insights?.aiPowered ? "✨ Powered by Google Gemini" : "🛡️ Grounded Data Analysis"}
              </span>
            </div>
          </div>

          {loading && (
            <div className="ai-loading-box">
              <div className="ai-typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <p>Analyzing MySQL occupancy records, payment transactions, and complaint categories...</p>
            </div>
          )}

          {error && (
            <div className="error-box">
              <p>{error}</p>
              <button onClick={loadInsights} className="retry-btn">
                Retry
              </button>
            </div>
          )}

          {insights && !loading && (
            <div className="insights-content-grid">
              {/* Executive Summary */}
              <div className="insight-card full-width-card">
                <h3>📋 Executive Operational Summary</h3>
                <p className="insight-lead-text">{insights.executiveSummary}</p>
              </div>

              {/* Key Strengths */}
              <div className="insight-card strength-card">
                <h3>✅ Operational Strengths</h3>
                <ul>
                  {insights.keyStrengths?.map((item, idx) => (
                    <li key={idx}>{item}</li>
                  ))}
                </ul>
              </div>

              {/* Critical Alerts */}
              <div className="insight-card alert-card">
                <h3>🚨 Critical Action Alerts</h3>
                <ul>
                  {insights.criticalAlerts?.map((item, idx) => (
                    <li key={idx}>{item}</li>
                  ))}
                </ul>
              </div>

              {/* Actionable Recommendations */}
              <div className="insight-card rec-card full-width-card">
                <h3>💡 Practical Business & Management Recommendations</h3>
                <div className="recs-grid">
                  {insights.actionableRecommendations?.map((rec, idx) => (
                    <div key={idx} className="rec-item-box">
                      <span className="rec-number">#{idx + 1}</span>
                      <p>{rec}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Visual Distribution Charts */}
          {metrics && !loading && (
            <div className="analytics-charts-grid">
              {/* Fee Collection Progress Bar */}
              <div className="chart-box">
                <h3>Financial Recovery Status</h3>
                <div className="progress-bar-container">
                  {(() => {
                    const totalFee = metrics.totalRentCollected + metrics.outstandingRent;
                    const paidPercent = totalFee > 0 ? Math.round((metrics.totalRentCollected / totalFee) * 100) : 100;
                    return (
                      <>
                        <div className="progress-labels">
                          <span>Collected: ₹{metrics.totalRentCollected.toLocaleString()} ({paidPercent}%)</span>
                          <span>Pending: ₹{metrics.outstandingRent.toLocaleString()}</span>
                        </div>
                        <div className="progress-track">
                          <div
                            className="progress-fill paid-fill"
                            style={{ width: `${paidPercent}%` }}
                          ></div>
                        </div>
                      </>
                    );
                  })()}
                </div>
              </div>

              {/* Complaints by Category Chart */}
              <div className="chart-box">
                <h3>Complaints by Category</h3>
                <div className="category-bars-list">
                  {metrics.complaintsByCategory && Object.entries(metrics.complaintsByCategory).length > 0 ? (
                    Object.entries(metrics.complaintsByCategory).map(([cat, count]) => {
                      const totalComp = metrics.totalComplaints || 1;
                      const pct = Math.round((count / totalComp) * 100);
                      return (
                        <div key={cat} className="cat-bar-item">
                          <div className="cat-bar-labels">
                            <span className="cat-name">{cat}</span>
                            <span className="cat-count">{count} ticket(s) ({pct}%)</span>
                          </div>
                          <div className="cat-bar-track">
                            <div
                              className="cat-bar-fill"
                              style={{ width: `${Math.max(8, pct)}%` }}
                            ></div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <p className="no-records-msg">No complaints registered in the system.</p>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* =========================================================
            EXISTING MANAGEMENT LINKS (PRESERVED)
        ========================================================= */}
        <h2 style={{ marginTop: "40px", marginBottom: "15px" }}>Management Modules</h2>
        <div className="dashboard-grid">
          <Link className="card" to="/students">
            <h2>Students</h2>
            <p>Manage registered students</p>
          </Link>

          <Link className="card" to="/rooms">
            <h2>Rooms</h2>
            <p>Manage PG rooms & AI recommendations</p>
          </Link>

          <Link className="card" to="/allocations">
            <h2>Allocations</h2>
            <p>Allocate rooms to students</p>
          </Link>

          <Link className="card" to="/fees">
            <h2>Fees</h2>
            <p>Manage student rent and payments</p>
          </Link>

          <Link className="card" to="/complaints">
            <h2>Complaints</h2>
            <p>AI categorized maintenance tickets</p>
          </Link>

          <Link className="card" to="/reviews">
            <h2>Reviews</h2>
            <p>View student ratings & feedback</p>
          </Link>

          <Link className="card" to="/meals">
            <h2>Meal Schedule</h2>
            <p>Manage breakfast, lunch and dinner</p>
          </Link>
        </div>
      </div>
    </>
  );
}

export default AdminDashboard;