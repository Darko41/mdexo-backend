import { useState, useEffect } from "react";

export default function RealEstateSlider() {
  const [realEstates, setRealEstates] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch data when the component mounts
  useEffect(() => {
    fetch("http://localhost:8080/api/real-estates/")  // Replace with your actual API endpoint
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok " + response.statusText);
        }
        return response.json();
      })
      .then((data) => {
        setRealEstates(data.content); // Adjust to match your backend response (e.g., data.content)
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching real estate data:", error);
        setLoading(false);
      });
  }, []);

  // Loading state
  if (loading) {
    return <div>Loading...</div>;
  }

  // Function to format the date as DD/MM/YYYY
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  };

  return (
    <div className="real-estates-container p-4">
      <h2 className="text-center text-2xl font-bold mb-4">Available Real Estates</h2>
      
      <div className="grid grid-cols-3 gap-4">
        {realEstates.length > 0 ? (
          realEstates.map((estate) => (
            <div key={estate.id} className="real-estate-item border p-4 rounded shadow">
              <h3 className="font-bold">{estate.title}</h3>
              <p>{estate.description}</p>
              <p><strong>Price:</strong> ${estate.price}</p>
              <p><strong>Location:</strong> {estate.city}, {estate.state}</p>
              <p><strong>Size:</strong> {estate.sizeInSqMt} sq meters</p>
              <p><strong>Created At:</strong> {formatDate(estate.createdAt)}</p>
            </div>
          ))
        ) : (
          <p>No real estate listings available.</p>
        )}
      </div>
    </div>
  );
}
