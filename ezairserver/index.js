import express from 'express'; //pentru REST API
import { JsonDB, Config } from "node-json-db";//baza de date json TEMP
import { v4 as uuidv4 } from "uuid"; //generare id unic

const app = express();
const port = 3000;

const db = new JsonDB(new Config("baza", true, true, "/"));

// Middleware to parse JSON request bodies
app.use(express.json());

// Constante pentru chei și mesaje
const CHEI = {
    ID_UTILIZATOR: "userId",
    NUME_UTILIZATOR: "username",
    PAROLA: "parola",
    EMAIL: "email",
    NUME: "nume",
    PRENUME: "prenume",
    DATA_NASTERII: "dataNasterii",
    ADMIN: "admin",
    TELEFON: "telefon",
    ID: "id",
    STARE: "stare"
};

const MESAJE = {
    EROARE_SERVER: "Eroare internă server",
    EROARE_AUTENTIFICARE: "Date de autentificare incorecte",
    SUCCES_CREARE_CONT: "Cont creat cu succes",
    EROARE_CREARE_CONT: "Eroare la crearea contului",
    UTILIZATOR_NEGASIT: "Utilizatorul nu a fost găsit",
    SUCCES_STERGERE: "Utilizator șters cu succes",
    EROARE_STERGERE: "Eroare la ștergerea utilizatorului"
};

// Actualizăm constructorul pentru User
class user {
    constructor(username, parola, email, nume, prenume, dataNasterii, admin = false, telefon) {
        this[CHEI.ID] = uuidv4();
        this[CHEI.NUME_UTILIZATOR] = username;
        this[CHEI.PAROLA] = parola;
        this[CHEI.EMAIL] = email;
        this[CHEI.NUME] = nume;
        this[CHEI.PRENUME] = prenume;
        this[CHEI.DATA_NASTERII] = dataNasterii;
        this[CHEI.ADMIN] = admin;
        this.setari = {
            notificariEmail: true,
            notificariSMS: false,
            notificariPushWeb: true,
            notificariPromotii: true,
            notificariAnulari: true,
            notificariModificari: true
        };
        this[CHEI.TELEFON] = telefon;
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
        this[CHEI.ID] = uuidv4();
        this[CHEI.ID_UTILIZATOR] = userId;
        this.zborId = zborId;
        this.detaliiZbor = detaliiZbor;
        this.dataZbor = dataZbor;
        this[CHEI.STARE] = "ACTIV";
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

app.get('/zboruri/orase', async (req, res) => {
    try {
        const zboruri = await db.getData("/zboruri");
        const orase = new Set();
        zboruri.forEach(zbor => {
            orase.add(zbor.origine);
            orase.add(zbor.destinatie);
        });
        res.status(200).json(Array.from(orase));
    } catch (error) {
        console.error("Eroare la obținerea orașelor:", error);
        res.status(500).send("Eroare la obținerea orașelor");
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
        res.status(200).json(user);
    } catch (error) {
        console.error("Eroare la verificarea datelor de login:", error);
        res.status(401).json({ error: MESAJE.EROARE_AUTENTIFICARE });
    }
});

//metoda pentru creare cont
app.post('/users/creareCont', async (req, res) => {
    try {
        const { username, parola, email, nume, prenume, dataNasterii, admin } = req.body;
        const userNou = new user(username, parola, email, nume, prenume, dataNasterii, admin);
        await creareContUserNou(userNou);
        res.status(200).json({ mesaj: MESAJE.SUCCES_CREARE_CONT });
    } catch (error) {
        console.error("Eroare la crearea contului:", error);
        res.status(500).json({ error: MESAJE.EROARE_CREARE_CONT });
    }
});

app.delete('/users/:id', async (req, res) => {
    try {
        const id = req.params.id;
        if (await stergereUser(id)) {
            res.status(200).json({ mesaj: MESAJE.SUCCES_STERGERE });
        } else {
            res.status(404).json({ error: MESAJE.UTILIZATOR_NEGASIT });
        }
    } catch (error) {
        console.error("Eroare la ștergerea utilizatorului:", error);
        res.status(500).json({ error: MESAJE.EROARE_STERGERE });
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
        const { origine, destinatie, data } = req.query;
        let zboruri = await db.getData("/zboruri");
        
        // Filtrare după origine și destinație (obligatorii)
        let zboruriGasite = zboruri.filter(zbor => zbor.origine === origine && zbor.destinatie === destinatie);
        
        // Filtrare suplimentară după dată (opțională)
        if (data) {
            zboruriGasite = zboruriGasite.filter(zbor => {
                // Verifică dacă data coincide
                // În funcție de formatul datelor, ar putea fi necesar să convertim
                const dataPlecare = zbor.dataPlecare;
                return dataPlecare.includes(data) || data.includes(dataPlecare);
            });
        }
        
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

// Endpoint pentru modificarea zborului biletului
app.put('/bilete/:id/modificare-zbor', async (req, res) => {
    try {
        const biletId = req.params.id;
        const { zborId, dataNoua } = req.body;
        
        // Verificare dacă zborul există și are locuri disponibile
        let zboruri = await db.getData("/zboruri");
        const zborIndex = zboruri.findIndex(z => z.id === zborId);
        
        if (zborIndex === -1) {
            return res.status(404).send("Zborul selectat nu a fost găsit");
        }
        
        if (zboruri[zborIndex].locuriLibere <= 0) {
            return res.status(400).send("Nu mai sunt locuri disponibile pe acest zbor");
        }
        
        // Obține detaliile noului zbor pentru actualizarea biletului
        const zborNou = zboruri[zborIndex];
        
        // Actualizează biletul cu noul zbor
        let bilete = await db.getData("/bilete");
        const indexBilet = bilete.findIndex(b => b.id === biletId);
        
        if (indexBilet === -1) {
            return res.status(404).send("Biletul nu a fost găsit");
        }
        
        // Salvează ID-ul vechi pentru a incrementa locurile disponibile
        const zborVechiId = bilete[indexBilet].zborId;
        
        // Actualizează biletul
        bilete[indexBilet].zborId = zborId;
        bilete[indexBilet].dataZbor = dataNoua;
        bilete[indexBilet].detaliiZbor = `${zborNou.origine} -> ${zborNou.destinatie}`;
        bilete[indexBilet].pret = zborNou.pret; // Actualizează și prețul cu prețul noului zbor
        bilete[indexBilet].stare = "MODIFICAT";
        
        // Decrementează locurile disponibile pentru noul zbor
        zboruri[zborIndex].locuriLibere--;
        
        // Incrementează locurile disponibile pentru zborul vechi
        const zborVechiIndex = zboruri.findIndex(z => z.id === zborVechiId);
        if (zborVechiIndex !== -1) {
            zboruri[zborVechiIndex].locuriLibere++;
        }
        
        // Salvează modificările
        await db.push("/bilete", bilete, true);
        await db.push("/zboruri", zboruri, true);
        
        res.status(200).send("Biletul a fost mutat pe un alt zbor cu succes");
    } catch (error) {
        console.error("Eroare la modificarea zborului biletului:", error);
        res.status(500).send("Eroare la modificarea zborului biletului");
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
        const user = users.find(user => 
            user[CHEI.NUME_UTILIZATOR] === username && 
            user[CHEI.PAROLA] === parola
        );
        if (user) {
            return user;
        }
        throw new Error(MESAJE.EROARE_AUTENTIFICARE);
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
