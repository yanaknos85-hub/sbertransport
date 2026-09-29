
/* eslint-disable no-unused-expressions */
import { Radio, RadioChangeEvent } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import * as React from 'react';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, ITripTariff, TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { TaxiToolTripPortal } from './Card/CostToolTip';
import TaxiClassCardWrapper from './TaxiClassCardWrapper';
import styles from './taxiClasses.module.scss';
import {
  ExternalClassesConfig, TransportTypesConfig, busClasses, busConfig, busIdStub
} from './TaxiClassesConfig';
import { ITaxiClassesProps } from './types';

const TaxiClasses: React.FC<ITaxiClassesProps> = observer(
  ({
    tariffsInfo,
    value = {},
    onChange,
    isEditable,
    form,
    purpose,
    tripLimit,
    isExternal,
    externalPrices,
    externalTaxiClass,
    availableTaxiClasses,
    setProcess,
  }): JSX.Element => {
    const { [StoreNames.limitsStore]: limitsStore, [StoreNames.transportTypesStore]: transportTypesStore }
      = useAppStoreContext();

    const [showCarToolTrip, setShowCarToolTrip] = useState<number | null>(null);
    const { bonuses } = limitsStore;

    useEffect(() => {
      limitsStore.getAccountBonuses();
    }, [limitsStore]);

    const itemsWhichMayBeShown = useMemo(() => {
      const items: ITaxi[] = [];
      let hasBus = false;
      const availableTransportTypes = [...transportTypesStore.availableTransportTypes];
      Object.values(TransportTypesConfig).forEach(item => {
        const isBus = busClasses.includes(item.taxiClass as TaxiClassEnum);
        const isAvailable = availableTransportTypes.some(transportType => transportType.name === item.transportType);
        if (isBus) {
          hasBus = true;
        } else if (isAvailable) {
          items.push(item);
        }
      });
      if (hasBus) {
        items.push(busConfig);
      }
      return items;
    }, [transportTypesStore.availableTransportTypes]);

    const externalClasses = Object.values(ExternalClassesConfig);

    const clearNestedRadio = () => form?.setFieldsValue({
      [`externalPrices_${TaxiClassEnum.ECONOMY}`]: undefined,
      [`externalPrices_${TaxiClassEnum.COMFORT}`]: undefined,
      [`externalPrices_${TaxiClassEnum.COMFORT_PLUS}`]: undefined,
      [`externalPrices_${TaxiClassEnum.BUSINESS}`]: undefined,
    });

    const onTransportChange = (event: RadioChangeEvent): void => {
      clearNestedRadio();

      if (onChange) {
        onChange(event.target.value);
      }
    };

    const classes = isExternal ? externalClasses : itemsWhichMayBeShown;
    const findTariffs = useCallback(
      (option: ITaxi): (undefined | ITripTariff)[] => {
        if (isExternal) {
          return [undefined]; // show tariff in list for external provider
        }

        if (!tariffsInfo.length) {
          return [undefined]; // no tariffs have been received, show tariff in list with no data
        }

        const { taxiClass, transportType } = option;
        if (taxiClass === TaxiClassEnum.BUS) {
          const busTariffs = tariffsInfo.filter(item => busClasses.includes(item.taxiClass as TaxiClassEnum));
          return busTariffs.length
            ? [
              busTariffs.reduce((prev, curr) => {
                return {
                  ...prev,
                  id: busIdStub,
                  cost: Math.min(prev.cost, curr.cost),
                };
              }),
            ]
            : [];
        }
        return tariffsInfo.filter(item => {
          if (item.transportType.name !== TransportTypeEnum.TAXI) {
            return item.transportType.name === transportType;
          }
          return item.taxiClass === taxiClass;
        });
      },
      // eslint-disable-next-line react-hooks/exhaustive-deps
      [tariffsInfo, isExternal, externalTaxiClass]
    );

    return (
      <Radio.Group
        className={styles.wrapper__ant_radio_button_wrapper}
        value={value}
        onChange={onTransportChange}
      >
        <TaxiToolTripPortal top={showCarToolTrip} />
        <div className={classNames('with-hor-scroll-bar')}>
          {classes.map(option => findTariffs(option).map(tariff => (
            <TaxiClassCardWrapper
              key={`${option.name}-${externalTaxiClass}_${tariff?.id}`}
              option={option}
              tariff={tariff}
              tariffsInfo={tariffsInfo}
              externalPrices={externalPrices}
              isEditable={isEditable}
              form={form}
              purpose={purpose ?? ''}
              tripLimit={tripLimit}
              isExternal={isExternal}
              externalTaxiClass={externalTaxiClass}
              economyTaxiCost={tariffsInfo.find(t => t.taxiClass === 'ECONOMY')?.cost}
              setShowCarToolTrip={setShowCarToolTrip}
              availableTaxiClasses={availableTaxiClasses}
              bonuses={bonuses}
              setProcess={setProcess}
            />
          ))
          )}
        </div>
      </Radio.Group>
    );
  }
);

export default TaxiClasses;
