import { useState, useEffect } from "react";
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

  useEffect(() => {
    if (selectedItem) {
      setFormData(selectedItem);
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
      } else {
        await API.post("/create", formData);
        alert("Created successfully!");
      }

      refreshList();
      goToList();
    } catch (error) {
      console.error(error);
      alert("Error saving data");
    }
  };

  return (
    <div className="bg-white shadow-md rounded-lg p-6 max-w-3xl">
      <h1 className="text-2xl font-bold mb-4 text-gray-800">
        {selectedItem ? "Edit Evidence" : "Create Evidence"}
      </h1>

     <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-5">

       {/* NAME */}
       <div className="col-span-2">
         <label className="block text-sm font-medium mb-1">Evidence Name *</label>
         <input
           type="text"
           name="name"
           value={formData.name}
           onChange={handleChange}
           className="w-full border p-2 rounded focus:ring-2 focus:ring-indigo-400"
         />
       </div>

       {/* TYPE */}
       <div>
         <label className="block text-sm font-medium mb-1">Evidence Type *</label>
         <select name="type" value={formData.type} onChange={handleChange} className="w-full border p-2 rounded">
           <option value="">Select Type</option>
           <option>Physical</option>
           <option>Digital</option>
           <option>Document</option>
         </select>
       </div>

       {/* STATUS */}
       <div>
         <label className="block text-sm font-medium mb-1">Status *</label>
         <select name="status" value={formData.status} onChange={handleChange} className="w-full border p-2 rounded">
           <option value="">Select Status</option>
           <option>ACTIVE</option>
           <option>PENDING</option>
           <option>COMPLETED</option>
         </select>
       </div>

       {/* PRIORITY */}
       <div>
         <label className="block text-sm font-medium mb-1">Priority *</label>
         <select name="priority" value={formData.priority} onChange={handleChange} className="w-full border p-2 rounded">
           <option value="">Select Priority</option>
           <option>HIGH</option>
           <option>MEDIUM</option>
           <option>LOW</option>
         </select>
       </div>

       {/* CASE NUMBER */}
       <div>
         <label className="block text-sm font-medium mb-1">Case Number</label>
         <input type="text" name="caseNumber" value={formData.caseNumber} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* CASE NAME */}
       <div>
         <label className="block text-sm font-medium mb-1">Case Name</label>
         <input type="text" name="caseName" value={formData.caseName} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* DEPARTMENT */}
       <div>
         <label className="block text-sm font-medium mb-1">Department</label>
         <select name="department" value={formData.department} onChange={handleChange} className="w-full border p-2 rounded">
           <option value="">Select Department</option>
           <option>Forensics</option>
           <option>Cyber Crime</option>
           <option>Police</option>
         </select>
       </div>

       {/* ASSIGNED TO */}
       <div>
         <label className="block text-sm font-medium mb-1">Assigned To</label>
         <input type="text" name="assignedTo" value={formData.assignedTo} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* DATE COLLECTED */}
       <div>
         <label className="block text-sm font-medium mb-1">Date Collected *</label>
         <input type="date" name="dateCollected" value={formData.dateCollected} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* DEADLINE */}
       <div>
         <label className="block text-sm font-medium mb-1">Deadline</label>
         <input type="date" name="deadline" value={formData.deadline} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* LOCATION */}
       <div>
         <label className="block text-sm font-medium mb-1">Location</label>
         <input type="text" name="location" value={formData.location} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* SOURCE */}
       <div>
         <label className="block text-sm font-medium mb-1">Source</label>
         <input type="text" name="source" value={formData.source} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* DESCRIPTION */}
       <div className="col-span-2">
         <label className="block text-sm font-medium mb-1">Description *</label>
         <textarea name="description" value={formData.description} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* TAGS */}
       <div className="col-span-2">
         <label className="block text-sm font-medium mb-1">Tags</label>
         <input type="text" name="tags" value={formData.tags} onChange={handleChange} className="w-full border p-2 rounded"/>
       </div>

       {/* BUTTON */}
       <button className="bg-indigo-500 hover:bg-indigo-600 text-white p-2 rounded col-span-2 mt-2">
         {selectedItem ? "Update" : "Submit"}
       </button>

     </form>
    </div>
  );
}

export default CreatePage;