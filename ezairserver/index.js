import express from 'express'; //pentru REST API
import { JsonDB, Config } from "node-json-db";//baza de date json TEMP
import { v4 as uuidv4 } from "uuid"; //generare id unic

const app = express();
const port = 3000;

const db = new JsonDB(new Config("baza", true, true, "/"));


//metoda pentru logare
app.get('/login', async (req, res) => {
    const { username, password } = req.query;
    try {
        const user = await verificareDateLogin(username, password);
        res.status(200).send('Login successful');
    } catch (error) {
        console.error("Eroare la verificarea datelor de login:", error);
        res.status(401).send('Invalid credentials');
    }
});

//pornirea serverului
app.listen(port, async () => {
    console.log(`Server is running on http://localhost:${port}`);
    await initializareBaza();
});

//FUNCTII

//initializarea bazei de date json
async function initializareBaza() {
    try {
        if (!await db.exists("/users")) {
            await db.push("/users", []);
        }
    } catch (error) {
        console.error("Eroare la initializarea bazei de date:", error);
        throw error;
    }
}

async function editareUser(id, dateUpdatate) {
    try {
        let users = await db.getData("/users");
        await verificareArray(users);
        const index = users.findIndex(client => client.id === id);
        if (index !== -1) {
            users[index] = { ...users[index], ...dateUpdatate };
            db.push("/users", users, true);
        } else {
            throw new Error("Userul nu a fost găsit");
        }
    } catch (error) {
        console.error("Eroare la editarea datelor userului:", error);
        throw error;
    }
}

//verificarea datelor pentru login
async function verificareDateLogin(username, password) {
    try {
        let users = await db.getData("/users");
        await verificareArray(users);
        const user = users.find(client => client.username === username && client.password === password);
        if (user) {
            return user;
        } else {
            throw new Error("Date de login incorecte");
        }
    } catch (error) {
        console.error("Eroare la verificarea datelor de login:", error);
        throw error;
    }
}
