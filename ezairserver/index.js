import express from 'express'; //pentru REST API
import { JsonDB, Config } from "node-json-db";//baza de date json TEMP
import { v4 as uuidv4 } from "uuid"; //generare id unic

const app = express();
const port = 3000;

const db = new JsonDB(new Config("baza", true, true, "/"));

// Middleware to parse JSON request bodies
app.use(express.json());

class user {
    constructor(username, parola, email, nume, prenume, dataNasterii, admin) {
        this.id = uuidv4();
        this.username = username;
        this.parola = parola;
        this.email = email;
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.admin = admin;
    }
}

app.get('/', (req, res) => {
    res.send('Hello World!');
}
);

app.get('/users/populareLista', async (req, res) => {
    try {
        const users = await db.getData("/users");
        res.status(200).json(users);
    } catch (error) {
        console.error("Eroare la obținerea utilizatorilor:", error);
        res.status(500).send("Eroare la obținerea utilizatorilor");
    }
});

//metoda pentru logare
app.post('/users/login', async (req, res) => {
    try {
        const { username, parola } = req.body;
        const user = await verificareDateLogin(username, parola);
        res.status(200).json(user); // Trimite răspuns JSON
    } catch (error) {
        console.error("Eroare la verificarea datelor de login:", error);
        res.status(401).json({ error: "Invalid credentials" }); // Trimite eroare JSON
    }
});

//metoda pentru creare cont
app.post('/users/creareCont', async (req, res) => {
    try {
        const { username, parola, email, nume, prenume, dataNasterii, admin} = req.body;
        const userNou = new user(username, parola, email, nume, prenume, dataNasterii, admin);
        await creareContUserNou(userNou);
        res.status(200).send('User created successfully');
    } catch (error){
        console.error("Eroare la autentificare.", error);
        res.status(500).send('Eroare la creearea userului');
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
        const index = users.findIndex(users => users.id === id);
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
async function verificareDateLogin(username, parola) {
    try {
        let users = await db.getData("/users");
        const user = users.find(users => users.username === username && users.parola === parola);
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

//verificarea matritei(nu merge fara uneori ns)
async function verificareArray(ar) {
    if (!Array.isArray(ar)) {
        ar = [];
    }
    return ar;
}

//adaugarea userului in baza de date
async function creareContUserNou(user) {
    try {
        let users = await db.getData("/users");
        await verificareArray(users);
        users.push(user);
        db.push("/users", users, true);
    } catch (error) {
        console.error("Eroaore la salvarea datelor userului. ", error);
        throw error;
    }
}
