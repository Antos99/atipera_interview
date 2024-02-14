# atipera_interview_app

## Description

The "atipera_interview" application is a web-based platform 
that enables the retrieval of information about repositories
belonging to a specified user from the Github API.

## Technologies

Application was created using Java 21 and Spring Boot 3.2.2 (Spring Web).
[MockServer](https://www.mock-server.com/), [Mockito](https://site.mockito.org/)
and JUnit 5 were utilized for unit and integration testing.

## Run application

To run the application, execute the following commands:

for Linux:
```bash
./mvnw clean package
java -jar target/atipera_interview-0.0.1-SNAPSHOT.jar
```
for Windows:
```powershell
mvnw.cmd clean package
java -jar target/atipera_interview-0.0.1-SNAPSHOT.jar
```
**NOTE:** Java version 21 is required!
## How to use application

The application accepts the following HTTP requests:
```http
GET http://host:8080/api/v1/github/username/repositories
Accept: application/json
```
- *host* - the host address (typically *localhost* for local machine)
- *username* - the name of the GitHub user for which to retrieve
information about all its repositories


If the specified username exists on Github, the application returns an HTTP response with
status code 200 and a JSON body in the following format:

```json
[
    {
        "repositoryName": "${repositoryName}",
        "ownerLogin": "${githubOwnerLogin}",
        "branches": [
            {
                "name": "${branchName}",
                "lastCommitSha": "${lastCommitSha}"
            }
        ]
    }
]
```
**NOTE:** Response contains only not-fork repositories.

If the specified username does not exist on Github, the application returns an HTTP response
with status code 404 and a JSON response in the following format:

```json
{
    "status": 404,
    "message": "User with username ${username} does not exist"
}
```
