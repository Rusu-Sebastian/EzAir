import { JsonDB, Config } from "node-json-db";
import { v4 as uuidv4 } from "uuid";

// Initialize the database connection
const db = new JsonDB(new Config("baza", true, true, "/"));

// Flight class (same as in index.js)
class zbor {
    constructor(origine, destinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, modelAvion, locuriLibere, pret) {
        this.id = uuidv4();
        this.origine = origine;
        this.destinatie = destinatie;
        this.dataPlecare = dataPlecare;
        this.oraPlecare = oraPlecare;
        this.dataSosire = dataSosire;
        this.oraSosire = oraSosire;
        this.modelAvion = modelAvion;
        this.locuriLibere = locuriLibere;
        this.pret = pret;
    }
}

// Function to add a flight to the database
async function addFlight(flight) {
    try {
        let zboruri = await db.getData("/zboruri");
        zboruri.push(flight);
        db.push("/zboruri", zboruri, true);
        console.log(`Flight added: ${flight.origine} -> ${flight.destinatie} on ${flight.dataPlecare} at ${flight.oraPlecare}`);
    } catch (error) {
        console.error("Error adding flight:", error);
    }
}

// List of flights to add (realistic routes with appropriate data)
const flightsToAdd = [
    // European flights
    new zbor("Bucuresti", "Paris", "15/7/2024", "08:45", "15/7/2024", "11:15", "Boeing 737-800", 180, 129.99),
    new zbor("Paris", "Bucuresti", "15/7/2024", "13:30", "15/7/2024", "16:00", "Boeing 737-800", 180, 134.99),
    new zbor("Bucuresti", "Madrid", "16/7/2024", "09:15", "16/7/2024", "12:00", "Airbus A320", 150, 159.99),
    new zbor("Madrid", "Bucuresti", "16/7/2024", "14:30", "16/7/2024", "17:15", "Airbus A320", 150, 164.99),
    new zbor("Bucuresti", "Rome", "17/7/2024", "10:00", "17/7/2024", "11:45", "Boeing 737-700", 140, 119.99),
    new zbor("Rome", "Bucuresti", "17/7/2024", "13:45", "17/7/2024", "15:30", "Boeing 737-700", 140, 124.99),
    
    // Domestic flights
    new zbor("Bucuresti", "Cluj-Napoca", "18/7/2024", "07:30", "18/7/2024", "08:40", "ATR 72-600", 70, 79.99),
    new zbor("Cluj-Napoca", "Bucuresti", "18/7/2024", "10:00", "18/7/2024", "11:10", "ATR 72-600", 70, 79.99),
    new zbor("Bucuresti", "Timisoara", "19/7/2024", "08:15", "19/7/2024", "09:30", "ATR 72-600", 70, 69.99),
    new zbor("Timisoara", "Bucuresti", "19/7/2024", "11:00", "19/7/2024", "12:15", "ATR 72-600", 70, 69.99),
    new zbor("Bucuresti", "Iasi", "20/7/2024", "09:30", "20/7/2024", "10:45", "Boeing 737-700", 140, 59.99),
    new zbor("Iasi", "Bucuresti", "20/7/2024", "12:30", "20/7/2024", "13:45", "Boeing 737-700", 140, 59.99),
    
    // Long-haul flights
    new zbor("Bucuresti", "New York", "21/7/2024", "10:30", "21/7/2024", "14:45", "Boeing 777-300ER", 300, 499.99),
    new zbor("New York", "Bucuresti", "22/7/2024", "16:30", "23/7/2024", "08:45", "Boeing 777-300ER", 300, 529.99),
    new zbor("Bucuresti", "Dubai", "23/7/2024", "22:15", "24/7/2024", "04:30", "Airbus A330", 250, 329.99),
    new zbor("Dubai", "Bucuresti", "24/7/2024", "06:45", "24/7/2024", "11:00", "Airbus A330", 250, 349.99),
    
    // Additional European routes
    new zbor("Bucuresti", "Amsterdam", "25/7/2024", "07:00", "25/7/2024", "09:15", "Airbus A320", 150, 149.99),
    new zbor("Amsterdam", "Bucuresti", "25/7/2024", "11:30", "25/7/2024", "13:45", "Airbus A320", 150, 154.99),
    new zbor("Bucuresti", "Barcelona", "26/7/2024", "06:45", "26/7/2024", "09:00", "Boeing 737-800", 180, 139.99),
    new zbor("Barcelona", "Bucuresti", "26/7/2024", "11:15", "26/7/2024", "13:30", "Boeing 737-800", 180, 144.99),
    
    // Future dates
    new zbor("Bucuresti", "Viena", "1/8/2024", "08:00", "1/8/2024", "09:15", "Airbus A320", 150, 89.99),
    new zbor("Viena", "Bucuresti", "1/8/2024", "11:00", "1/8/2024", "12:15", "Airbus A320", 150, 94.99),
    new zbor("Bucuresti", "Atena", "2/8/2024", "09:30", "2/8/2024", "11:00", "Boeing 737-700", 140, 109.99),
    new zbor("Atena", "Bucuresti", "2/8/2024", "13:15", "2/8/2024", "14:45", "Boeing 737-700", 140, 114.99),
    
    // Holiday flights
    new zbor("Bucuresti", "Antalya", "5/8/2024", "06:30", "5/8/2024", "08:45", "Airbus A321", 200, 169.99),
    new zbor("Antalya", "Bucuresti", "5/8/2024", "10:30", "5/8/2024", "12:45", "Airbus A321", 200, 179.99),
    new zbor("Bucuresti", "Santorini", "7/8/2024", "07:15", "7/8/2024", "09:00", "Boeing 737-800", 180, 189.99),
    new zbor("Santorini", "Bucuresti", "7/8/2024", "11:00", "7/8/2024", "12:45", "Boeing 737-800", 180, 199.99)
];

// Add all flights
async function addAllFlights() {
    for (const flight of flightsToAdd) {
        await addFlight(flight);
    }
    console.log("All flights have been added successfully!");
}

// Execute the function
addAllFlights().catch(console.error);
