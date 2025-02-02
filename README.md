# mdexo
Web presentation for home adaptation business and real estate advertisement.

ENDPOINTS

CRUD:

1. POST /api/add - add new realestate
2. GET /api/real-estates - view all realestates
3. PUT /api/real-estates/{propertyId} - update realestate object
4. DELETE /api/delete/{propertyId} - delete a realestate


Authentication:
POST /api/authenticate – for user login (JWT token generation).
POST /api/users/register – for new user registration.


Listings:
TODO: GET /api/listings – view all listings (with optional query parameters for filters).
TODO: POST /api/listings – create a new listing (authenticated agent).
TODO: PUT /api/listings/{id} – update an existing listing (authenticated agent).
TODO: DELETE /api/listings/{id} – delete a listing (authenticated agent).
TODO: GET /api/listings/{id} – view a single listing in detail.

User Profile:
TODO: GET /api/users/{username} – view a user’s profile information.
TODO: PUT /api/users/{username} – update user profile information.

Favorites and Inquiries:
TODO: POST /api/favorites – save a listing as a favorite.
TODO: POST /api/inquiries – submit an inquiry for a listing.




