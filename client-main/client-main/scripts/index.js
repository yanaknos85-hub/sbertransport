// Вообще можно взять базовый конфиг
// module.exports = require('@sber-sbertransport/tool-kit/build/mf')
// Но, так меньше маневренности, поэтому
// базовые конфиги берем из тулкита, остальное настраиваем в проекте
const cracoConfig = require("@sber-sbertransport/tool-kit/build/mf/craco/cracoConfig");
const cracoPlugins = require("@sber-sbertransport/tool-kit/build/mf/craco/cracoPlugins");
const overrideWebpackConfig = require("@sber-sbertransport/tool-kit/build/mf/webpack/overrideWebpackConfig");
const devServer = require("@sber-sbertransport/tool-kit/build/mf/devServer/devServer");
const { devServerProxyTarget } = require("@sber-sbertransport/tool-kit/build/mf/constants");

const mocksRouter = require("./devServer/devServer.mocks.router");

const { IS_DEBUG, networkLoop, isBasicAuth, devServerPort, MF_CONFIG } = require("./constants");

const config = {
  ...cracoConfig,
  devServer: {
    ...devServer({
      port: devServerPort,
      proxyTarget: devServerProxyTarget || devServerProxyTarget.DEV_PLATFORM,
      networkLoop,
      isBasicAuth,
    }),
    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error("webpack-dev-server is not defined");
      }

      // Для прод версии!
      devServer.app.get("/apps.json", (_, response) => {
        response.send({
          fleet: "http://front-micro-fleet.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru",
          cargo: "http://front-micro-cargo.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru",
          auth: "http://front-micro-auth.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru ",
          passengers: "http://front-micro-passenger.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru",
        });
      });

      devServer.app.use("/api/mock", mocksRouter);
      return middlewares;
    },
  },
  plugins: [
    ...cracoPlugins,
    {
      plugin: {
        overrideWebpackConfig: overrideWebpackConfig({
          IS_DEBUG,
          MF_CONFIG,
        }),
      },
    },
  ],
};

module.exports = config;
