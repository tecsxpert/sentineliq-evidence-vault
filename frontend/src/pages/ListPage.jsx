import { useEffect, useState } from "react";
import { getAllEvidence, searchEvidence } from "../services/evidenceService";
import API from "../services/api";

function ListPage({ setPage: setAppPage, setSelectedItem }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  // 🔥 pagination states
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 🔥 fetch data with pagination + search
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        const result = search
          ? await searchEvidence(search, page, 5)
          : await getAllEvidence(page, 5);

        setData(result.content);
        setTotalPages(result.totalPages);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [page, search]);

  // 🔥 delete
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete?");
    if (!confirmDelete) return;

    try {
      await API.delete(`/${id}`);

      const result = await getAllEvidence(page, 5);
      setData(result.content);
      setTotalPages(result.totalPages);
    } catch (error) {
      console.error(error);
    }
  };

  if (loading) {
    return <div className="p-5 text-lg">Loading...</div>;
  }

  if (data.length === 0) {
    return <div className="p-5 text-lg">No data available</div>;
  }

  return (
    <div className="bg-white shadow-md rounded-lg p-6">
      <h1 className="text-2xl font-bold mb-4 text-gray-800">
        Evidence List
      </h1>

      {/* 🔍 SEARCH BAR (ADDED HERE) */}
      <div className="mb-4 flex gap-2">
        <input
          type="text"
          placeholder="Search by name, type, status..."
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
          className="border p-2 rounded w-full focus:ring-2 focus:ring-indigo-400"
        />

        {search && (
          <button
            onClick={() => setSearch("")}
            className="px-3 py-2 bg-gray-300 rounded"
          >
            Clear
          </button>
        )}
      </div>

      <table className="w-full text-left">
        <thead className="bg-gray-100 text-gray-700">
          <tr>
            <th className="p-3">ID</th>
            <th className="p-3">Name</th>
            <th className="p-3">Type</th>
            <th className="p-3">Status</th>
            <th className="p-3">Priority</th>
            <th className="p-3">Assigned To</th>
            <th className="p-3">Date</th>
            <th className="p-3">Deadline</th>
            <th className="p-3 text-center">Actions</th>
          </tr>
        </thead>

        <tbody>
          {data.map((item) => (
            <tr key={item.id} className="border-t hover:bg-gray-50">
              <td className="p-3">{item.id}</td>
              <td className="p-3 font-medium">{item.name}</td>
              <td className="p-3">{item.type || "-"}</td>
              <td className="p-3">{item.status}</td>
              <td className="p-3">{item.priority || "-"}</td>
              <td className="p-3">{item.assignedTo || "-"}</td>
              <td className="p-3">
                {item.dateCollected || "-"}
              </td>
              <td className="p-3">
                {item.deadline || "-"}
              </td>

              <td className="p-3 flex gap-3 justify-center">
                <button
                  onClick={() => {
                    setSelectedItem(item);
                    setAppPage("edit");
                  }}
                  className="bg-amber-400 hover:bg-amber-500 text-black px-3 py-1 rounded"
                >
                  Edit
                </button>

                <button
                  onClick={() => handleDelete(item.id)}
                  className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 🔥 PAGINATION */}
      <div className="flex justify-between items-center mt-6">
        <button
          onClick={() => setPage(page - 1)}
          disabled={page === 0}
          className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
        >
          Previous
        </button>

        <span className="text-sm font-medium">
          Page {page + 1} of {totalPages}
        </span>

        <button
          onClick={() => setPage(page + 1)}
          disabled={page === totalPages - 1}
          className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}

export default ListPage;