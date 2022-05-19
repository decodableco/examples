# Using Postman to Send Events

## Postman & OAuth2

- first retrieve an access token for the API
- then use that token to auth future requests


```bash
yq -r .tokens.default.access_token ~/.decodable/auth
```

## Use Cases

- backend microservices in a SAGA - microservices that do not interface with applications
- customer onboarding - build entire pipelines to get customers their data
- custom UI - to handle data requests from customers through a data portal or data catalog