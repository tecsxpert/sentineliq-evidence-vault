import API from "./api";

// GET all data
export const getAllEvidence = async (page, size) => {
  const response = await API.get(`/all?page=${page}&size=${size}`);
  return response.data;
};

export const searchEvidence = async (query, page, size) => {
  const response = await API.get(
    `/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`
  );
  return response.data;
};