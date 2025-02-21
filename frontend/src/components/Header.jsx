import React, { useState } from "react";

export default function Header() {
	// State to hold real estate data
	const [realEstates, setRealEstates] = useState([]);

	// Fetch real estate data when button is clicked
	const fetchRealEstates = () => {
		fetch('http://localhost:8080/api/real-estates/')  // Make sure the URL matches your backend's endpoint
			.then(response => {
				// Check if response is OK (status code 200-299)
				if (!response.ok) {
					throw new Error('Network response was not ok ' + response.statusText);
				}
				return response.json();  // Parse the response as JSON
			})
			.then(data => {
				console.log('Fetched real estate data:', data);  // Log the fetched data to verify the structure
				setRealEstates(data.content);  // Store the data in the state
			})
			.catch(error => {
				console.error('Error fetching real estate data:', error);  // Log any errors that occur
			});
	};

	return (
		<div>
			{/* Header section */}
			<div className="flex h-25 w-screen flex-row justify-between bg-blue-600">
				<div className="ml-26 flex w-2/5 flex-row items-center justify-start">
					<button className="mr-6 bg-amber-300 hover:bg-blue-600">
						КУПОВИНА
					</button>
					<button className="mr-6">ИЗНАЈМЉИВАЊЕ</button>
					<button>ПРОДАЈА</button>
				</div>
				<div className="logo w-2/5"></div>
				<div className="mr-26 flex w-1/5 flex-row items-center justify-end">
					<button className="mr-6">ПОМОЋ</button>
					<button>ПРИЈАВА</button>
				</div>
			</div>

			{/* Button to trigger fetch */}
			<button onClick={fetchRealEstates} className="p-2 bg-green-500 text-white hover:bg-green-700">
				Show Real Estates
			</button>

			{/* Render real estate data */}
			<div className="real-estates-list mt-4">
				{realEstates.length === 0 ? (
					<p>No real estates available</p>
				) : (
					<ul>
						{realEstates.map((estate, index) => (
							<li key={index} className="border p-4 mb-4">
								<h3 className="font-bold text-lg">{estate.title}</h3>
								<p>{estate.description}</p>
								<p><strong>Type:</strong> {estate.propertyType}</p>
								<p><strong>Price:</strong> ${estate.price}</p>
								<p><strong>Location:</strong> {estate.city}, {estate.state}</p>
								<p><strong>Size:</strong> {estate.sizeInSqMt} sq meters</p>
								<p><strong>Address:</strong> {estate.address}, {estate.zipCode}</p>
								<p><strong>Created At:</strong> {estate.createdAt}</p>
								<p><strong>Updated At:</strong> {estate.updatedAt}</p>
							</li>
						))}
					</ul>
				)}
			</div>
		</div>
	);
}
