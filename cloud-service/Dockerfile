FROM node:22-alpine

WORKDIR /app
COPY package.json ./
COPY src ./src

RUN mkdir -p /app/data

ENV HOST=0.0.0.0
ENV PORT=8080
ENV DATA_FILE=/app/data/events.json

EXPOSE 8080

CMD ["npm", "start"]
