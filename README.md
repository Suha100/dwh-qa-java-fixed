Run: mvn -q clean test
Open reports/extent-report.html

📊 Java Test Automation Framework (API, DB, ETL)
This repository contains a Java-based Test Automation Framework designed for validating Data Warehouse pipelines and applications.
It provides end-to-end testing coverage for:
REST API Testing (functional & schema validation)
Database Testing (SQL validation with expected results)
ETL/Data Pipeline Testing (integration checks)

🚀 Features
Java + TestNG based test framework
RestAssured for API automation
H2/SQL Database validation for schema & data checks
Excel-driven test cases for data-driven testing
WireMock for service virtualization (mock APIs)
Apache POI for reading test data from Excel
Extent Reports for rich HTML reporting
Separate reports for:
reports/rest-report.html
reports/db-report.html
reports/etl-report.html

📂 Project Structure
dwh-qa-java/
│── src/test/java/com/dwhqa/framework/
│   ├── api/           # API Tests
│   ├── db/            # Database Tests
│   ├── etl/           # ETL Tests
│   ├── reporting/     # Extent Report Listeners
│   └── util/          # Utilities (Excel, DB, Schema, WireMock)
│
│── src/test/resources/
│   ├── data/          # Test data (Excel files)
│   ├── sql/           # Schema & seed data scripts
│   └── schemas/       # JSON Schema files for API validation
│
│── reports/           # Generated test reports
│── pom.xml            # Maven dependencies

🧪 Types of Testing
1. API Testing
Uses RestAssured to send requests & validate responses
Schema validation via JSON Schema Validator
Test data comes from Excel (api-cases.xlsx)
Mocked endpoints using WireMock
Sample Test:
@Test
public void validateGetProduct() {
given().baseUri(WireMockSupport.baseUri())
.when().get("/v1/products/SKU-1001")
.then().statusCode(200);
}

2. Database Testing
Runs SQL queries against H2 in-memory DB
Validates:
Scalar results (COUNT, SUM, etc.)
Rowset results (detect mismatches)
Test cases stored in Excel (db-cases.xlsx)
Sample SQL in Excel:
SELECT COUNT(*) FROM product WHERE status = 'ACTIVE'

3. ETL Testing
Validates data pipeline transformations
Runs SQL queries pre/post ETL load
Detects missing or mismatched records
Results stored in CSV diff files under artifacts/db/

📑 Reporting
The framework generates separate HTML reports for each test type:
API Report → reports/rest-report.html
Database Report → reports/db-report.html
ETL Report → reports/etl-report.html
Each report contains:
✔ Test execution summary
✔ Pass/fail details
✔ Screenshots / SQL diff output links

▶▶️ Setup Instructions
1. Prerequisites
Install Java 17+ (verify with java -version)
Install Maven 3.8+ (verify with mvn -version)
Recommended IDE: IntelliJ IDEA or Eclipse

2. Clone the Repo
git clone https://github.com/your-org/dwh-qa-java.git
cd dwh-qa-java

3. Install Dependencies
Maven will auto-download required dependencies:
mvn clean install -DskipTests

4. Run Tests
Run all tests:
mvn clean test

Run only API Tests:
mvn test -Dgroups=api

Run only DB Tests:
mvn test -Dgroups=db

Run only ETL Tests:
mvn test -Dgroups=etl

5. View Reports
After execution, reports will be generated under the reports/ folder:
reports/rest-report.html
reports/db-report.html
reports/etl-report.html

📦 Tech Stack
Java 17+
Maven (build tool)
TestNG (test runner)
RestAssured (API testing)
WireMock (API mocking)
Apache POI (Excel reading)
H2 Database (in-memory DB for testing)
ExtentReports (reporting)

📸 Sample Reports
API Report
Database Report
ETL Report

✅ This framework is extendable and can integrate with CI/CD pipelines (Jenkins, GitHub Actions) for automated validation.