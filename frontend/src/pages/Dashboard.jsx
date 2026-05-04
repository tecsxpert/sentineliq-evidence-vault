import { useEffect, useState } from "react";
import API from "../services/api";
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

function Dashboard() {
  const [data, setData] = useState({});

  useEffect(() => {
    API.get("/dashboard").then((res) => setData(res.data));
  }, []);

  const chartData = [
    { name: "Active", value: data.active || 0 },
    { name: "Pending", value: data.pending || 0 },
    { name: "Completed", value: data.completed || 0 },
  ];

  const COLORS = ["#16a34a", "#eab308", "#1B4F8A"];

  return (
    <div className="mx-auto max-w-5xl">
      <h1 className="mb-6 text-2xl font-bold">Dashboard</h1>

      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded bg-white p-4 text-center shadow">
          <p className="text-gray-500">Total</p>
          <h2 className="text-2xl font-bold">{data.total || 0}</h2>
        </div>

        <div className="rounded bg-green-100 p-4 text-center shadow">
          <p className="text-gray-600">Active</p>
          <h2 className="text-2xl font-bold">{data.active || 0}</h2>
        </div>

        <div className="rounded bg-yellow-100 p-4 text-center shadow">
          <p className="text-gray-600">Pending</p>
          <h2 className="text-2xl font-bold">{data.pending || 0}</h2>
        </div>

        <div className="rounded bg-blue-100 p-4 text-center shadow">
          <p className="text-gray-600">Completed</p>
          <h2 className="text-2xl font-bold">{data.completed || 0}</h2>
        </div>
      </div>

      <div className="rounded bg-white p-4 shadow">
        <h2 className="mb-4 text-center text-lg font-semibold">
          Evidence Status Distribution
        </h2>

        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie data={chartData} dataKey="value" nameKey="name" outerRadius={100} label>
              {chartData.map((entry, index) => (
                <Cell key={entry.name} fill={COLORS[index]} />
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
