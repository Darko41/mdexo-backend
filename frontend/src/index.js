import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom"; // Ensure BrowserRouter is imported here
import App from "./App"; // Import App component
import "./index.css"; // Ensure you are importing the styles correctly

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <BrowserRouter> {/* Wrap App with BrowserRouter to enable routing */}
    <App />
  </BrowserRouter>
);
