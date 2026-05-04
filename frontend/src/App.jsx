import { useEffect, useState } from "react";
import ListPage from "./pages/ListPage";
import CreatePage from "./pages/CreatePage";
import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Analytics from "./pages/Analytics";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

function App() {
  const initialPage = window.location.pathname === "/reset-password" ? "reset-password" : "home";
  const [page, setPage] = useState(initialPage);
  const [selectedItem, setSelectedItem] = useState(null);

  const isLoggedIn = !!localStorage.getItem("token");

  useEffect(() => {
    if (!isLoggedIn && !["login", "register", "forgot-password", "reset-password", "home"].includes(page)) {
      setPage("login");
    }
  }, [page, isLoggedIn]);

  return (
    <div className="min-h-screen bg-gray-100 font-sans">
      <div className="bg-[#1B4F8A] px-4 py-4 text-white shadow sm:px-6">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <h1 className="text-2xl font-bold">Evidence Vault</h1>

          <div className="flex flex-wrap gap-3">
            {!isLoggedIn && (
              <>
                <button
                  onClick={() => setPage("login")}
                  className="min-h-11 rounded bg-white px-4 py-2 text-[#1B4F8A] hover:bg-gray-100"
                >
                  Login
                </button>

                <button
                  onClick={() => setPage("register")}
                  className="min-h-11 rounded bg-gray-200 px-4 py-2 text-black hover:bg-gray-300"
                >
                  Register
                </button>
              </>
            )}

            {isLoggedIn && (
              <>
                <button onClick={() => setPage("dashboard")} className="min-h-11 rounded bg-white/15 px-4 py-2 hover:bg-white/25">
                  Dashboard
                </button>
                <button onClick={() => setPage("analytics")} className="min-h-11 rounded bg-white/15 px-4 py-2 hover:bg-white/25">
                  Analytics
                </button>
                <button
                  onClick={() => {
                    setSelectedItem(null);
                    setPage("list");
                  }}
                  className="min-h-11 rounded bg-white/15 px-4 py-2 hover:bg-white/25"
                >
                  List
                </button>
                <button
                  onClick={() => {
                    setSelectedItem(null);
                    setPage("create");
                  }}
                  className="min-h-11 rounded bg-white/15 px-4 py-2 hover:bg-white/25"
                >
                  Create
                </button>
                <button
                  onClick={() => {
                    localStorage.removeItem("token");
                    setPage("home");
                  }}
                  className="min-h-11 rounded bg-red-600 px-4 py-2 hover:bg-red-700"
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      <main className="mx-auto max-w-6xl p-4 sm:p-6">
        {page === "home" && <Home setPage={setPage} />}
        {page === "login" && <Login setPage={setPage} />}
        {page === "register" && <Register setPage={setPage} />}
        {page === "forgot-password" && <ForgotPassword setPage={setPage} />}
        {page === "reset-password" && <ResetPassword setPage={setPage} />}
        {page === "dashboard" && isLoggedIn && <Dashboard />}
        {page === "analytics" && isLoggedIn && <Analytics />}
        {page === "list" && isLoggedIn && (
          <ListPage setPage={setPage} setSelectedItem={setSelectedItem} />
        )}
        {(page === "create" || page === "edit") && isLoggedIn && (
          <CreatePage
            selectedItem={selectedItem}
            refreshList={() => setPage("list")}
            goToList={() => setPage("list")}
          />
        )}
      </main>
    </div>
  );
}

export default App;
