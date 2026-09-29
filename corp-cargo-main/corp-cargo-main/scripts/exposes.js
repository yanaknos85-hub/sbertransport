// Экспорты МФ в формате - алиас : src

module.exports = {
  './version': './version.json',
  './App': './src/app/prod/App',
  './AppProvider': './src/app/prod/AppProvider',
  './routes': './src/constants/constants.routes',

  './modules/Registry/tabRoutes': './src/modules/Registry/constants/Registry.routes',
  './modules/BusinessReports/tabRoutes': './src/modules/BusinessReports/constants/BusinessReports.routes',
  
  './modules/OrderExecution': './src/modules/OrderExecution/OrderExecutionContent',
  './modules/OrderExecution/Router': './src/modules/OrderExecution/OrderExecutionRouter',

  './modules/TripSettings/TripSettingsProvider': './src/modules/TripSettings/context/TripSettingsProvider',
  './modules/TripSettings/TransportTypes': './src/modules/TripSettings/components/TransportTypes/TransportTypesContent',
  './modules/TripSettings/TripPurposes': './src/modules/TripSettings/components/TripPurposes/TripPurposesContent',
  './modules/TripSettings/Approvals': './src/modules/TripSettings/components/Approvals/ApprovalsContent',
  './modules/TripSettings/SharedRideSettings': './src/modules/TripSettings/components/SharedRideSettings/SharedRideSettingsContent',

  './modules/ServiceSettings/DeadlineSettings': './src/modules/ServiceSettings/components/DeadlineSettings/DeadlineSettingsContent',
  './modules/ServiceSettings/NotificationsSettings': './src/modules/ServiceSettings/components/NotificationsSettings/NotificationsSettingsContent',
  './modules/ServiceSettings/RegistrySettings': './src/modules/ServiceSettings/components/RegistrySettings/RegistrySettingsContent',

  './modules/TariffSettings/TariffSettingsProvider': './src/modules/TariffSettings/context/TariffSettingsProvider',
  './modules/TariffSettings/Contractors': './src/modules/TariffSettings/components/Contractors/ContractorsContent',
  './modules/TariffSettings/Contracts': './src/modules/TariffSettings/components/Contracts/ContractsContent',
  './modules/TariffSettings/Tariffs': './src/modules/TariffSettings/components/Tariffs/TariffsContent',

  './modules/Customers/CustomersProvider': './src/modules/Customers/context/CustomersProvider',
  './modules/Customers/Contracts': './src/modules/Customers/Contracts/ContractsContent',
  './modules/Customers/Tariffs': './src/modules/Customers/Tariffs/TariffsContent',
}
