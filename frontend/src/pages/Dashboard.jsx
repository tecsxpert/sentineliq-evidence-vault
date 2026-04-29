import { useEffect, useState } from "react";
import API from "../services/api";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

function Dashboard() {
  const [data, setData] = useState({});

  useEffect(() => {
    const fetchDashboard = async () => {
      const res = await API.get("/dashboard");
      setData(res.data);
    };

    fetchDashboard();
  }, []);

  // 🔥 chart data
  const chartData = [
    { name: "Active", value: data.active || 0 },
    { name: "Pending", value: data.pending || 0 },
    { name: "Completed", value: data.completed || 0 },
  ];

  const COLORS = ["#22c55e", "#eab308", "#3b82f6"];

  return (
    <div className="p-6 max-w-5xl mx-auto">

      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>

      {/* KPI CARDS */}
      <div className="grid grid-cols-4 gap-4 mb-6">

        <div className="bg-white shadow p-4 rounded text-center">
          <p className="text-gray-500">Total</p>
          <h2 className="text-2xl font-bold">{data.total || 0}</h2>
        </div>

        <div className="bg-green-100 shadow p-4 rounded text-center">
          <p className="text-gray-600">Active</p>
          <h2 className="text-2xl font-bold">{data.active || 0}</h2>
        </div>

        <div className="bg-yellow-100 shadow p-4 rounded text-center">
          <p className="text-gray-600">Pending</p>
          <h2 className="text-2xl font-bold">{data.pending || 0}</h2>
        </div>

        <div className="bg-blue-100 shadow p-4 rounded text-center">
          <p className="text-gray-600">Completed</p>
          <h2 className="text-2xl font-bold">{data.completed || 0}</h2>
        </div>

      </div>

      {/* 🔥 PIE CHART */}
      <div className="bg-white shadow rounded p-4">
        <h2 className="text-lg font-semibold mb-4 text-center">
          Evidence Status Distribution
        </h2>

        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={chartData}
              dataKey="value"
              nameKey="name"
              outerRadius={100}
              label
            >
              {chartData.map((entry, index) => (
                <Cell key={index} fill={COLORS[index]} />
              ))}
            </Pie>

            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>

    </div>
  );
}

export default Dashboard;