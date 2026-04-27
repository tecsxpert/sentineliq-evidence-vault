import { useState, useEffect } from "react";
import ListPage from "./pages/ListPage";
import CreatePage from "./pages/CreatePage";
import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";
import Register from "./pages/Register";

function App() {
  const [page, setPage] = useState("home");
  const [selectedItem, setSelectedItem] = useState(null);

  const isLoggedIn = !!localStorage.getItem("token");

  // 🔥 Redirect logic (SAFE way)
  useEffect(() => {
    if (
      !isLoggedIn &&
      page !== "login" &&
      page !== "register" &&
      page !== "home"
    ) {
      setPage("login");
    }
  }, [page, isLoggedIn]);

  return (
    <div className="min-h-screen bg-gray-100">

      {/* 🔷 NAVBAR */}
      <div className="bg-slate-800 text-white px-6 py-4 shadow flex items-center justify-between">

        {/* TITLE */}
        <h1 className="text-2xl font-bold tracking-wide">
          Evidence Vault
        </h1>

        {/* NAVIGATION */}
        <div className="flex gap-4">

          {/* 🔓 NOT LOGGED IN */}
          {!isLoggedIn && (
            <>
              <button
                onClick={() => setPage("login")}
                className="bg-indigo-500 hover:bg-indigo-600 px-4 py-2 rounded"
              >
                Login
              </button>

              <button
                onClick={() => setPage("register")}
                className="bg-gray-300 hover:bg-gray-400 text-black px-4 py-2 rounded"
              >
                Register
              </button>
            </>
          )}

          {/* 🔐 LOGGED IN */}
          {isLoggedIn && (
            <>
              <button
                onClick={() => setPage("dashboard")}
                className="bg-slate-600 hover:bg-slate-500 px-4 py-2 rounded"
              >
                Dashboard
              </button>

              <button
                onClick={() => {
                  setSelectedItem(null);
                  setPage("list");
                }}
                className="bg-slate-600 hover:bg-slate-500 px-4 py-2 rounded"
              >
                List
              </button>

              <button
                onClick={() => {
                  setSelectedItem(null);
                  setPage("create");
                }}
                className="bg-slate-600 hover:bg-slate-500 px-4 py-2 rounded"
              >
                Create
              </button>

              <button
                onClick={() => {
                  localStorage.removeItem("token");
                  setPage("home");
                }}
                className="bg-red-500 hover:bg-red-600 px-4 py-2 rounded"
              >
                Logout
              </button>
            </>
          )}
        </div>
      </div>

      {/* 🔥 CONTENT */}
      <div className="p-6 max-w-5xl mx-auto">

        {page === "home" && <Home setPage={setPage} />}
        {page === "login" && <Login setPage={setPage} />}
        {page === "register" && <Register setPage={setPage} />}

        {page === "dashboard" && isLoggedIn && <Dashboard />}

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

      </div>
    </div>
  );
}

export default App;