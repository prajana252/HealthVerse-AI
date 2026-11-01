# HEALTHVERSE AI

A Java (Spring Boot) personal health risk screening and prevention platform. It guides a user through an eight-stage assessment, runs a Java rule-based analysis engine, shows a results dashboard with charts, builds a personalised action plan, and produces a downloadable PDF report.

**Important:** this is an educational screening tool, not a medical diagnostic system. It does not diagnose disease or predict that anyone will develop a condition.

## Features
- Landing page, 8-stage wizard (profile, lifestyle, body metrics, symptoms, analysis, insights, plan, report)
- Live BMI calculation in Java with an animated gauge (WHO adult categories)
- Six Java assessors: cardiovascular, type 2 diabetes (Indian Diabetes Risk Score when inputs allow), respiratory, sleep, stress/lifestyle, nutrition
- Transparent scoring: every result lists the factors that produced it, and missing inputs are reported
- Emergency warning for chest pain, severe breathlessness, fainting
- Recommendation engine and interactive daily/weekly goals with progress tracking
- Chart.js dashboard built from the user's actual results
- PDF report (OpenPDF) with a "How your results were generated" section
- Save, reopen and delete assessments; demo mode with fictional data

## Tech stack
Java 17, Spring Boot 3.3 (MVC, Data JPA, Validation), H2 (default) or MySQL, OpenPDF, Maven, HTML/CSS/JavaScript, Chart.js.

## Project structure
```
src/main/java/com/healthverse/
  controller/      REST endpoints
  service/         BmiCalculatorService, AssessmentService
  assessment/      HealthRiskEngine and the six risk services
  recommendation/  RecommendationService
  report/          ReportService (PDF), MethodologyService
  model/           JPA entities     repository/  Spring Data     dto/  request and response records
  config/          CORS             exception/   error handling
src/main/resources/static/   frontend (index.html, assessment.html, css/, js/)
src/main/resources/db/schema-mysql.sql   optional manual MySQL schema
demo/sample-assessment.json  fictional sample input
Dockerfile, scripts/build-pages.sh       deployment helpers
```

## Run it locally
1. Install **JDK 17+** and **Maven 3.9+** (check with `java -version` and `mvn -v`). IntelliJ IDEA Community or VS Code with the Java extension both work: open the extracted folder as a Maven project.
2. In the project folder run: `mvn spring-boot:run`
3. Open http://localhost:8080
4. Run the tests: `mvn test`

By default it uses an in-memory H2 database, so no setup is needed. Data is lost when the app stops.

### Using MySQL
1. Install MySQL and run `src/main/resources/db/schema-mysql.sql` (or just create an empty database named `healthverse`; the tables are created automatically).
2. Set environment variables before starting the app:
   - `DB_URL=jdbc:mysql://localhost:3306/healthverse`
   - `DB_USER=your_user`  `DB_PASSWORD=your_password`

## Test all eight stages
1. Home page: click **Explore with demo data**. A "Demo mode" banner appears.
2. Slide 1: clear the age field and press Continue; an error appears. Refill it.
3. Slides 2 to 3: change height or weight and watch the BMI gauge update (170 cm and 70 kg gives 24.2).
4. Slide 4: search "chest", select chest discomfort; the urgent warning appears.
5. Run the analysis (slide 5), then open the dashboard (slide 6): check all five charts and the insights.
6. Slide 7: tick goals; the progress bar updates and survives a page refresh.
7. Slide 8: read "How your results were generated" and click **Download PDF report**.
8. Click **Save this assessment**, go to the home page, then **View report** and **Delete**.

## API
| Method | Path | Purpose |
|---|---|---|
| POST | /api/bmi | BMI, category, healthy weight range |
| POST | /api/assessments/analyze | Analyse without saving |
| POST | /api/recommendations | Recommendation plan |
| POST | /api/assessments | Save (returns id and one-time access code) |
| GET | /api/assessments/{id} | Open a saved assessment (header `X-Access-Code`) |
| POST | /api/assessments/history | Summaries for id and code pairs |
| DELETE | /api/assessments/{id} | Delete (header `X-Access-Code`) |
| POST | /api/reports/pdf | PDF report |
| POST | /api/reports/methodology | "How results were generated" content |

## Privacy (read before you demo)
No accounts. Name, medications, allergies and symptom details stay in the browser (or go only into the PDF) and are never stored on the server. A saved assessment holds the answers used by the engine, protected by a random access code (only its SHA-256 hash is stored). Anyone who has the code can read that record. There is no login, rate limiting or encryption at rest, and this is not HIPAA or GDPR compliant. **Do not enter real personal health information on a public demo.**

## Put the code on GitHub
1. Create a free account at github.com and a new empty repository named `healthverse-ai` (no README).
2. In the project folder run:
```
git init
git add .
git commit -m "HEALTHVERSE AI"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/healthverse-ai.git
git push -u origin main
```
(GitHub Desktop works too: File, Add local repository, Publish.)

## Get a public link
GitHub Pages cannot run Java, so the backend must be hosted on a Java-capable host. The simplest option serves the website and the API from one address:

### Option A (recommended): host everything on Render
1. Push the project to GitHub (above).
2. Create an account at render.com, choose **New, Web Service**, and connect your repository.
3. Choose the **Docker** runtime (the included `Dockerfile` builds the app). Pick the free instance type if offered.
4. Deploy. When it finishes you get a public URL like `https://healthverse-ai.onrender.com`. Open it.
5. Notes: free instances may sleep after inactivity, so the first load can take a minute. The default H2 database resets on every restart. For persistent data, create a MySQL database from any provider and set `DB_URL`, `DB_USER`, `DB_PASSWORD` in Render's Environment settings. Menu names and free-tier terms change, so check the host's current documentation.

### Option B: frontend on GitHub Pages, backend on Render
1. Deploy the backend as in Option A.
2. In the project folder run `bash scripts/build-pages.sh https://YOUR-APP.onrender.com` (Git Bash on Windows works). Or copy the contents of `src/main/resources/static` into a folder called `docs` and edit `docs/js/config.js` so `HV_API_BASE` is your Render URL.
3. Commit and push the `docs` folder. On GitHub open Settings, Pages, choose branch `main` and folder `/docs`.
4. In Render add the environment variable `CORS_ORIGINS=https://YOUR-USERNAME.github.io` so the backend accepts requests from your Pages site.
5. Your Pages address is `https://YOUR-USERNAME.github.io/healthverse-ai/`.

## Environment variables
`PORT` (set by hosts), `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DDL_AUTO` (default `update`), `CORS_ORIGINS` (default `*`, set it in production).

## Troubleshooting
- "Could not reach the Java server": start the backend, or set the correct URL in `js/config.js`.
- Port 8080 in use: set `PORT=9090` and open http://localhost:9090.
- Charts missing: they load Chart.js from a CDN and need internet access.
- PDF shows blank for non-English text: the PDF uses a basic Latin font.

## Known limitations
- Only the diabetes score is a published screening tool. The other thresholds are simple educational rules, and the cardiovascular result is not Framingham or ASCVD because cholesterol is not collected.
- Insight sentences on the dashboard are generated in the browser; scores, levels, plan and PDF come from Java.
- Exercise days per week, medications, allergies and symptom duration/severity are collected but not used in scoring.
- Adults (18+) only.
