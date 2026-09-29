/* eslint-disable no-unused-expressions */
import * as React from 'react';
import { useState } from 'react';
import { Radio, RadioChangeEvent } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { TaxiToolTripPortal } from './Card/CostToolTip';
import TaxiClassCardWrapper from './TaxiClassCardWrapper';
import { ExternalClassesConfig, TransportTypesConfig } from './TaxiClassesConfig';
import { ITaxiClassesProps } from './types';

import styles from './taxiClasses.module.scss';

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
  }): JSX.Element => {
    const { [StoreNames.transportTypesStore]: transportTypesStore } = useAppStoreContext();
    const [showCarToolTrip, setShowCarToolTrip] = useState<number | null>(null);

    const availableTransportTypes = [...transportTypesStore.availableTransportTypes];

    const taxiClasses = Object.values(TransportTypesConfig).filter(taxiClassConfig => availableTransportTypes.some(transportType => transportType.name === taxiClassConfig.transportType)
    );

    const externalClasses = Object.values(ExternalClassesConfig);

    const clearNestedRadio = () => {
      form?.setFieldsValue({ [`externalPrices_${TaxiClassEnum.ECONOMY}`]: undefined });
      form?.setFieldsValue({ [`externalPrices_${TaxiClassEnum.COMFORT}`]: undefined });
      form?.setFieldsValue({ [`externalPrices_${TaxiClassEnum.BUSINESS}`]: undefined });
    };

    const onTransportChange = (event: RadioChangeEvent): void => {
      clearNestedRadio();

      if (onChange) {
        onChange(event.target.value);
      }
    };

    const classes = isExternal ? externalClasses : taxiClasses;

    return (
      <Radio.Group
        className={styles.wrapper__ant_radio_button_wrapper}
        value={value}
        onChange={onTransportChange}
      >
        <TaxiToolTripPortal top={showCarToolTrip} />
        <div className={classNames('with-hor-scroll-bar')}>
          {classes.map(option => (
            <TaxiClassCardWrapper
              key={`${option.name}-${externalTaxiClass}`}
              option={option}
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
            />
          ))}
        </div>
      </Radio.Group>
    );
  }
);

export default TaxiClasses;
