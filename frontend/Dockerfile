FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# environment.ts is gitignored; fall back to the example (no Maps key: the map is the only feature that needs one)
RUN [ -f src/environments/environment.ts ] || cp src/environments/environment.example.ts src/environments/environment.ts
RUN npm run build

FROM node:22-alpine
WORKDIR /app
# The server bundle includes everything it imports except Node built-ins, so no node_modules are needed
COPY --from=build /app/dist/frontend dist/frontend
EXPOSE 4200
CMD ["node", "dist/frontend/server/server.mjs"]
