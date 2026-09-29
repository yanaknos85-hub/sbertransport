export enum TripTypes {
  Passenger = 'passenger',
  Cargo = 'cargo',
}

// TODO 33005, authorFullName и factCost временно скрываются, пока нет данных с бэка

export enum Columns {
  HumanReadableId = 'humanReadableId',
  RequestHumanReadableId = 'requestHumanReadableId',
  DesiredDate = 'desiredDate',
  RouteStart = 'routeStart',
  RouteWaypoints = 'routeWaypoints',
  RouteEnd = 'routeEnd',
  ExpectedTime = 'expectedTime',
  ExpectedWaitTime = 'expectedWaitTime',
  ExpectedDistance = 'expectedDistance',
  MinRideDistanceCost = 'minRideDistanceCost',
  WaitCostPerMin = 'waitCostPerMin',
  RideCostPerKm = 'rideCostPerKm',
  TotalWithoutVAT = 'totalWithoutVAT',
  Status = 'status',
  Comment = 'comment',
  TripType = 'tripType',
  ContractNumber = 'contractNumber',
  DriverFullName = 'driverFullName',
  VehicleNumber = 'vehicleNumber',
  PassengerFullNames = 'passengerFullNames',
  // AuthorFullName = 'authorFullName',
  CreationTime = 'creationTime',
  FactDistance = 'factDistance',
  // FactCost = 'factCost',
  FactTime = 'factTime',
  FactWaitTime = 'factWaitTime',
}
