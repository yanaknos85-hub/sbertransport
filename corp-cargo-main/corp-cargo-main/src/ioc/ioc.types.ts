export const TYPES = {
  selfEmployee: Symbol.for('selfEmployee'),
  selfEmployeeProvider: Symbol.for('selfEmployeeProvider'),
  IHttpServiceFactory: Symbol.for('IHttpServiceFactory'),
  IHttpService: Symbol.for('IHttpService'),
  ILogger: Symbol.for('ILogger'),
  IResponseService: Symbol.for('IResponseService'),
  IHistory: Symbol.for('IHistory'),
  Token: Symbol.for('Token'),

  ISelfEmployeeService: Symbol.for('ISelfEmployeeService'),
  ISelfEmployeeStore: Symbol.for('ISelfEmployeeStore'),
  ISelfEmployeeStoreProvider: Symbol.for('ISelfEmployeeStoreProvider'),

  IEmployeeStore: Symbol.for('IEmployeeStore'),
  IEmployeeService: Symbol.for('IEmployeeService'),

  IEmployeesAttributeStore: Symbol.for('IEmployeesAttributeStore'),
  IEmployeesAttributeService: Symbol.for('IEmployeesAttributeService'),

  IFilesService: Symbol.for('IFilesService'),
  IFilesStore: Symbol.for('IFilesStore'),

  IAuthService: Symbol.for('IAuthService'),
  IAuthStore: Symbol.for('IAuthStore'),

  ICargoService: Symbol.for('ICargoService'),
  ICargoStore: Symbol.for('ICargoStore'),

  IPlannerStore: Symbol.for('IPlannerStore'),
  IPlannerService: Symbol.for('IPlannerService'),
};
