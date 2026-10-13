import React, { useEffect, useState } from "react";
import api from "../services/api";

function Fees() {
  const user = JSON.parse(localStorage.getItem("user"));
  const isAdmin = user?.role === "ADMIN";

  const [fees, setFees] = useState([]);
  const [students, setStudents] = useState([]);

  const [form, setForm] = useState({
    studentId: "",
    amount: "",
    dueDate: ""
  });

  // Get all fees
  const loadFees = async () => {
    try {
      const response = await api.get("/fees");

      if (isAdmin) {
        // Admin can see all fees
        setFees(response.data);
      } else {
        // Student should see only their own fees
        const studentResponse = await api.get("/students");

        const currentStudent = studentResponse.data.find(
          (student) => student.email === user.email
        );

        if (currentStudent) {
          const studentFees = await api.get(
            `/fees/student/${currentStudent.id}`
          );

          setFees(studentFees.data);
        } else {
          setFees([]);
        }
      }
    } catch (error) {
      console.error(error);
      alert("Failed to load fees");
    }
  };

  // Get students - only needed for admin
  const loadStudents = async () => {
    if (!isAdmin) return;

    try {
      const response = await api.get("/students");
      setStudents(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    loadFees();
    loadStudents();
  }, []);

  // Add fee - ADMIN ONLY
  const addFee = async (e) => {
    e.preventDefault();

    if (!isAdmin) {
      alert("Only admin can add fees");
      return;
    }

    try {
      await api.post(`/fees/student/${form.studentId}`, {
        amount: Number(form.amount),
        dueDate: form.dueDate
      });

      alert("Fee added successfully");

      setForm({
        studentId: "",
        amount: "",
        dueDate: ""
      });

      loadFees();
    } catch (error) {
      console.error(error);
      alert("Failed to add fee");
    }
  };

  // Mark fee as PAID - ADMIN ONLY
  const markPaid = async (id) => {
    if (!isAdmin) {
      alert("Only admin can update fee status");
      return;
    }

    try {
      await api.put(`/fees/${id}/status?value=PAID`);

      alert("Fee marked as paid");

      loadFees();
    } catch (error) {
      console.error(error);
      alert("Failed to update fee");
    }
  };

  // Delete fee - ADMIN ONLY
  const deleteFee = async (id) => {
    if (!isAdmin) {
      alert("Only admin can delete fees");
      return;
    }

    if (!window.confirm("Are you sure you want to delete this fee?")) {
      return;
    }

    try {
      await api.delete(`/fees/${id}`);

      alert("Fee deleted successfully");

      loadFees();
    } catch (error) {
      console.error(error);
      alert("Failed to delete fee");
    }
  };

  return (
    <div className="page-container">

      <h1>Fees</h1>

      {/* ADMIN ONLY - ADD FEE FORM */}
      {isAdmin && (
        <div className="form-card">

          <h2>Add Student Fee</h2>

          <form onSubmit={addFee}>

            <select
              value={form.studentId}
              onChange={(e) =>
                setForm({
                  ...form,
                  studentId: e.target.value
                })
              }
              required
            >
              <option value="">Select Student</option>

              {students.map((student) => (
                <option key={student.id} value={student.id}>
                  {student.name} - {student.email}
                </option>
              ))}
            </select>

            <input
              type="number"
              placeholder="Amount"
              value={form.amount}
              onChange={(e) =>
                setForm({
                  ...form,
                  amount: e.target.value
                })
              }
              required
            />

            <input
              type="date"
              value={form.dueDate}
              onChange={(e) =>
                setForm({
                  ...form,
                  dueDate: e.target.value
                })
              }
              required
            />

            <button type="submit">
              Add Fee
            </button>

          </form>

        </div>
      )}

      {/* STUDENT MESSAGE */}
      {!isAdmin && (
        <div className="student-info">
          <p>
            You can view your fee details here.
            Only the administrator can add or update fees.
          </p>
        </div>
      )}

      {/* FEES TABLE */}

      <div className="table-container">

        <table>

          <thead>
            <tr>
              <th>ID</th>

              {isAdmin && <th>Student</th>}

              <th>Amount</th>
              <th>Due Date</th>
              <th>Status</th>

              {isAdmin && <th>Action</th>}
            </tr>
          </thead>

          <tbody>

            {fees.length === 0 ? (

              <tr>
                <td
                  colSpan={isAdmin ? "6" : "4"}
                  style={{ textAlign: "center" }}
                >
                  No fee records found
                </td>
              </tr>

            ) : (

              fees.map((fee) => (

                <tr key={fee.id}>

                  <td>{fee.id}</td>

                  {isAdmin && (
                    <td>
                      {fee.student?.name || "Student"}
                    </td>
                  )}

                  <td>₹{fee.amount}</td>

                  <td>{fee.dueDate}</td>

                  <td>
                    <span
                      className={
                        fee.paymentStatus === "PAID"
                          ? "status-paid"
                          : "status-pending"
                      }
                    >
                      {fee.paymentStatus}
                    </span>
                  </td>

                  {isAdmin && (

                    <td>

                      {fee.paymentStatus !== "PAID" && (
                        <button
                          className="paid-btn"
                          onClick={() => markPaid(fee.id)}
                        >
                          Mark Paid
                        </button>
                      )}

                      <button
                        className="delete-btn"
                        onClick={() => deleteFee(fee.id)}
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
  );
}

export default Fees;