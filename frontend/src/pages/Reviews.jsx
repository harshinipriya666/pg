import React, { useEffect, useState } from "react";
import api from "../services/api";

function Reviews() {

  const user = JSON.parse(localStorage.getItem("user"));
  const isAdmin = user?.role === "ADMIN";

  const [reviews, setReviews] = useState([]);

  const [form, setForm] = useState({
    studentId: "",
    rating: "",
    accommodationRating: "",
    foodRating: "",
    cleanlinessRating: "",
    facilitiesRating: "",
    comment: ""
  });

  // Load reviews
  const loadReviews = async () => {

    try {

      if (isAdmin) {

        // ADMIN - view all reviews
        const response = await api.get("/reviews");

        setReviews(response.data);

      } else {

        // STUDENT - find logged-in student
        const studentsResponse = await api.get("/students");

        const currentStudent = studentsResponse.data.find(
          student => student.email === user.email
        );

        if (currentStudent) {

          const response = await api.get(
            `/reviews/student/${currentStudent.id}`
          );

          setReviews(response.data);

        } else {

          setReviews([]);

        }
      }

    } catch (error) {

      console.error(error);
      alert("Failed to load reviews");

    }
  };

  useEffect(() => {
    loadReviews();
  }, []);

  // STUDENT ONLY - Submit review
  const submitReview = async (e) => {

    e.preventDefault();

    if (isAdmin) {
      alert("Admin cannot submit reviews");
      return;
    }

    try {

      await api.post(
        `/reviews/student/${form.studentId}`,
        {
          rating: Number(form.rating),
          accommodationRating: Number(form.accommodationRating),
          foodRating: Number(form.foodRating),
          cleanlinessRating: Number(form.cleanlinessRating),
          facilitiesRating: Number(form.facilitiesRating),
          comment: form.comment
        }
      );

      alert("Review submitted successfully");

      setForm({
        studentId: "",
        rating: "",
        accommodationRating: "",
        foodRating: "",
        cleanlinessRating: "",
        facilitiesRating: "",
        comment: ""
      });

      loadReviews();

    } catch (error) {

      console.error(error);
      alert("Failed to submit review");

    }
  };

  // ADMIN ONLY - Delete review
  const deleteReview = async (id) => {

    if (!isAdmin) {
      alert("Only admin can delete reviews");
      return;
    }

    if (!window.confirm("Delete this review?")) {
      return;
    }

    try {

      await api.delete(`/reviews/${id}`);

      alert("Review deleted successfully");

      loadReviews();

    } catch (error) {

      console.error(error);
      alert("Failed to delete review");

    }
  };

  return (
    <div className="page-container">

      <h1>Reviews</h1>

      {/* =================================
          STUDENT ONLY REVIEW FORM
          ================================= */}

      {!isAdmin && (

        <div className="form-card">

          <h2>Give Your Review</h2>

          <form onSubmit={submitReview}>

            <input
              type="number"
              placeholder="Student ID"
              value={form.studentId}
              onChange={(e) =>
                setForm({
                  ...form,
                  studentId: e.target.value
                })
              }
              required
            />

            <input
              type="number"
              min="1"
              max="5"
              placeholder="Overall Rating (1-5)"
              value={form.rating}
              onChange={(e) =>
                setForm({
                  ...form,
                  rating: e.target.value
                })
              }
              required
            />

            <input
              type="number"
              min="1"
              max="5"
              placeholder="Accommodation Rating (1-5)"
              value={form.accommodationRating}
              onChange={(e) =>
                setForm({
                  ...form,
                  accommodationRating: e.target.value
                })
              }
              required
            />

            <input
              type="number"
              min="1"
              max="5"
              placeholder="Food Rating (1-5)"
              value={form.foodRating}
              onChange={(e) =>
                setForm({
                  ...form,
                  foodRating: e.target.value
                })
              }
              required
            />

            <input
              type="number"
              min="1"
              max="5"
              placeholder="Cleanliness Rating (1-5)"
              value={form.cleanlinessRating}
              onChange={(e) =>
                setForm({
                  ...form,
                  cleanlinessRating: e.target.value
                })
              }
              required
            />

            <input
              type="number"
              min="1"
              max="5"
              placeholder="Facilities Rating (1-5)"
              value={form.facilitiesRating}
              onChange={(e) =>
                setForm({
                  ...form,
                  facilitiesRating: e.target.value
                })
              }
              required
            />

            <textarea
              placeholder="Write your review"
              value={form.comment}
              onChange={(e) =>
                setForm({
                  ...form,
                  comment: e.target.value
                })
              }
              required
            />

            <button type="submit">
              Submit Review
            </button>

          </form>

        </div>

      )}

      {/* =================================
          ADMIN MESSAGE
          ================================= */}

      {isAdmin && (

        <div className="student-info">

          <p>
            View student reviews and feedback from this section.
            Only students can submit reviews.
          </p>

        </div>

      )}

      {/* =================================
          REVIEWS TABLE
          ================================= */}

      <div className="table-container">

        <table>

          <thead>

            <tr>

              <th>ID</th>
              <th>Student</th>
              <th>Overall</th>
              <th>Accommodation</th>
              <th>Food</th>
              <th>Cleanliness</th>
              <th>Facilities</th>
              <th>Comment</th>

              {isAdmin && (
                <th>Action</th>
              )}

            </tr>

          </thead>

          <tbody>

            {reviews.length === 0 ? (

              <tr>

                <td
                  colSpan={isAdmin ? 9 : 8}
                  style={{ textAlign: "center" }}
                >
                  No reviews found
                </td>

              </tr>

            ) : (

              reviews.map((review) => (

                <tr key={review.id}>

                  <td>
                    {review.id}
                  </td>

                  <td>
                    {review.student?.name || "Student"}
                  </td>

                  <td>
                    ⭐ {review.rating}/5
                  </td>

                  <td>
                    {review.accommodationRating}/5
                  </td>

                  <td>
                    {review.foodRating}/5
                  </td>

                  <td>
                    {review.cleanlinessRating}/5
                  </td>

                  <td>
                    {review.facilitiesRating}/5
                  </td>

                  <td>
                    {review.comment}
                  </td>

                  {isAdmin && (

                    <td>

                      <button
                        className="delete-btn"
                        onClick={() =>
                          deleteReview(review.id)
                        }
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

export default Reviews;