import { Routes, Route } from "react-router-dom";

// Authentication
import Login from "./pages/Login";
import Register from "./pages/Register";

// Dashboards
import StudentDashboard from "./pages/StudentDashboard";
import AdminDashboard from "./pages/AdminDashboard";

// Management pages
import Students from "./pages/Students";
import Rooms from "./pages/Rooms";
import Allocations from "./pages/Allocations";
import Fees from "./pages/Fees";
import Complaints from "./pages/Complaints";
import Reviews from "./pages/Reviews";
import Meals from "./pages/Meals";

// Protection
import ProtectedRoute from "./components/ProtectedRoute";

// AI Assistant
import AIChatbot from "./components/AIChatbot";

function App() {
    return (
        <>
        <Routes>

            {/* =========================
                PUBLIC ROUTES
            ========================= */}

            <Route path="/" element={<Login />} />

            <Route
                path="/login"
                element={<Login />}
            />

            <Route
                path="/register"
                element={<Register />}
            />


            {/* =========================
                STUDENT DASHBOARD
            ========================= */}

            <Route
                path="/student-dashboard"
                element={
                    <ProtectedRoute>
                        <StudentDashboard />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                ADMIN DASHBOARD
            ========================= */}

            <Route
                path="/admin-dashboard"
                element={
                    <ProtectedRoute>
                        <AdminDashboard />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                STUDENTS
            ========================= */}

            <Route
                path="/students"
                element={
                    <ProtectedRoute>
                        <Students />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                ROOMS
            ========================= */}

            <Route
                path="/rooms"
                element={
                    <ProtectedRoute>
                        <Rooms />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                ALLOCATIONS
            ========================= */}

            <Route
                path="/allocations"
                element={
                    <ProtectedRoute>
                        <Allocations />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                FEES
            ========================= */}

            <Route
                path="/fees"
                element={
                    <ProtectedRoute>
                        <Fees />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                COMPLAINTS
            ========================= */}

            <Route
                path="/complaints"
                element={
                    <ProtectedRoute>
                        <Complaints />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                REVIEWS
            ========================= */}

            <Route
                path="/reviews"
                element={
                    <ProtectedRoute>
                        <Reviews />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                MEAL SCHEDULE
            ========================= */}

            <Route
                path="/meals"
                element={
                    <ProtectedRoute>
                        <Meals />
                    </ProtectedRoute>
                }
            />


            {/* =========================
                INVALID URL
            ========================= */}

            <Route
                path="*"
                element={<Login />}
            />

        </Routes>
        <AIChatbot />
        </>
    );
}

export default App;