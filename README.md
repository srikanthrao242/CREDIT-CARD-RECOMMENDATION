# CREDIT-CARD-RECOMMENDATION


#### Introduction <br>
This service will expose an api endpoint that consumes some information about the user’s financial situation and return credit cards recommended for them, sorted based on their eligibility and the cards’ APRss(annual percentage rate).
<br>
#### creditcards api<br>
(/creaditcards) when we call this api it will do following. <br>

  * Validates inputs if there is any condition fails throw appropriate errors. <br>
  * When tha input comes from user it will create inputs for Partner 1 - CSCards and  Partner 2 - ScoredCards <br>
  * It will create clients for partner-1 and partner-2 with baseUri from config/environmental variables. <br>
  * It will call partner- 1 and partner-2 with created inputs and get get their response.
  * And it will calculate the sortingScore using eligibility/approvalRating and apr <br>
                sortingScore = eligibility ∗ (math.pow(1/apr, 2) ) <br>
  * If there is any exception it will throw with description as response.

![Sequence Diargam](/src/main/resources/sequenceDiagram.png?raw=true "Sequence Diagram")
<br>

### used stack
<br>
scala-2.13<br>
akka-http<br>
akka-stream<br>
pure-config<br>
spray-json<br>

### Environment variables<br>

### compile and run<br>
HTTP_PORT<br>
CSCARDS_ENDPOINT<br>
SCOREDCARDS_ENDPOINT<br>

Find start.sh file it will do clean compile and start the server 
