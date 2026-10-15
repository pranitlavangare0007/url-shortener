import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  
});

api.interceptors.response.use(
  res => res,
  err => {
    
    const msg = err?.response?.data?.message;


    if (msg) {
      alert(msg);
    } else {
      alert("Server not reachable");
    }

    return Promise.reject(err);
  }
);

export default api;
