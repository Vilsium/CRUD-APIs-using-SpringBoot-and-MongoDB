# Tournament Data API  
  
A RESTful CRUD API built with **Spring Boot** and **MongoDB** for managing cricket tournament data including teams and players.  
  
---  

  
## 🛠 Technologies Used  
  
- **Java 17+**  
- **Spring Boot 3.x**  
- **Spring Data MongoDB**  
- **MongoDB**  
- **Maven**  
  
---  
  
## 📡 API Endpoints  
  
### Teams  
  
| Method | Endpoint | Description |  
|--------|----------|-------------|  
| `GET` | `/api/teams` | Get all teams |  
| `GET` | `/api/teams/{id}` | Get a particular team by ID |  
| `GET` | `/api/teams/details/{id}` | Get team details with squad (player names & roles) |  
| `POST` | `/api/teams` | Create a new team |  
| `PUT` | `/api/teams/update/{id}` | Full update of a team |  
| `PATCH` | `/api/teams/update/{id}` | Partial update of a team |  
| `DELETE` | `/api/teams/{id}` | Delete a team |  
  
### Players  
  
| Method | Endpoint | Description |  
|--------|----------|-------------|  
| `GET` | `/api/players` | Get all players |  
| `GET` | `/api/players/{id}` | Get a particular player by ID |  
| `POST` | `/api/players` | Create a new player |  
| `PUT` | `/api/players/update/{id}` | Full update of a player |  
| `PATCH` | `/api/players/update/{id}` | Partial update of a player |  
| `DELETE` | `/api/players/{id}` | Delete a player |  
  
---  
  
## 📝 Request & Response Examples  
  
### Teams  
  
#### 1. Get All Teams  
  
```  
GET /api/teams  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Teams retrieved successfully",  
    "data": [  
        {  
            "id": "1",  
            "teamName": "Chennai Super Kings",  
            "homeGround": "M. A. Chidambaram Stadium",  
            "captainId": "1",  
            "coach": "Stephen Fleming",  
            "playerIds": ["1", "2", "3", "4", "5"]  
        }  
    ]  
}  
```  
  
---  
  
#### 2. Get Team by ID  
  
```  
GET /api/teams/{id}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team retrieved successfully",  
    "data": {  
        "id": "1",  
        "teamName": "Chennai Super Kings",  
        "homeGround": "M. A. Chidambaram Stadium",  
        "captainId": "1",  
        "coach": "Stephen Fleming",  
        "playerIds": ["1", "2", "3", "4", "5"]  
    }  
}  
```  
  
---  
  
#### 3. Get Team Details with Squad  
  
```  
GET /api/teams/details/{id}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team details retrieved successfully",  
    "data": {  
        "id": "1",  
        "teamName": "Chennai Super Kings",  
        "homeGround": "M. A. Chidambaram Stadium",  
        "captainId": "1",  
        "coach": "Stephen Fleming",  
        "squad": [  
            {  
                "id": "1",  
                "name": "MS Dhoni",  
                "role": "Wicket Keeper"  
            },  
            {  
                "id": "2",  
                "name": "Ravindra Jadeja",  
                "role": "All Rounder"  
            }  
        ]  
    }  
}  
```  
  
---  
  
#### 4. Create Team  
  
```  
POST /api/teams  
```  
  
**Request Body:**  
```json  
{  
    "teamName": "Chennai Super Kings",  
    "homeGround": "M. A. Chidambaram Stadium",  
    "captainId": "1",  
    "coach": "Stephen Fleming",  
    "playerIds": ["1", "2", "3", "4", "5"]  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team created successfully",  
    "data": {  
        "id": "1",  
        "teamName": "Chennai Super Kings",  
        "homeGround": "M. A. Chidambaram Stadium",  
        "captainId": "1",  
        "coach": "Stephen Fleming",  
        "playerIds": ["1", "2", "3", "4", "5"]  
    }  
}  
```  
  
---  
  
#### 5. Full Update Team (PUT)  
  
```  
PUT /api/teams/update/{id}  
```  
  
**Request Body:** (All fields required)  
```json  
{  
    "teamName": "Chennai Super Kings",  
    "homeGround": "M. A. Chidambaram Stadium",  
    "captainId": "2",  
    "coach": "New Coach",  
    "playerIds": ["1", "2", "3", "4", "5"]  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team updated successfully",  
    "data": {  
        "id": "1",  
        "teamName": "Chennai Super Kings",  
        "homeGround": "M. A. Chidambaram Stadium",  
        "captainId": "2",  
        "coach": "New Coach",  
        "playerIds": ["1", "2", "3", "4", "5"]  
    }  
}  
```  
  
---  
  
#### 6. Partial Update Team (PATCH)  
  
```  
PATCH /api/teams/update/{id}  
```  
  
**Request Body:** (Only fields to update)  
```json  
{  
    "coach": "New Coach Name"  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team partially updated successfully",  
    "data": {  
        "id": "1",  
        "teamName": "Chennai Super Kings",  
        "homeGround": "M. A. Chidambaram Stadium",  
        "captainId": "1",  
        "coach": "New Coach Name",  
        "playerIds": ["1", "2", "3", "4", "5"]  
    }  
}  
```  
  
---  
  
#### 7. Delete Team  
  
```  
DELETE /api/teams/{id}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Team deleted successfully",  
    "data": "deleted team documnet"  
}  
```  
  
---  
  
### Players  
  
#### 1. Get All Players  
  
```  
GET /api/players  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Players retrieved successfully",  
    "data": [  
        {  
            "id": "1",  
            "name": "MS Dhoni",  
            "role": "Wicket Keeper",  
            "battingStyle": "Right Hand Batsman",  
            "bowlingStyle": null,  
            "teamId": "1",  
            "stats": {  
                "matchesPlayed": 250,  
                "runsScored": 5000,  
                "wicketsTaken": 0,  
                "catchesTaken": 150  
            }  
        }  
    ]  
}  
```  
  
---  
  
#### 2. Get Player by ID  
  
```  
GET /api/players/{id}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Player retrieved successfully",  
    "data": {  
        "id": "1",  
        "name": "MS Dhoni",  
        "role": "Wicket Keeper",  
        "battingStyle": "Right Hand Batsman",  
        "bowlingStyle": null,  
        "teamId": "1",  
        "stats": {  
            "matchesPlayed": 250,  
            "runsScored": 5000,  
            "wicketsTaken": 0,  
            "catchesTaken": 150  
        }  
    }  
}  
```  
  
---  
  
#### 3. Create Player  
  
```  
POST /api/players  
```  
  
**Request Body:**  
```json  
{  
    "name": "MS Dhoni",  
    "role": "Wicket Keeper",  
    "battingStyle": "Right Hand Batsman",  
    "bowlingStyle": null,  
    "teamId": "1",  
    "stats": {  
        "matchesPlayed": 250,  
        "runsScored": 5000,  
        "wicketsTaken": 0,  
        "catchesTaken": 150  
    }  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Player created successfully",  
    "data": {  
        "id": "21",  
        "name": "MS Dhoni",  
        "role": "Wicket Keeper",  
        "battingStyle": "Right Hand Batsman",  
        "bowlingStyle": null,  
        "teamId": "1",  
        "stats": {  
            "matchesPlayed": 250,  
            "runsScored": 5000,  
            "wicketsTaken": 0,  
            "catchesTaken": 150  
        }  
    }  
}  
```  
  
---  
  
#### 4. Full Update Player (PUT)  
  
```  
PUT /api/players/update/{id}  
```  
  
**Request Body:** (All fields required)  
```json  
{  
    "name": "MS Dhoni",  
    "role": "Batsman",  
    "battingStyle": "Right Hand Batsman",  
    "bowlingStyle": null,  
    "teamId": "1",  
    "stats": {  
        "matchesPlayed": 260,  
        "runsScored": 5500,  
        "wicketsTaken": 0,  
        "catchesTaken": 160  
    }  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Player updated successfully",  
    "data": {  
        "id": "1",  
        "name": "MS Dhoni",  
        "role": "Batsman",  
        "battingStyle": "Right Hand Batsman",  
        "bowlingStyle": null,  
        "teamId": "1",  
        "stats": {  
            "matchesPlayed": 260,  
            "runsScored": 5500,  
            "wicketsTaken": 0,  
            "catchesTaken": 160  
        }  
    }  
}  
```  
  
---  
  
#### 5. Partial Update Player (PATCH)  
  
```  
PATCH /api/players/update/{id}  
```  
  
**Request Body:** (Only fields to update)  
```json  
{  
    "role": "All Rounder"  
}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Player partially updated successfully",  
    "data": {  
        "id": "1",  
        "name": "MS Dhoni",  
        "role": "All Rounder",  
        "battingStyle": "Right Hand Batsman",  
        "bowlingStyle": null,  
        "teamId": "1",  
        "stats": {  
            "matchesPlayed": 250,  
            "runsScored": 5000,  
            "wicketsTaken": 0,  
            "catchesTaken": 150  
        }  
    }  
}  
```  
  
---  
  
#### 6. Delete Player  
  
```  
DELETE /api/players/{id}  
```  
  
**Response:**  
```json  
{  
    "success": true,  
    "message": "Player deleted successfully",  
    "data": "deleted player document"  
}  
```  
  
---  
  
## 📊 Data Models  
  
### Team  
  
| Field | Type | Description |  
|-------|------|-------------|  
| `id` | String | Unique identifier |  
| `teamName` | String | Name of the team |  
| `homeGround` | String | Home stadium |  
| `captainId` | String | ID of the captain (references Player) |  
| `coach` | String | Name of the coach |  
| `playerIds` | Array[String] | List of player IDs in the team |  
  
### Player  
  
| Field | Type | Description |  
|-------|------|-------------|  
| `id` | String | Unique identifier |  
| `name` | String | Name of the player |  
| `role` | String | Player role (Batsman, Bowler, All Rounder, Wicket Keeper) |  
| `battingStyle` | String | Batting style |  
| `bowlingStyle` | String | Bowling style |  
| `teamId` | String | ID of the team (references Team) |  
| `stats` | Object | Player statistics |  
  
### Stats (Embedded in Player)  
  
| Field | Type | Description |  
|-------|------|-------------|  
| `matchesPlayed` | Integer | Total matches played |  
| `runsScored` | Integer | Total runs scored |  
| `wicketsTaken` | Integer | Total wickets taken |  
| `catchesTaken` | Integer | Total catches taken |  
  
---  
  
## 📁 Project Structure  
  
```  
src/main/java/com/example/tournament_data/  
├── TournamentDataApplication.java  
├── model/  
│   ├── Player.java  
│   ├── Team.java  
│   └── Stats.java  
├── repository/  
│   ├── PlayerRepository.java  
│   └── TeamRepository.java  
├── service/  
│   ├── PlayerService.java  
│   └── TeamService.java  
├── controller/  
│   ├── PlayerController.java  
│   └── TeamController.java  
└── dto/  
    ├── ApiResponse.java  
    └── TeamDetailsResponse.java  
```  
  
---  
  
## 👤 Author  
  
Vyom Singhal
  
---  
