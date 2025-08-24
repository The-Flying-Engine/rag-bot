## 1) Verify required software

Open **Command Prompt** and run each command.
If any command shows “not recognized,” install that software first.

```bat
git --version
java -version
node -v
npm -v
ollama --version
```

> Note: Spring Boot uses Maven. If your repo includes `mvnw.cmd`, you don’t need to install Maven.
> If there’s no `mvnw.cmd`, install Maven and ensure `mvn -v` works.

---

## 2) Get the code

```bat
cd %USERPROFILE%
mkdir code
cd code
git clone https://github.com/<your-org>/<your-repo>.git
cd <your-repo>
```

Replace `<your-org>/<your-repo>` with your actual repo path.

---

## 3) Download and start Ollama models

Open **Command Prompt #1** (keep it running):

```bat
ollama serve
```

Open **Command Prompt #2** and pull the models:

```bat
ollama pull llama3
ollama pull nomic-embed-text
```

---

## 4) Start the Spring Boot backend (port 8080)

From your **project root** (where `pom.xml` is):

```bat
.\mvnw.cmd spring-boot:run
```

> If there’s no `mvnw.cmd`, use:
>
> ```bat
> mvn spring-boot:run
> ```

When started, check in your browser:

* Backend page: **[http://localhost:8080/](http://localhost:8080/)**
* H2 console: **[http://localhost:8080/h2](http://localhost:8080/h2)**

  * JDBC URL: `jdbc:h2:file:./ragdb`
  * User: `sa`
  * Password: *(leave empty)*

---

## 5) Run the React frontend (port 5173)

Open **Command Prompt #3**:

```bat
cd %USERPROFILE%\code\<your-repo>\rag-ui
npm install
npm run dev
```

Open in browser:

* React: **[http://localhost:5173](http://localhost:5173)**
* Backend: **[http://localhost:8080](http://localhost:8080)**
* H2: **[http://localhost:8080/h2](http://localhost:8080/h2)**


* Keep **3 terminals** open:

  1. `ollama serve`
  2. Spring Boot (`mvnw.cmd spring-boot:run`)
  3. React (`npm run dev`)
* If the browser shows a **CORS** error, ensure the backend has:

  ```
  app.cors.allowed-origins=http://localhost:5173
  ```

  Then restart the backend.
* If ports are busy, close conflicting apps or change the port in config.
