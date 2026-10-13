import React, { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import api, { aiAPI } from "../services/api";

function Complaints() {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const isAdmin = user?.role === "ADMIN";

  const [complaints, setComplaints] = useState([]);
  const [filterCategory, setFilterCategory] = useState("ALL");
  const [filterStatus, setFilterStatus] = useState("ALL");

  const [form, setForm] = useState({
    studentId: "",
    subject: "",
    description: "",
    category: "OTHER",
    priority: "MEDIUM",
    priorityReason: "",
    aiSummary: "",
  });

  // AI Assistance State
  const [aiLoading, setAiLoading] = useState(false);
  const [aiSuggestion, setAiSuggestion] = useState(null);

  // Load complaints
  const loadComplaints = async () => {
    try {
      if (isAdmin) {
        // ADMIN - see all complaints
        const response = await api.get("/complaints");
        setComplaints(response.data);
      } else {
        // STUDENT - find logged-in student
        const studentsResponse = await api.get("/students");
        const currentStudent = studentsResponse.data.find(
          (student) => student.email === user?.email
        );

        if (currentStudent) {
          setForm((prev) => ({ ...prev, studentId: currentStudent.id }));
          const response = await api.get(
            `/complaints/student/${currentStudent.id}`
          );
          setComplaints(response.data);
        } else {
          setComplaints([]);
        }
      }
    } catch (error) {
      console.error(error);
      alert("Failed to load complaints");
    }
  };

  useEffect(() => {
    loadComplaints();
  }, []);

  // AI Assist handler
  const handleAIAssist = async () => {
    if (!form.description.trim() && !form.subject.trim()) {
      alert("Please enter a subject or description first to get AI assistance.");
      return;
    }

    setAiLoading(true);
    try {
      const res = await aiAPI.assistComplaint({
        subject: form.subject,
        description: form.description,
        studentId: form.studentId ? Number(form.studentId) : null,
      });

      setAiSuggestion(res.data);
    } catch (err) {
      console.error("AI Complaint Assist error:", err);
      alert("AI complaint assistance temporarily unavailable.");
    } finally {
      setAiLoading(false);
    }
  };

  // Apply AI suggestions
  const applyAISuggestions = () => {
    if (!aiSuggestion) return;
    setForm((prev) => ({
      ...prev,
      subject: aiSuggestion.refinedSubject || prev.subject,
      category: aiSuggestion.suggestedCategory || prev.category,
      priority: aiSuggestion.suggestedPriority || prev.priority,
      priorityReason: aiSuggestion.priorityReason || "",
      aiSummary: aiSuggestion.summary || "",
    }));
    setAiSuggestion(null);
  };

  // STUDENT ONLY - Submit complaint
  const submitComplaint = async (e) => {
    e.preventDefault();

    if (isAdmin) {
      alert("Admin cannot submit complaints");
      return;
    }

    try {
      await api.post(`/complaints/student/${form.studentId}`, {
        subject: form.subject,
        description: form.description,
        category: form.category,
        priority: form.priority,
        priorityReason: form.priorityReason,
        aiSummary: form.aiSummary,
      });

      alert("Complaint submitted successfully!");

      setForm((prev) => ({
        ...prev,
        subject: "",
        description: "",
        category: "OTHER",
        priority: "MEDIUM",
        priorityReason: "",
        aiSummary: "",
      }));
      setAiSuggestion(null);

      loadComplaints();
    } catch (error) {
      console.error(error);
      alert("Failed to submit complaint");
    }
  };

  // ADMIN ONLY - Resolve complaint
  const resolveComplaint = async (id) => {
    if (!isAdmin) {
      alert("Only admin can resolve complaints");
      return;
    }

    try {
      await api.put(`/complaints/${id}/status?value=RESOLVED`);
      alert("Complaint resolved successfully");
      loadComplaints();
    } catch (error) {
      console.error(error);
      alert("Failed to resolve complaint");
    }
  };

  // ADMIN ONLY - Delete complaint
  const deleteComplaint = async (id) => {
    if (!isAdmin) {
      alert("Only admin can delete complaints");
      return;
    }

    if (!window.confirm("Delete this complaint?")) {
      return;
    }

    try {
      await api.delete(`/complaints/${id}`);
      alert("Complaint deleted");
      loadComplaints();
    } catch (error) {
      console.error(error);
      alert("Failed to delete complaint");
    }
  };

  // Filter complaints
  const filteredComplaints = complaints.filter((c) => {
    const matchCat = filterCategory === "ALL" || c.category === filterCategory;
    const matchStat = filterStatus === "ALL" || c.status === filterStatus;
    return matchCat && matchStat;
  });

  return (
    <>
      <Navbar />

      <div className="container">
        <div className="page-header-banner">
          <div>
            <h1>Complaints & Maintenance Portal</h1>
            <p>
              Report facility issues in plain English. AI automatically categorizes and prioritizes your ticket for quick resolution.
            </p>
          </div>
        </div>

        {/* =========================================================
            STUDENT ONLY: AI-ASSISTED COMPLAINT SUBMISSION
        ========================================================= */}
        {!isAdmin && (
          <div className="form-card ai-complaint-form-card">
            <div className="ai-section-title">
              <h2>Submit a New Complaint</h2>
              <p>Type what is wrong, and click AI Assist to automatically categorize and prioritize your request.</p>
            </div>

            <form onSubmit={submitComplaint}>
              <div className="form-row-grid">
                <div className="form-group">
                  <label>Student ID</label>
                  <input
                    type="number"
                    placeholder="Student ID"
                    value={form.studentId}
                    onChange={(e) =>
                      setForm({ ...form, studentId: e.target.value })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Subject / Title</label>
                  <input
                    type="text"
                    placeholder="Brief description of the issue"
                    value={form.subject}
                    onChange={(e) =>
                      setForm({ ...form, subject: e.target.value })
                    }
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Detailed Description</label>
                <textarea
                  placeholder="Explain what is broken, leaking, or needs attention in your room or common area..."
                  rows="4"
                  value={form.description}
                  onChange={(e) =>
                    setForm({ ...form, description: e.target.value })
                  }
                  required
                />
              </div>

              {/* AI Assist Action Button */}
              <div className="ai-assist-trigger-row">
                <button
                  type="button"
                  className="ai-assist-btn"
                  onClick={handleAIAssist}
                  disabled={aiLoading}
                >
                  {aiLoading ? "✨ Analyzing with AI..." : "✨ AI Auto-Categorize & Prioritize"}
                </button>
                <span className="ai-hint-text">
                  AI will analyze urgency, category, and explain the priority reason.
                </span>
              </div>

              {/* AI Suggestion Preview Box */}
              {aiSuggestion && (
                <div className="ai-suggestion-box">
                  <div className="ai-suggestion-header">
                    <strong>✨ AI Analysis Recommendations:</strong>
                    <button
                      type="button"
                      className="ai-accept-btn"
                      onClick={applyAISuggestions}
                    >
                      ✓ Accept Suggestions
                    </button>
                  </div>

                  <div className="ai-suggestion-grid">
                    <div>
                      <span className="sugg-label">Suggested Category:</span>
                      <span className="category-badge">{aiSuggestion.suggestedCategory}</span>
                    </div>
                    <div>
                      <span className="sugg-label">Suggested Priority:</span>
                      <span className={`priority-badge priority-${aiSuggestion.suggestedPriority?.toLowerCase()}`}>
                        {aiSuggestion.suggestedPriority}
                      </span>
                    </div>
                  </div>

                  <div className="sugg-reason">
                    <strong>Reason for Priority:</strong> {aiSuggestion.priorityReason}
                  </div>
                  <div className="sugg-summary">
                    <strong>AI Summary:</strong> {aiSuggestion.summary}
                  </div>
                </div>
              )}

              {/* Category & Priority Selectors (Editable by user) */}
              <div className="form-row-grid" style={{ marginTop: "15px" }}>
                <div className="form-group">
                  <label>Category</label>
                  <select
                    value={form.category}
                    onChange={(e) => setForm({ ...form, category: e.target.value })}
                  >
                    <option value="PLUMBING">Plumbing (Water / Taps / Pipes)</option>
                    <option value="ELECTRICITY">Electricity (Lights / Fans / Power)</option>
                    <option value="CLEANING">Cleaning & Housekeeping</option>
                    <option value="FOOD">Food & Kitchen</option>
                    <option value="MAINTENANCE">Carpentry & Maintenance</option>
                    <option value="INTERNET">Internet & Wi-Fi</option>
                    <option value="OTHER">Other Issues</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Priority</label>
                  <select
                    value={form.priority}
                    onChange={(e) => setForm({ ...form, priority: e.target.value })}
                  >
                    <option value="LOW">Low (Non-urgent)</option>
                    <option value="MEDIUM">Medium (Standard)</option>
                    <option value="HIGH">High (Urgent / Critical)</option>
                  </select>
                </div>
              </div>

              <button type="submit" className="ai-primary-btn" style={{ marginTop: "15px", width: "100%" }}>
                Submit Complaint Ticket
              </button>
            </form>
          </div>
        )}

        {/* =========================================================
            ADMIN INFO MESSAGE
        ========================================================= */}
        {isAdmin && (
          <div className="student-info">
            <p>
              Manage student complaints, prioritize urgent maintenance tickets, and track resolution statuses.
            </p>
          </div>
        )}

        {/* Filter Bar */}
        <div className="complaint-filter-bar">
          <div className="filter-group">
            <label>Filter by Category:</label>
            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
            >
              <option value="ALL">All Categories</option>
              <option value="PLUMBING">Plumbing</option>
              <option value="ELECTRICITY">Electricity</option>
              <option value="CLEANING">Cleaning</option>
              <option value="FOOD">Food</option>
              <option value="MAINTENANCE">Maintenance</option>
              <option value="INTERNET">Internet</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Filter by Status:</label>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>
        </div>

        {/* =========================================================
            COMPLAINTS TABLE
        ========================================================= */}
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Category</th>
                <th>Priority</th>
                <th>Subject</th>
                <th>Description / AI Summary</th>
                <th>Status</th>
                {isAdmin && <th>Action</th>}
              </tr>
            </thead>

            <tbody>
              {filteredComplaints.length === 0 ? (
                <tr>
                  <td colSpan={isAdmin ? 8 : 7} style={{ textAlign: "center" }}>
                    No complaints found matching the criteria.
                  </td>
                </tr>
              ) : (
                filteredComplaints.map((complaint) => (
                  <tr key={complaint.id}>
                    <td>#{complaint.id}</td>
                    <td>{complaint.student?.name || "Student"}</td>
                    <td>
                      <span className="category-pill">
                        {complaint.category || "OTHER"}
                      </span>
                    </td>
                    <td>
                      <span
                        className={`priority-badge priority-${(
                          complaint.priority || "MEDIUM"
                        ).toLowerCase()}`}
                      >
                        {complaint.priority === "HIGH" && "🔴 "}
                        {complaint.priority === "MEDIUM" && "🟠 "}
                        {complaint.priority === "LOW" && "🔵 "}
                        {complaint.priority || "MEDIUM"}
                      </span>
                    </td>
                    <td>
                      <strong>{complaint.subject}</strong>
                    </td>
                    <td style={{ maxWidth: "280px" }}>
                      <p>{complaint.description}</p>
                      {complaint.priorityReason && (
                        <div className="priority-reason-hint">
                          <em>Why {complaint.priority}: {complaint.priorityReason}</em>
                        </div>
                      )}
                    </td>
                    <td>
                      <span
                        className={
                          complaint.status === "RESOLVED"
                            ? "status-paid"
                            : "status-pending"
                        }
                      >
                        {complaint.status}
                      </span>
                    </td>
                    {isAdmin && (
                      <td>
                        {complaint.status !== "RESOLVED" && (
                          <button
                            className="paid-btn"
                            onClick={() => resolveComplaint(complaint.id)}
                          >
                            Resolve
                          </button>
                        )}
                        <button
                          className="delete-btn"
                          onClick={() => deleteComplaint(complaint.id)}
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
    </>
  );
}

export default Complaints;