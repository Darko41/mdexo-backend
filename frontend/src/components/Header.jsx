// src/components/Header.jsx
import { Link } from "react-router-dom"; // Import Link for routing

export default function Header() {
  return (
    <div>
      {/* Header Section */}
      <div className="flex h-25 w-screen flex-row justify-between bg-blue-600">
        <div className="ml-26 flex w-2/5 flex-row items-center justify-start">
          {/* Home Button */}
          <Link to="/" className="mr-6 bg-amber-300 hover:bg-blue-600 p-2 rounded">
            Real Estate Company INC
          </Link>
          
          <Link to="/real-estates">
            <button className="mr-6 bg-amber-300 hover:bg-blue-600">
              КУПОВИНА
            </button>
          </Link>
          <Link to="/iznamljivanje">
            <button className="mr-6 mr-6 bg-amber-300 hover:bg-blue-600">ИЗНАЈМЉИВАЊЕ</button>
          </Link>
          <Link to="/prodaja">
            <button className="mr-6 mr-6 bg-amber-300 hover:bg-blue-600">ПРОДАЈА</button>
          </Link>
        </div>
        <div className="logo w-2/5"></div>
        <div className="mr-26 flex w-1/5 flex-row items-center justify-end">
          <Link to="/pomoc">
            <button className="mr-6 mr-6 bg-amber-300 hover:bg-blue-600">ПОМОЋ</button>
          </Link>
          <Link to="/prijava">
            <button className="mr-6 mr-6 bg-amber-300 hover:bg-blue-600">ПРИЈАВА</button>
          </Link>
        </div>
      </div>
    </div>
  );
}
