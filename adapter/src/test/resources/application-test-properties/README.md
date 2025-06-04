configuration for REST-adapter which is offers both consumer and
provider endpoints, and consumer calls provider endpoint without
a security server in between.

client (rest) <- rest -> consumer endpoint <- XROAD SOAP -> provider endpoint <- rest -> wiremock rest api

communication between consumer and provider endpoint is encrypted, and
clients JSON message is converted with convertPost = true setting