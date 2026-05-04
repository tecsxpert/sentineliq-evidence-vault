import API from "./api";

export const getAllEvidence = async (page, size) => {
  const response = await API.get(`/all?page=${page}&size=${size}`);
  return response.data;
};

export const searchEvidence = async (filters, page, size) => {
  const params = new URLSearchParams({
    page,
    size,
    q: filters.q || "",
  });

  if (filters.type) params.append("type", filters.type);
  if (filters.status) params.append("status", filters.status);
  if (filters.fromDate) params.append("fromDate", filters.fromDate);
  if (filters.toDate) params.append("toDate", filters.toDate);

  const response = await API.get(`/search?${params.toString()}`);
  return response.data;
};
