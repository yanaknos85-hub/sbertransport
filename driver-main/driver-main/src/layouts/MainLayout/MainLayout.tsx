import {
  FC, Suspense, useEffect, useRef, useState
} from 'react';
import {
  FloatingPanel, FloatingPanelRef, Mask
} from 'antd-mobile';
import { Outlet, useLocation } from 'react-router';
import { observer } from 'mobx-react';

import { useProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { useAppStore } from 'stores/stores.context';
import useErrorBoundary from 'hooks/useErrorBoundary';
import Spin from 'components/Spin';
import PhoneConfirmationAlert from 'components/PhoneConfirmationAlert';
import ErrorBoundary from 'components/ErrorBoundary';
import { routes } from 'constants/routes.constants';
import OnlineSwitcher from './components/OnlineSwitcher/OnlineSwitcher';
import OnlineButton from './components/OnlineButton/OnlineButton';
import Nav from './components/Nav/Nav';
import Map from './components/Map';
import TripsUpdate from './components/TripsUpdate';
import { useCurrentTrip } from 'api/services/Trips/Trips.query';
import { useLongPress } from '../../hooks/useLongPress';
import { LongPressEventType } from '../../types/longPressTypes';
import ChangeDeparture from './components/ChangeDeparture/ChangeDeparture';
import { LatLngTuple } from 'api/services/Geo/Geo.types';

import styles from './MainLayout.module.scss';

const MainLayout: FC = observer(() => {
  const { data: currentTrip } = useCurrentTrip({
    refetchOnWindowFocus: true,
  });

  const [isChangeDepartureVisible, setIsChangeDepartureVisible] = useState(false);
  const [newCoordinates, setNewCoordinates] = useState<LatLngTuple>([0, 0]);

  const onLongPress = () => {
    setIsChangeDepartureVisible(true);
  };

  const longPressEvent = useLongPress(() => {
    onLongPress();
  }, {
    detect: LongPressEventType.Touch, threshold: 4000, cancelOnMovement: true, cancelOutsideElement: true,
  });

  const { online, phoneConfirmed } = useProfile().data;

  const [isMaskVisible, setIsMaskVisible] = useState(false);

  const onHeightChange = (height: number) => {
    setIsMaskVisible(height > 200);
  };

  const { pathname } = useLocation();

  const { mainLayoutStore } = useAppStore();

  const isNav = pathname.startsWith(routes.Nav);

  const isFloatingPanelVisible = online || mainLayoutStore.needConfirmVehicle;

  const floatingPanel = useRef<FloatingPanelRef>(null);

  useEffect(() => {
    mainLayoutStore.initPanel(floatingPanel);
  }, [isFloatingPanelVisible, floatingPanel, mainLayoutStore]);

  const closePanel = () => {
    floatingPanel.current?.setHeight(mainLayoutStore.floatingPanelAnchors[0]);
  };

  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <>
      <ErrorBoundary fallback={() => null}>
        <TripsUpdate />
      </ErrorBoundary>

      <Mask
        visible={isMaskVisible && isFloatingPanelVisible}
        className={styles.mask}
        onMaskClick={closePanel}
      />
      {currentTrip && (
      <ChangeDeparture
        isChangeDepartureVisible={isChangeDepartureVisible}
        handleChange={() => setIsChangeDepartureVisible(false)}
        trip={currentTrip}
        clickedPoints={newCoordinates}
      />
      )}
      <div style={{ height: '100%' }} {...longPressEvent()}>
        <Map handleClick={setNewCoordinates} />
      </div>
      <Nav />
      <OnlineSwitcher />
      {!phoneConfirmed && <PhoneConfirmationAlert className={styles.warning} />}

      {isFloatingPanelVisible ? (
        <FloatingPanel
          anchors={mainLayoutStore.floatingPanelAnchors}
          onHeightChange={onHeightChange}
          className={styles['floating-panel']}
          ref={floatingPanel}
        >
          <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
            <Suspense fallback={<Spin />}>
              {!isNav && <Outlet />}
            </Suspense>
          </ErrorBoundary>
        </FloatingPanel>
      ) : (
        <OnlineButton />
      )}
    </>
  );
});

export default MainLayout;
