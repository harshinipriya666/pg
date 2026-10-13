import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import API from "../services/api";

function Students() {

  const [students, setStudents] = useState([]);

  const loadStudents = async () => {

    try {

      const response = await API.get("/students");

      setStudents(response.data);

    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    loadStudents();
  }, []);

  const deleteStudent = async (id) => {

    if (!window.confirm("Delete this student?")) {
      return;
    }

    await API.delete(`/students/${id}`);

    loadStudents();
  };

  return (
    <>
      <Navbar />

      <div className="container">

        <h1>Students</h1>

        <table>

          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Course</th>
              <th>Year</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>

            {students.map((student) => (

              <tr key={student.id}>

                <td>{student.id}</td>
                <td>{student.name}</td>
                <td>{student.email}</td>
                <td>{student.phone}</td>
                <td>{student.course}</td>
                <td>{student.year}</td>

                <td>
                  <button
                    className="delete-btn"
                    onClick={() =>
                      deleteStudent(student.id)
                    }
                  >
                    Delete
                  </button>
                </td>

              </tr>

            ))}

          </tbody>

        </table>

      </div>
    </>
  );
}

export default Students;