import React, { useEffect, useState } from "react";
import api from "../services/api";

function Meals() {

  const [meals, setMeals] = useState([]);

  const [form, setForm] = useState({
    mealType: "Breakfast",
    mealTime: "",
    scheduleDate: "",
    notificationMessage: ""
  });

  // Get logged-in user
  const user = JSON.parse(localStorage.getItem("user"));

  // Check whether logged-in user is admin
  const isAdmin = user?.role === "ADMIN";

  // Load meals
  const loadMeals = async () => {
    try {
      const response = await api.get("/meals");
      setMeals(response.data);
    } catch (error) {
      console.error("Failed to load meals", error);
    }
  };

  useEffect(() => {
    loadMeals();
  }, []);

  // Handle form changes
  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };

  // Add meal - ADMIN ONLY
  const addMeal = async (e) => {
    e.preventDefault();

    if (!isAdmin) {
      alert("Only admin can manage meal schedules.");
      return;
    }

    try {
      await api.post("/meals", form);

      alert("Meal schedule added successfully!");

      setForm({
        mealType: "Breakfast",
        mealTime: "",
        scheduleDate: "",
        notificationMessage: ""
      });

      loadMeals();

    } catch (error) {
      console.error(error);
      alert("Failed to add meal schedule");
    }
  };

  // Delete meal - ADMIN ONLY
  const deleteMeal = async (id) => {

    if (!isAdmin) {
      alert("Only admin can delete meal schedules.");
      return;
    }

    if (!window.confirm("Are you sure you want to delete this meal schedule?")) {
      return;
    }

    try {
      await api.delete(`/meals/${id}`);

      alert("Meal schedule deleted successfully!");

      loadMeals();

    } catch (error) {
      console.error(error);
      alert("Failed to delete meal schedule");
    }
  };

  return (
    <div className="page-container">

      <h1>Meal Schedule</h1>

      {/* ================= ADMIN SECTION ================= */}
      {isAdmin && (
        <div className="card">

          <h2>Manage Meal Schedule</h2>

          <form onSubmit={addMeal}>

            <select
              name="mealType"
              value={form.mealType}
              onChange={handleChange}
              required
            >
              <option value="Breakfast">Breakfast</option>
              <option value="Lunch">Lunch</option>
              <option value="Dinner">Dinner</option>
            </select>

            <input
              type="time"
              name="mealTime"
              value={form.mealTime}
              onChange={handleChange}
              required
            />

            <input
              type="date"
              name="scheduleDate"
              value={form.scheduleDate}
              onChange={handleChange}
              required
            />

            <input
              type="text"
              name="notificationMessage"
              placeholder="Notification message"
              value={form.notificationMessage}
              onChange={handleChange}
              required
            />

            <button type="submit">
              Add Meal Schedule
            </button>

          </form>

        </div>
      )}

      {/* ================= STUDENT MESSAGE ================= */}
      {!isAdmin && (
        <div className="student-notice">
          <strong>Meal Notifications</strong>
          <p>
            Meal timings and notifications are managed by the administrator.
          </p>
        </div>
      )}

      {/* ================= MEAL TABLE ================= */}

      <div className="table-container">

        <table>

          <thead>
            <tr>
              <th>Meal</th>
              <th>Time</th>
              <th>Date</th>
              <th>Notification</th>

              {/* Action column only for Admin */}
              {isAdmin && <th>Action</th>}
            </tr>
          </thead>

          <tbody>

            {meals.length === 0 ? (

              <tr>
                <td
                  colSpan={isAdmin ? "5" : "4"}
                  style={{ textAlign: "center" }}
                >
                  No meal schedules available
                </td>
              </tr>

            ) : (

              meals.map((meal) => (

                <tr key={meal.id}>

                  <td>{meal.mealType}</td>

                  <td>{meal.mealTime}</td>

                  <td>{meal.scheduleDate}</td>

                  <td>{meal.notificationMessage}</td>

                  {/* Delete button only for Admin */}
                  {isAdmin && (
                    <td>
                      <button
                        className="delete-btn"
                        onClick={() => deleteMeal(meal.id)}
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

export default Meals;