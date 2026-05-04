import { useEffect, useMemo, useState } from "react";
import { getAllEvidence, searchEvidence } from "../services/evidenceService";
import API from "../services/api";

const initialFilters = {
  q: "",
  type: "",
  status: "",
  fromDate: "",
  toDate: "",
};

const displayValue = (item, keys) => {
  for (const key of keys) {
    const value = item?.[key];
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      return value;
    }
  }
  return "-";
};

function ListPage({ setPage: setAppPage, setSelectedItem }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState(initialFilters);
  const [debouncedFilters, setDebouncedFilters] = useState(initialFilters);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const hasFilters = useMemo(
    () => Object.values(debouncedFilters).some((value) => value),
    [debouncedFilters]
  );

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setDebouncedFilters(filters);
      setPage(0);
    }, 350);

    return () => window.clearTimeout(timer);
  }, [filters]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        const result = hasFilters
          ? await searchEvidence(debouncedFilters, page, 5)
          : await getAllEvidence(page, 5);

        setData(result.content || []);
        setTotalPages(result.totalPages || 0);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [page, debouncedFilters, hasFilters]);

  const updateFilter = (name, value) => {
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const downloadCSV = async () => {
    try {
      const res = await API.get("/export", { responseType: "blob" });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");

      link.href = url;
      link.setAttribute("download", "evidence.csv");
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("CSV download failed", error);
    }
  };

  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete?");
    if (!confirmDelete) return;

    try {
      await API.delete(`/${id}`);
      const result = hasFilters
        ? await searchEvidence(debouncedFilters, page, 5)
        : await getAllEvidence(page, 5);
      setData(result.content || []);
      setTotalPages(result.totalPages || 0);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="rounded-lg bg-white p-4 shadow-md sm:p-6">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Evidence List</h1>
        <button
          onClick={downloadCSV}
          className="min-h-11 rounded bg-green-600 px-4 py-2 text-white hover:bg-green-700"
        >
          Export CSV
        </button>
      </div>

      <div className="mb-5 grid gap-3 md:grid-cols-5">
        <input
          type="text"
          placeholder="Search name, type, status, case, tags"
          value={filters.q}
          onChange={(event) => updateFilter("q", event.target.value)}
          className="min-h-11 rounded border p-2 focus:ring-2 focus:ring-indigo-400 md:col-span-2"
        />

        <select
          value={filters.type}
          onChange={(event) => updateFilter("type", event.target.value)}
          className="min-h-11 rounded border p-2"
        >
          <option value="">All types</option>
          <option value="Physical">Physical</option>
          <option value="Digital">Digital</option>
          <option value="Document">Document</option>
        </select>

        <select
          value={filters.status}
          onChange={(event) => updateFilter("status", event.target.value)}
          className="min-h-11 rounded border p-2"
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="COMPLETED">Completed</option>
        </select>

        <button
          onClick={() => setFilters(initialFilters)}
          className="min-h-11 rounded bg-gray-200 px-3 py-2 hover:bg-gray-300"
        >
          Clear
        </button>

        <input
          type="date"
          value={filters.fromDate}
          onChange={(event) => updateFilter("fromDate", event.target.value)}
          className="min-h-11 rounded border p-2"
        />
        <input
          type="date"
          value={filters.toDate}
          onChange={(event) => updateFilter("toDate", event.target.value)}
          className="min-h-11 rounded border p-2"
        />
      </div>

      {loading ? (
        <div className="p-5 text-lg">Loading...</div>
      ) : data.length === 0 ? (
        <div className="rounded border border-dashed p-8 text-center text-gray-600">
          No evidence records match the current filters.
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[900px] text-left">
            <thead className="bg-gray-100 text-gray-700">
              <tr>
                <th className="p-3">S.No</th>
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
              {data.map((item, index) => (
                <tr key={item.id} className="border-t hover:bg-gray-50">
                  <td className="p-3">{page * 5 + index + 1}</td>
                  <td className="p-3 font-medium">{item.name}</td>
                  <td className="p-3">{displayValue(item, ["type", "evidenceType"])}</td>
                  <td className="p-3">{displayValue(item, ["status"])}</td>
                  <td className="p-3">{displayValue(item, ["priority", "Priority"])}</td>
                  <td className="p-3">{displayValue(item, ["assignedTo", "assigned_to", "assigned"])}</td>
                  <td className="p-3">{displayValue(item, ["dateCollected", "date_collected", "collectedDate"])}</td>
                  <td className="p-3">{displayValue(item, ["deadline", "deadLine", "dueDate", "due_date"])}</td>
                  <td className="flex gap-3 p-3">
                    <button
                      onClick={() => {
                        setSelectedItem(item);
                        setAppPage("edit");
                      }}
                      className="min-h-11 rounded bg-amber-400 px-3 py-1 text-black hover:bg-amber-500"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(item.id)}
                      className="min-h-11 rounded bg-red-500 px-3 py-1 text-white hover:bg-red-600"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-6 flex items-center justify-between gap-3">
        <button
          onClick={() => setPage((current) => Math.max(current - 1, 0))}
          disabled={page === 0}
          className="min-h-11 rounded bg-gray-300 px-4 py-2 disabled:opacity-50"
        >
          Previous
        </button>

        <span className="text-sm font-medium">
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
        </span>

        <button
          onClick={() => setPage((current) => current + 1)}
          disabled={totalPages === 0 || page >= totalPages - 1}
          className="min-h-11 rounded bg-gray-300 px-4 py-2 disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}

export default ListPage;
