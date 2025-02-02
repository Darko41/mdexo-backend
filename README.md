# mdexo
Web presentation for home adaptation business and real estate advertisement.

ENDPOINTS

CRUD:
1. POST /api/add - add new realestate (json)
2. GET /api/real-estates - view all realestates
3. PUT /api/real-estates/{propertyId} - update realestate object
4. DELETE /api/delete/{propertyId} - delete a realestate


Authentication:
1. POST /api/authenticate – for user login (JWT token generation).
2. POST /api/users/register – for new user registration.


Listings:
1. TODO: GET /api/listings – view all listings (with optional query parameters for filters).
2. TODO: POST /api/listings – create a new listing (authenticated agent).
3. TODO: PUT /api/listings/{id} – update an existing listing (authenticated agent).
4. TODO: DELETE /api/listings/{id} – delete a listing (authenticated agent).
5. TODO: GET /api/listings/{id} – view a single listing in detail.

User Profile:
1. GET /api/users/{username} – view a user’s profile information.
2. PUT /api/users/{username} – update user profile information.
3. DELETE /api/users/{username} – delete an user
4. POS /api/users/add – add new user (json)

Favorites and Inquiries:
1. TODO: POST /api/favorites – save a listing as a favorite.
2. TODO: POST /api/inquiries – submit an inquiry for a listing.




