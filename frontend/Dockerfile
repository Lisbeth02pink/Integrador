FROM node:20-alpine

WORKDIR /app

# Copiar dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm install

# Copiar código
COPY . .

# Exponer puerto Angular
EXPOSE 4200

# Ejecutar Angular
CMD ["npm", "start"]