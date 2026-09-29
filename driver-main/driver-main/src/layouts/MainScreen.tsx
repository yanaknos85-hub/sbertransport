import { FC, lazy, Suspense } from 'react';
import { observer } from 'mobx-react';
import { useCurrentTrip } from 'api/services/Trips/Trips.query';
import { useAppStore } from 'stores/stores.context';
import Spin from 'components/Spin';
import ErrorBoundary from 'components/ErrorBoundary';

const VehicleConfirm = lazy(() => import('modules/VehicleConfirm'));
const Trips = lazy(() => import('modules/Trips'));
const ActiveTrip = lazy(() => import('modules/ActiveTrip'));

const MainScreen: FC = observer(() => {
  const { mainLayoutStore } = useAppStore();

  const { data: currentTrip, isLoading } = useCurrentTrip({
    refetchOnWindowFocus: true,
  });

  if (mainLayoutStore.needConfirmVehicle) return (
    <ErrorBoundary>
      <Suspense fallback={<Spin />}>
        <VehicleConfirm />
      </Suspense>
    </ErrorBoundary>
  );

  if (isLoading) return <Spin />;

  if (!currentTrip) return (
    <ErrorBoundary>
      <Suspense fallback={<Spin />}>
        <Trips />
      </Suspense>
    </ErrorBoundary>
  );

  return (
    <ErrorBoundary>
      <Suspense fallback={<Spin />}>
        <ActiveTrip trip={currentTrip} />
      </Suspense>
    </ErrorBoundary>
  );
});

export default MainScreen;
