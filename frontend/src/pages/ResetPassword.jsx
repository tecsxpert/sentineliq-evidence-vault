import { useMemo, useState } from "react";
import API from "../services/api";

function ResetPassword({ setPage }) {
  const [newPassword, setNewPassword] = useState("");
  const token = useMemo(() => new URLSearchParams(window.location.search).get("token") || "", []);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await API.post("/auth/reset-password", {
        token,
        newPassword,
      });
      alert(response.data);
      window.history.replaceState({}, "", "/");
      setPage("login");
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Unable to reset password");
    }
  };

  return (
    <div className="flex justify-center items-center h-[80vh]">
      <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-lg p-6 w-96">
        <h2 className="text-xl font-bold mb-4 text-center">Reset Password</h2>

        <input
          type="password"
          placeholder="New Password"
          className="border p-2 w-full mb-3 rounded"
          value={newPassword}
          onChange={(event) => setNewPassword(event.target.value)}
        />

        <button disabled={!token} className="bg-indigo-500 hover:bg-indigo-600 text-white w-full p-2 rounded disabled:opacity-50">
          Reset Password
        </button>

        {!token && <p className="text-sm text-red-600 text-center mt-3">Reset token is missing.</p>}
      </form>
    </div>
  );
}

export default ResetPassword;
