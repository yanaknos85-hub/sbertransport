// Подключенные МФ ремоуты

module.exports = {
  // auth: 'auth@http://localhost:3001/auth.js?v=[Date.now()]',
  // cargo: 'cargo@http://localhost:3002/cargo.js?v=[Date.now()]',
  // passengers: 'passengers@http://localhost:3003/passengers.js?v=[Date.now()]',
  // fleet : 'fleet@http://localhost:3004/fleet.js?v=[Date.now()]',

  auth: "auth@http://front-micro-auth.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/auth.js",
  cargo: "cargo@http://front-micro-cargo.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/cargo.js?v=[Date.now()]",
  passengers:
    "passengers@http://front-micro-passenger.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/passengers.js?v=[Date.now()]",
  fleet: "fleet@http://front-micro-fleet.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru/fleet.js?v=[Date.now()]",
};
