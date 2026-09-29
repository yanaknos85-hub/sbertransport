export enum routes {
  Auth = '/oauth',
  Logout = '/oauth/logout',
  BasicSuccess = '/oauth/basic-success',
  Success = '/oauth/success',
  Failure = '/oauth/failure',
  ResetPassword = '/oauth/reset',
  SudirApi = '/api/sudir/oauth2/authorization/sudir',

  Home = '/',
  Nav = '/nav',
  Profile = '/nav/profile',
  ContactPhone = '/nav/contact-phone',
  DriverLicense = '/nav/driver-license',
  PhoneConfirm = '/nav/phone-confirm',
  CodeConfirm = '/nav/code-confirm',
  SuccessPhoneConfirm = '/nav/success-phone-confirm',
  MyTrips = '/nav/trips',
  TripInfo = '/nav/trip-info/:id',
  Trip = '/trip/:id',
  TripFinish = '/trip-finish/:id',

  Page404 = '/404',
}
