// Вообще можно взять базовый конфиг
// module.exports = require('@sber-sbertransport/tool-kit/build/mf')
// Но, так меньше маневренности, поэтому
// базовые конфиги берем из тулкита, остальное настраиваем в проекте
const cracoConfig = require('@sber-sbertransport/tool-kit/build/mf/craco/cracoConfig');
const cracoPlugins = require('@sber-sbertransport/tool-kit/build/mf/craco/cracoPlugins');
const overrideWebpackConfig = require('@sber-sbertransport/tool-kit/build/mf/webpack/overrideWebpackConfig');
const devServer = require('@sber-sbertransport/tool-kit/build/mf/devServer/devServer');
const { devServerProxyTarget } = require('@sber-sbertransport/tool-kit/build/mf/constants');

const mocksRouter = require('./devServer/devServer.mocks.router');

const {
  IS_DEBUG,
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  MF_CONFIG,
  webpackDevEntryPath,
} = require('./constants');

const config = {
  ...cracoConfig,
  devServer: devServer({
    port: devServerPort,
    proxyTarget: devServerProxyTarget || devServerProxyTarget.DEV_CARGO,
    networkLoop,
    isBasicAuth,
    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error('webpack-dev-server is not defined');
      }
      devServer.app.use('/api/mock', mocksRouter);
      return middlewares;
    },
  }),
  plugins: [
    ...cracoPlugins,
    {
      plugin: {
        overrideWebpackConfig: overrideWebpackConfig({
          IS_DEBUG,
          IS_REMOTE,
          MF_CONFIG,
          webpackDevEntryPath,
        }),
      },
    },
  ],
};

module.exports = config;
