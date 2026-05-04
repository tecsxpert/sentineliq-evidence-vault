import { useState } from "react";
import API from "../services/api";

function ForgotPassword({ setPage }) {
  const [email, setEmail] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await API.post("/auth/forgot-password", { email });
      alert(response.data);
      setPage("login");
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Unable to request password reset");
    }
  };

  return (
    <div className="flex justify-center items-center h-[80vh]">
      <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-lg p-6 w-96">
        <h2 className="text-xl font-bold mb-4 text-center">Forgot Password</h2>

        <input
          type="email"
          placeholder="Email"
          className="border p-2 w-full mb-3 rounded"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />

        <button className="bg-indigo-500 hover:bg-indigo-600 text-white w-full p-2 rounded">
          Send Reset Link
        </button>

        <p className="text-sm text-center mt-4">
          <span onClick={() => setPage("login")} className="text-indigo-500 cursor-pointer hover:underline">
            Back to Login
          </span>
        </p>
      </form>
    </div>
  );
}

export default ForgotPassword;
