/* eslint-disable no-unsafe-optional-chaining */
import '../../override.scss';
import { Input } from 'antd';
import { observer } from 'mobx-react';
import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import debounce from 'lodash/debounce';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';
import { Moment } from 'moment';

import { ReactComponent as SearchIcon } from 'shared/components/Images/searchIcon.svg';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';
import { getTimeZone } from 'modules/EmployeeApp/components/CreateTripRequest/utils/utils';
import { IContentDispatcherTransport } from 'stores/Trip/Trip.interface';
import { SpinWrapped } from 'shared/components';
import { formatRubles } from 'utils';

import styles from './availableCars.module.scss';

interface PassengerInformationProps {
  form: FormInstance;
  setVisibleGroupTransferChoosingBookingInterval: React.Dispatch<React.SetStateAction<boolean>>;
  setVisibleGroupTransferChoosingCar: React.Dispatch<React.SetStateAction<boolean>>;
  setVisiblevisibleGroupTransferForm: React.Dispatch<React.SetStateAction<boolean>>;
  switchBookingTime: boolean;
  dateBooking: Moment | undefined;
}

export const AvailableCars: FC<PassengerInformationProps> = observer(
  ({
    form,
    setVisibleGroupTransferChoosingBookingInterval,
    setVisibleGroupTransferChoosingCar,
    setVisiblevisibleGroupTransferForm,
    switchBookingTime,
    dateBooking,
  }): JSX.Element => {
    const {
      [StoreNames.tripStore]: tripStore,
      [StoreNames.geoStore]: geo,
      [StoreNames.selfStore]: selfStore,
    } = useAppStoreContext();
    const [transports, setTransports] = useState<IContentDispatcherTransport[] | never[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [searchAvailableCars, setSearchAvailableCars] = useState('');
    const date = form.getFieldValue('desiredDateGroupTransfer');
    const route = geo.calculatedRoute;

    const getContractorDispatcherTransports = () => {
      if (route) {
        const data = {
          distance: route.distance,
          time: route.time,
          tripDate: (dateBooking ? dateBooking?.valueOf() : date.valueOf()),
          startPoint: { ...route.waypoints[0] },
          organizationId: selfStore.orgId,
          employeeId: selfStore.empId,
          waitingTime: route.waypoints[0].waitTime,
          timeZone: getTimeZone(),
          waypoints: route.waypoints,
        };

        const searchParams = {
          search: searchAvailableCars ?? undefined,
          availableOnly: !searchAvailableCars ? true : undefined,
        };

        setIsLoading(true);

        tripStore.getContractorDispatcherTransports(data, searchParams)
          .then(transports => {
            setIsLoading(false);
            setTransports(transports.content);
          });
      }
    };

    useEffect(() => {
      getContractorDispatcherTransports();
    }, [searchAvailableCars, dateBooking]);

    const choosingCar = car => {
      tripStore.setAvailableDispatcherTransport(car);

      if (!switchBookingTime) {
        if (!car.availableOnly) {
          setVisibleGroupTransferChoosingCar(false);
          setVisibleGroupTransferChoosingBookingInterval(true);
          return;
        }
        setVisiblevisibleGroupTransferForm(true);
        setVisibleGroupTransferChoosingCar(false);
        setVisibleGroupTransferChoosingBookingInterval(false);
        return;
      }
      setVisibleGroupTransferChoosingCar(false);
      setVisibleGroupTransferChoosingBookingInterval(true);
    };

    const debounceSearch = useCallback(
      debounce(value => {
        setSearchAvailableCars(value);
      }, 1000),
      []
    );

    const changeSearch = e => {
      debounceSearch(e.target.value);
    };

    return (
      <div className="wrapper_availableCars">
        <span className={styles.title_availableCars}>
          Доступные автомобили
        </span>
        <div className="search_availableCars">
          <Input
            onChange={changeSearch}
            suffix={(
              <SearchIcon />
          )}
          />
        </div>
        {isLoading ? (
          <SpinWrapped />
        )
          : (
            <div className={styles.list_availableCars}>
              {transports.map(car => (
                <div className={styles.wrapper_car} onClick={() => choosingCar(car)}>
                  <div className={styles.car_info}>
                    <span className={styles.car_title__brand}>{`${car.brand ?? ''} ${car.model ?? ''}`}</span>
                    <span className={styles.car_title__stateNumber}>{car?.stateNumber ?? ''}</span>
                  </div>
                  <div className={styles.car_cost}>
                    <span className={styles.cost_title}>{formatRubles(car.calculated.cost / 100)}</span>
                    <div className={styles.car_status}>
                      <div className={classNames(car.availableOnly ? styles.car_status__active : styles.car_status__busy)} />
                      <span className={styles.car_status__title}>{car.availableOnly ? 'Свободен' : 'Занят'}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
      </div>
    );
  }
);
