// Подключенные МФ ремоуты

module.exports = {
  // auth: "auth@http://localhost:3001/auth.js?v=[Date.now()]",
  // platform: "platform@http://localhost:4005/platform.js?v=[Date.now()]",
  // cargo: "cargo@http://localhost:4002/cargo.js?v=[Date.now()]",
  // passengers: "passengers@http://localhost:4003/passengers.js?v=[Date.now()]",
  // fleet: "fleet@http://localhost:4004/fleet.js?v=[Date.now()]",

  auth: "auth@http://front-micro-auth.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/auth.js",
  platform: "platform@http://front-corp-micro-platform.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/platform.js",
  cargo: "cargo@http://front-corp-micro-cargo.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/cargo.js",
  passengers:
    "passengers@http://front-corp-micro-passengers.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/passengers.js",
  fleet: "fleet@http://front-corp-micro-fleet.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/fleet.js",
};
