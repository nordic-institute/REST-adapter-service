#!/bin/sh
# proxies and records mountebank:13000 -> www.hel.fi
curl -i -X POST -H 'Content-Type: application/json' http://127.0.0.1:2525/imposters --data '{
  "port": 13000,
  "protocol": "http",
  "name": "proxyToProxy",
  "stubs": [
    {
      "responses": [
        {
          "proxy": {
            "to": "http://www.hel.fi/",
            "mode": "proxyAlways",
            "matches": { "body": true }
          }
        }
      ]
    }
  ]
}'