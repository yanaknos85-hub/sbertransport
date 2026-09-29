import { FC, lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router';
import { routes } from 'constants/routes.constants';
import { TripTabs } from 'constants/trips.constants';
import Logout from 'components/Logout/Logout';
import Reset from 'components/Basic/Reset/Reset';
import Spin from 'components/Spin';
import ErrorBoundary from 'components/ErrorBoundary';
import useErrorBoundary from 'hooks/useErrorBoundary';

const ProtectedLayout = lazy(() => import('layouts/ProtectedLayout'));
const MainLayout = lazy(() => import('layouts/MainLayout'));
const MainScreen = lazy(() => import('layouts/MainScreen'));
const Profile = lazy(() => import('modules/Profile'));
const ContactPhone = lazy(() => import('modules/ContactPhone'));
const DriverLicense = lazy(() => import('modules/DriverLicense'));
const PhoneConfirmation = lazy(() => import('modules/PhoneConfirmation'));
const CodeConfirmation = lazy(() => import('modules/CodeConfirmation'));
const SuccessPhoneConfirm = lazy(() => import('modules/SuccessPhoneConfirm'));
const MyTrips = lazy(() => import('modules/MyTrips'));
const Trip = lazy(() => import('modules/Trip'));
const TripFinish = lazy(() => import('modules/TripFinish'));
const Auth = lazy(() => import('modules/Auth'));
const Menu = lazy(() => import('modules/Menu'));

const AppRouter: FC = () => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<Spin />}>
        <Routes>
          <Route path={routes.Auth} element={<Auth />} />
          <Route path={routes.Logout} element={<Logout />} />
          <Route path={routes.ResetPassword} element={<Reset />} />

          <Route element={<ProtectedLayout />}>
            <Route path={routes.Home} element={<MainLayout />}>
              <Route index element={<MainScreen />} />
              <Route path={routes.Trip} element={<Trip />} />
              <Route path={routes.TripFinish} element={<TripFinish />} />

              <Route path={routes.Nav}>
                <Route index element={<Menu />} />
                <Route path={routes.Profile} element={<Profile />} />
                <Route path={routes.ContactPhone} element={<ContactPhone />} />
                <Route path={routes.DriverLicense} element={<DriverLicense />} />
                <Route path={routes.PhoneConfirm} element={<PhoneConfirmation />} />
                <Route path={routes.CodeConfirm} element={<CodeConfirmation />} />
                <Route path={routes.SuccessPhoneConfirm} element={<SuccessPhoneConfirm />} />
                <Route path={`${routes.MyTrips}/:type`} element={<MyTrips />} />
                <Route path={routes.TripInfo} element={<Trip />} />

                <Route path={routes.MyTrips} element={<Navigate replace to={`${routes.MyTrips}/${TripTabs.Active}`} />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to={routes.Page404} />} />
        </Routes>
      </Suspense>
    </ErrorBoundary>
  );
};

export default AppRouter;
