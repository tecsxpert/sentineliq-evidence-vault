import { useEffect, useMemo, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import API from "../services/api";

const COLORS = ["#1B4F8A", "#16a34a", "#f59e0b", "#dc2626", "#7c3aed"];

function toChartRows(map = {}) {
  return Object.entries(map).map(([name, value]) => ({ name, value }));
}

function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [period, setPeriod] = useState("all");

  useEffect(() => {
    API.get("/analytics").then((res) => setAnalytics(res.data));
  }, []);

  const statusRows = useMemo(() => toChartRows(analytics?.status), [analytics]);
  const typeRows = useMemo(() => toChartRows(analytics?.type), [analytics]);
  const priorityRows = useMemo(() => toChartRows(analytics?.priority), [analytics]);

  if (!analytics) {
    return <div className="p-5 text-lg">Loading analytics...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
        <select
          value={period}
          onChange={(event) => setPeriod(event.target.value)}
          className="min-h-11 rounded border px-3"
        >
          <option value="all">All records</option>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
        </select>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {Object.entries(analytics.dashboard || {}).map(([key, value]) => (
          <div key={key} className="rounded-lg bg-white p-4 shadow">
            <p className="text-sm capitalize text-gray-500">{key}</p>
            <p className="text-3xl font-bold text-[#1B4F8A]">{value}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-lg bg-white p-4 shadow">
          <h2 className="mb-4 font-semibold">Status Distribution</h2>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={statusRows} dataKey="value" nameKey="name" outerRadius={95} label>
                {statusRows.map((entry, index) => (
                  <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-lg bg-white p-4 shadow">
          <h2 className="mb-4 font-semibold">Evidence by Type</h2>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={typeRows}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" fill="#1B4F8A" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-lg bg-white p-4 shadow lg:col-span-2">
          <h2 className="mb-4 font-semibold">Priority Mix</h2>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={priorityRows}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" fill="#16a34a" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}

export default Analytics;
