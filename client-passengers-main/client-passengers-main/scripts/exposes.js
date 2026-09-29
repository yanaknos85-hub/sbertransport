// Экспорты МФ в формате - алиас : src

module.exports = {
  './version': './version.json',
  './App': './src/app/prod/App',
  './AppProvider': './src/app/prod/AppProvider',

  './menu': './src/modules/EmployeeApp/ui/SideMenu/MenuItems',
  './routes': './src/constants/constants.routes',

  // Мой транспорт - список
  './VehiclesList': './src/modules/EmployeeApp/components/Vehicles/components/VehiclesList/VehiclesList',
}
