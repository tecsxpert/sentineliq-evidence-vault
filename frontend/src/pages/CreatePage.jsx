import { useEffect, useState } from "react";
import API from "../services/api";

function CreatePage({ selectedItem, refreshList, goToList }) {
  const [formData, setFormData] = useState({
    name: "",
    type: "",
    status: "",
    priority: "",
    caseNumber: "",
    caseName: "",
    department: "",
    assignedTo: "",
    dateCollected: "",
    deadline: "",
    location: "",
    source: "",
    description: "",
    tags: "",
  });
  const [file, setFile] = useState(null);

  useEffect(() => {
    if (selectedItem) {
      setFormData({
        ...selectedItem,
        dateCollected: selectedItem.dateCollected || "",
        deadline: selectedItem.deadline || "",
      });
    }
  }, [selectedItem]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (selectedItem) {
        await API.put(`/${selectedItem.id}`, formData);
        alert("Updated successfully!");
      } else if (file) {
        const uploadData = new FormData();
        uploadData.append("file", file);
        Object.entries(formData).forEach(([key, value]) => {
          if (value !== undefined && value !== null && String(value).trim() !== "") {
            uploadData.append(key, value);
          }
        });
        await API.post("/upload", uploadData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        alert("Created successfully!");
      } else {
        await API.post("/create", formData);
        alert("Created successfully!");
      }

      refreshList();
      goToList();
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Error saving data");
    }
  };

  return (
    <div className="max-w-3xl rounded-lg bg-white p-4 shadow-md sm:p-6">
      <h1 className="mb-4 text-2xl font-bold text-gray-800">
        {selectedItem ? "Edit Evidence" : "Create Evidence"}
      </h1>

      <form onSubmit={handleSubmit} className="grid gap-5 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium">Evidence Name *</label>
          <input type="text" name="name" value={formData.name || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2 focus:ring-2 focus:ring-indigo-400" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Evidence Type *</label>
          <select name="type" value={formData.type || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2">
            <option value="">Select Type</option>
            <option>Physical</option>
            <option>Digital</option>
            <option>Document</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Status *</label>
          <select name="status" value={formData.status || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2">
            <option value="">Select Status</option>
            <option>ACTIVE</option>
            <option>PENDING</option>
            <option>COMPLETED</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Priority *</label>
          <select name="priority" value={formData.priority || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2">
            <option value="">Select Priority</option>
            <option>HIGH</option>
            <option>MEDIUM</option>
            <option>LOW</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Case Number</label>
          <input type="text" name="caseNumber" value={formData.caseNumber || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Case Name</label>
          <input type="text" name="caseName" value={formData.caseName || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Department</label>
          <select name="department" value={formData.department || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2">
            <option value="">Select Department</option>
            <option>Forensics</option>
            <option>Cyber Crime</option>
            <option>Police</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Assigned To</label>
          <input type="text" name="assignedTo" value={formData.assignedTo || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Date Collected *</label>
          <input type="date" name="dateCollected" value={formData.dateCollected || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Deadline</label>
          <input type="date" name="deadline" value={formData.deadline || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Location</label>
          <input type="text" name="location" value={formData.location || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Source</label>
          <input type="text" name="source" value={formData.source || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium">Description *</label>
          <textarea name="description" value={formData.description || ""} onChange={handleChange} className="min-h-24 w-full rounded border p-2" />
        </div>

        {!selectedItem && (
          <div className="sm:col-span-2">
            <label className="mb-1 block text-sm font-medium">Evidence File</label>
            <input type="file" accept=".pdf,.png,.jpg,.jpeg,.txt,.csv" onChange={(event) => setFile(event.target.files?.[0] || null)} className="w-full rounded border p-2" />
          </div>
        )}

        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium">Tags</label>
          <input type="text" name="tags" value={formData.tags || ""} onChange={handleChange} className="min-h-11 w-full rounded border p-2" />
        </div>

        <button className="mt-2 min-h-11 rounded bg-indigo-500 p-2 text-white hover:bg-indigo-600 sm:col-span-2">
          {selectedItem ? "Update" : "Submit"}
        </button>
      </form>
    </div>
  );
}

export default CreatePage;
