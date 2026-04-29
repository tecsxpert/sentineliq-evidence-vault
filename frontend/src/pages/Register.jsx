import { useState } from "react";
import API from "../services/api";

function Register({ setPage }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleRegister = async (e) => {
    e.preventDefault();

    try {
      await API.post("/auth/register", {
        username,
        password,
      });

      alert("Registered successfully!");
      setPage("login");
    } catch (error) {
      console.error(error);
      alert("Error registering");
    }
console.log("Sending:", { username, password });
  };

  return (
    <div className="flex justify-center items-center h-[80vh]">
      <form
        onSubmit={handleRegister}
        className="bg-white shadow-md rounded-lg p-6 w-96"
      >
        <h2 className="text-xl font-bold mb-4 text-center">Register</h2>

        <input
          type="text"
          placeholder="Username"
          className="border p-2 w-full mb-3 rounded"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <input
          type="password"
          placeholder="Password"
          className="border p-2 w-full mb-3 rounded"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button className="bg-green-500 hover:bg-green-600 text-white w-full p-2 rounded">
          Register
        </button>
        <p className="text-sm text-center mt-4">
          Already have an account?{" "}
          <span
            onClick={() => setPage("login")}
            className="text-indigo-500 cursor-pointer hover:underline"
          >
            Login
          </span>
        </p>
      </form>
    </div>
  );
}

export default Register;