import { BrowserRouter, Routes, Route } from "react-router-dom";
import UrlShortner from "./components/UrlShortner";
function App() {
 

  return (
    <BrowserRouter>

      <Routes>


        <Route path="/" element={<UrlShortner />} />
       


       
      </Routes>

    </BrowserRouter>
  )
}

export default App
