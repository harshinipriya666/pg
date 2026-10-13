import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import API from "../services/api";

function Allocations() {

  const [allocations, setAllocations] = useState([]);

  const [studentId, setStudentId] = useState("");
  const [roomId, setRoomId] = useState("");

  const loadAllocations = async () => {

    const response = await API.get("/allocations");

    setAllocations(response.data);
  };

  useEffect(() => {
    loadAllocations();
  }, []);

  const allocateRoom = async (e) => {

    e.preventDefault();

    try {

      await API.post("/allocations", {
        studentId: Number(studentId),
        roomId: Number(roomId)
      });

      setStudentId("");
      setRoomId("");

      loadAllocations();

    } catch (error) {

      alert(
        error.response?.data?.message ||
        "Allocation failed"
      );

    }
  };

  const deleteAllocation = async (id) => {

    await API.delete(`/allocations/${id}`);

    loadAllocations();
  };

  return (
    <>
      <Navbar />

      <div className="container">

        <h1>Room Allocations</h1>

        <form
          className="form-card"
          onSubmit={allocateRoom}
        >

          <input
            type="number"
            placeholder="Student ID"
            value={studentId}
            onChange={(e) =>
              setStudentId(e.target.value)
            }
            required
          />

          <input
            type="number"
            placeholder="Room ID"
            value={roomId}
            onChange={(e) =>
              setRoomId(e.target.value)
            }
            required
          />

          <button type="submit">
            Allocate Room
          </button>

        </form>

        <table>

          <thead>
            <tr>
              <th>ID</th>
              <th>Student</th>
              <th>Room</th>
              <th>Date</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>

            {allocations.map((item) => (

              <tr key={item.id}>

                <td>{item.id}</td>

                <td>
                  {item.student?.name}
                </td>

                <td>
                  {item.room?.roomNumber}
                </td>

                <td>
                  {item.allocationDate}
                </td>

                <td>
                  {item.status}
                </td>

                <td>
                  <button
                    className="delete-btn"
                    onClick={() =>
                      deleteAllocation(item.id)
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

export default Allocations;