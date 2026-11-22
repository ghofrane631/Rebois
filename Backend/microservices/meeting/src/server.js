const app = require('./app');
const initEurekaClient = require('./eureka-client');

const PORT = process.env.PORT || 8083;

app.listen(PORT, () => {
  console.log(`meeting microservice running on http://localhost:${PORT}`);
  console.log(`Swagger UI: http://localhost:${PORT}/swagger-ui`);
  initEurekaClient(PORT);
});
