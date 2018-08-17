FROM alpine:latest

EXPOSE 2525
ENTRYPOINT ["mb"]
CMD ["start"]

RUN apk add --update nodejs
RUN npm install -g mountebank --production
