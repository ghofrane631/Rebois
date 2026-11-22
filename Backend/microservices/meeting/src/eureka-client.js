const Eureka = require('eureka-js-client').Eureka;

function initEurekaClient(port) {
  const currentPort = parseInt(port || 8083, 10);
  const eurekaHost = process.env.EUREKA_HOST || 'localhost';
  const eurekaPort = parseInt(process.env.EUREKA_PORT || 8761, 10);

  const client = new Eureka({
    instance: {
      instanceId: `localhost:meeting:${currentPort}`,
      app: 'MEETING',
      hostName: 'localhost',
      ipAddr: '127.0.0.1',
      statusPageUrl: `http://localhost:${currentPort}/api/meetings/hello`,
      port: {
        '$': currentPort,
        '@enabled': true,
      },
      vipAddress: 'MEETING',
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn',
      },
    },
    eureka: {
      host: eurekaHost,
      port: eurekaPort,
      servicePath: '/eureka/apps/',
      maxRetries: 10,
      requestRetryDelay: 2000,
    },
  });

  client.start((error) => {
    if (error) {
      console.warn(`[Eureka] Warning: could not register MEETING on port ${currentPort}:`, error.message || error);
    } else {
      console.log(`[Eureka] MEETING service successfully registered on port ${currentPort}`);
    }
  });

  const cleanup = () => {
    client.stop(() => {
      console.log(`[Eureka] MEETING deregistered from Eureka`);
      process.exit();
    });
  };
  process.on('SIGINT', cleanup);
  process.on('SIGTERM', cleanup);

  return client;
}

module.exports = initEurekaClient;
