import express from 'express'; //pentru REST API
import { JsonDB, Config } from "node-json-db";//baza de date json TEMP
import { v4 as uuidv4 } from "uuid"; //generare id unic

const app = express();
const port = 3000;

const db = new JsonDB(new Config("baza", true, true, "/"));

// Middleware to parse JSON request bodies
app.use(express.json());

class user {
    constructor(username, parola, email, nume, prenume, dataNasterii, admin = false, telefon){
        this.id = uuidv4();
        this.username = username;
        this.parola = parola;
        this.email = email;
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.admin = admin;
        this.setari = {
            notificariEmail: true,
            notificariSMS: false,
            notificariPushWeb: true,
            notificariPromotii: true,
            notificariAnulari: true,
            notificariModificari: true
        };
        this.telefon = telefon;
    }
}

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

// Clasa pentru bilete
class bilet {
    constructor(userId, zborId, detaliiZbor, dataZbor, pret) {
        this.id = uuidv4();
        this.userId = userId;
        this.zborId = zborId;
        this.detaliiZbor = detaliiZbor;
        this.dataZbor = dataZbor;
        this.stare = "ACTIV";
        this.pret = pret;
    }
}

app.get('/', (req, res) => {
    res.send('E bine ma merge');
}
);

// popularea listelor de clienti si zborurii
app.get('/users/populareLista', async (req, res) => {
    try {
        const users = await db.getData("/users");
        res.status(200).json(users);
    } catch (error) {
        console.error("Eroare la obținerea utilizatorilor:", error);
        res.status(500).send("Eroare la obținerea utilizatorilor");
    }
});

app.get('/zboruri/populareLista', async (req, res) => {
    try {
        const zboruri = await db.getData("/zboruri");
        res.status(200).json(zboruri);
    } catch (error) {
        console.error("Eroare la obținerea zborurilor:", error);
        res.status(500).send("Eroare la obținerea zborurilor");
    }
}
);

//metoda pentru adaugarea unui zbor
app.post('/zboruri/adaugareZbor', async (req, res) => {
    try {
        const { origine, destinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, modelAvion, locuriLibere, pret } = req.body;
        const zborNou = new zbor(origine, destinatie, dataPlecare, oraPlecare, dataSosire, oraSosire, modelAvion, locuriLibere, pret);
        let zboruri = await db.getData("/zboruri");
        zboruri.push(zborNou);
        db.push("/zboruri", zboruri, true);
        res.status(200).send('Zbor adaugat cu succes');
    } catch (error) {
        console.error("Eroare la adaugarea zborului:", error);
        res.status(500).send('Eroare la adaugarea zborului');
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

app.delete('/users/:id', async (req, res) => {
    try {
        const id = req.params.id;
        if(await stergereUser(id) === true) {
            res.status(200).send('User sters cu succes');
        }
        else {
            res.status(404).send('Userul nu a fost gasit');
        }
    } catch (error) {
        console.error("Eroare la stergerea userului:", error);
        res.status(500).send('Eroare la stergerea userului');
    }
});

app.delete('/zboruri/stergereZbor/:id', async (req, res) => {
    try {
        const id = req.params.id;
        let zboruri = await db.getData("/zboruri");
        const index = zboruri.findIndex(zbor => zbor.id === id);
        if (index !== -1) {
            zboruri.splice(index, 1);
            db.push("/zboruri", zboruri, true);
            res.status(200).send('Zbor sters cu succes');
        } else {
            res.status(404).send('Zborul nu a fost gasit');
        }
    } catch (error) {
        console.error("Eroare la stergerea zborului:", error);
        res.status(500).send('Eroare la stergerea zborului');
    }
}
);
//metoda pentru editarea userului
app.put('/users/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const dateUpdatate = req.body;
        console.log("Editare utilizator:", id, dateUpdatate);
        await editareUser(id, dateUpdatate);
        res.status(200).send('User updated successfully');
    } catch (error) {
        console.error("Eroare la editarea userului:", error);
        if (error.message === "Userul nu a fost găsit") {
            res.status(404).send('Utilizatorul nu a fost găsit');
        } else {
            res.status(500).send('Eroare la editarea userului');
        }
    }
});
//metoda pentru editarea zborului
app.put('/zboruri/editareZbor/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const dateUpdatate = req.body;
        let zboruri = await db.getData("/zboruri");
        const index = zboruri.findIndex(zbor => zbor.id === id);
        if (index !== -1) {
            zboruri[index] = { ...zboruri[index], ...dateUpdatate };
            db.push("/zboruri", zboruri, true);
            res.status(200).send('Zbor updated successfully');
        } else {
            res.status(404).send('Zborul nu a fost gasit');
        }
    } catch (error) {
        console.error("Eroare la editarea zborului:", error);
        res.status(500).send('Eroare la editarea zborului');
    }
}
);
//metoda pentru cautarea zborului
app.get('/zboruri/cautareZbor', async (req, res) => {
    try {
        const { origine, destinatie } = req.query;
        let zboruri = await db.getData("/zboruri");
        const zboruriGasite = zboruri.filter(zbor => zbor.origine === origine && zbor.destinatie === destinatie);
        if (zboruriGasite.length > 0) {
            res.status(200).json(zboruriGasite);
        } else {
            res.status(404).send('Nu au fost gasite zboruri');
        }
    } catch (error) {
        console.error("Eroare la cautarea zborului:", error);
        res.status(500).send('Eroare la cautarea zborului');
    }
}
);
//metoda pentru cautarea userului
app.get('/users/cautareUser', async (req, res) => {
    try {
        const { username } = req.query;
        let users = await db.getData("/users");
        const userGasit = users.find(user => user.username === username);
        if (userGasit) {
            res.status(200).json(userGasit);
        } else {
            res.status(404).send('Userul nu a fost gasit');
        }
    } catch (error) {
        console.error("Eroare la cautarea userului:", error);
        res.status(500).send('Eroare la cautarea userului');
    }
}
);

// Endpoint pentru obținerea unui utilizator după ID
app.get('/users/:id', async (req, res) => {
    try {
        const id = req.params.id;
        
        let users = await db.getData("/users");
        
        const user = users.find(u => u.id === id);
        
        if (user) {
            res.status(200).json(user);
        } else {
            res.status(404).send('Utilizatorul nu a fost găsit');
        }
    } catch (error) {
        console.error("Eroare la obținerea utilizatorului:", error);
        res.status(500).send('Eroare la obținerea utilizatorului');
    }
});

// Endpoint pentru a obține biletele unui utilizator
app.get('/users/:id/bilete', async (req, res) => {
    try {
        const userId = req.params.id;
        let bilete = await db.getData("/bilete") || [];
        const bileteleUtilizatorului = bilete.filter(bilet => bilet.userId === userId);
        res.status(200).json(bileteleUtilizatorului);
    } catch (error) {
        console.error("Eroare la obținerea biletelor:", error);
        res.status(500).send("Eroare la obținerea biletelor");
    }
});

// Endpoint pentru cumpărarea unui bilet
app.post('/bilete/cumpara', async (req, res) => {
    try {
        const { userId, zborId, detaliiZbor, dataZbor, pret } = req.body;
        const biletNou = new bilet(userId, zborId, detaliiZbor, dataZbor, pret);
        
        // Actualizează numărul de locuri disponibile pentru zbor
        let zboruri = await db.getData("/zboruri");
        const indexZbor = zboruri.findIndex(z => z.id === zborId);
        if (indexZbor !== -1) {
            zboruri[indexZbor].locuriLibere--;
            await db.push("/zboruri", zboruri, true);
        }

        // Salvează biletul
        let bilete = await db.getData("/bilete") || [];
        bilete.push(biletNou);
        await db.push("/bilete", bilete, true);
        
        res.status(200).json(biletNou);
    } catch (error) {
        console.error("Eroare la cumpărarea biletului:", error);
        res.status(500).send("Eroare la cumpărarea biletului");
    }
});

// Endpoint pentru anularea unui bilet
app.put('/bilete/:id/anulare', async (req, res) => {
    try {
        const biletId = req.params.id;
        let bilete = await db.getData("/bilete");
        const indexBilet = bilete.findIndex(b => b.id === biletId);
        
        if (indexBilet !== -1) {
            bilete[indexBilet].stare = "ANULAT";
            
            // Incrementează locurile disponibile pentru zbor
            let zboruri = await db.getData("/zboruri");
            const indexZbor = zboruri.findIndex(z => z.id === bilete[indexBilet].zborId);
            if (indexZbor !== -1) {
                zboruri[indexZbor].locuriLibere++;
                await db.push("/zboruri", zboruri, true);
            }
            
            await db.push("/bilete", bilete, true);
            res.status(200).send("Bilet anulat cu succes");
        } else {
            res.status(404).send("Biletul nu a fost găsit");
        }
    } catch (error) {
        console.error("Eroare la anularea biletului:", error);
        res.status(500).send("Eroare la anularea biletului");
    }
});

// Endpoint pentru modificarea datei unui bilet
app.put('/bilete/:id/modificare-data', async (req, res) => {
    try {
        const biletId = req.params.id;
        const { dataNoua } = req.body;
        
        let bilete = await db.getData("/bilete");
        const indexBilet = bilete.findIndex(b => b.id === biletId);
        
        if (indexBilet !== -1) {
            bilete[indexBilet].dataZbor = dataNoua;
            bilete[indexBilet].stare = "MODIFICAT";
            await db.push("/bilete", bilete, true);
            res.status(200).send("Data biletului a fost modificată cu succes");
        } else {
            res.status(404).send("Biletul nu a fost găsit");
        }
    } catch (error) {
        console.error("Eroare la modificarea datei biletului:", error);
        res.status(500).send("Eroare la modificarea datei biletului");
    }
});

// Endpoint pentru salvarea setărilor utilizatorului
app.put('/users/:id/setari', async (req, res) => {
    try {
        const { notificariEmail, notificariSMS, notificariPushWeb } = req.body;
        const id = req.params.id;
        let users = await db.getData("/users");
        const index = users.findIndex(u => u.id === id);
        
        if (index !== -1) {
            users[index].setari = {
                notificariEmail: notificariEmail || false,
                notificariSMS: notificariSMS || false,
                notificariPushWeb: notificariPushWeb || false
            };
            await db.push("/users", users, true);
            res.status(200).json(users[index].setari);
        } else {
            res.status(404).send("Utilizatorul nu a fost găsit");
        }
    } catch (error) {
        console.error("Eroare la actualizarea setărilor:", error);
        res.status(500).send("Eroare la actualizarea setărilor");
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
        if (!await db.exists("/zboruri")) {
            await db.push("/zboruri", []);
        }
        if (!await db.exists("/bilete")) {
            await db.push("/bilete", []);
        }
    } catch (error) {
        console.error("Eroare la initializarea bazei de date:", error);
        throw error;
    }
}

async function editareUser(id, dateUpdatate) {
    try {
        let users = await db.getData("/users");
        const index = users.findIndex(user => user.id === id);
        console.log("Căutare utilizator cu ID:", id);
        console.log("Index găsit:", index);
        
        if (index !== -1) {
            // Păstrează ID-ul original
            const idOriginal = users[index].id;
            // Actualizează datele, păstrând valorile existente pentru câmpurile nelipsite
            users[index] = {
                ...users[index],
                ...dateUpdatate,
                id: idOriginal // Asigură că ID-ul rămâne neschimbat
            };
            await db.push("/users", users, true);
            console.log("Utilizator actualizat:", users[index]);
        } else {
            console.log("Nu s-a găsit utilizatorul cu ID:", id);
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


//adaugarea userului in baza de date
async function creareContUserNou(user) {
    try {
        let users = await db.getData("/users");
        users.push(user);
        db.push("/users", users, true);
    } catch (error) {
        console.error("Eroaore la salvarea datelor userului. ", error);
        throw error;
    }
}

async function stergereUser(id) {
    try {
        let users = await db.getData("/users");
        const index = users.findIndex(users => users.id === id);
        if (index !== -1) {
            users.splice(index, 1);
            db.push("/users", users, true);
            return true;
        } else {
            throw new Error("Userul nu a fost găsit");
        }
    } catch (error) {
        console.error("Eroare la stergerea userului:", error);
        throw error;
    }
}
