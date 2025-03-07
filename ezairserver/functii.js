export { initializareBaza, editareUser, verificareDateLogin, verificareArray, eroare };
import { JsonDB, Config } from "node-json-db";

//initializarea bazei de date json
async function initializareBaza() {
    const db = new JsonDB(new Config("baza", true, true, "/"));
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



//verificarea datelor pentru loigin
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


//verificarea matritei(nu merge fara)
async function verificareArray(ar) {
    if (!Array.isArray(ar)) {
        ar = [];
    }
    return ar;
}



function eroare(error, res) {
    console.error("Eroare:", error);
    if (error.name === 'NotFoundError') {
        res.status(404).json({ error: "Resursa nu a fost găsită" });
    } else if (error.name === 'ValidationError') {
        res.status(400).json({ error: "Date de validare incorecte" });
    } else {
        res.status(500).json({ error: "Eroare internă" });
    }
}