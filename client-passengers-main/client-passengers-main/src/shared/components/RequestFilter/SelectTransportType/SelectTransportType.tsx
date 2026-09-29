import React, { FC } from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportPicture } from 'modules/EmployeeApp/components/TaxiClasses/Card/components';

import styles from './styles.module.scss';

export interface SelectTransportTypeProps {
  lable: React.ReactNode;
  type: TransportTypeEnum;
  changeSelectedTransport: (type: string) => void;
  selectedTransport: string | undefined;
  _setTransportType: (value: TransportTypeEnum) => void;
}

export const SelectTransportType: FC<SelectTransportTypeProps> = ({
  lable, type, changeSelectedTransport, selectedTransport, _setTransportType,
}) => {
  const handleClick = (type: TransportTypeEnum) => {
    changeSelectedTransport(type);
    _setTransportType(type);
  };

  return (
    <div className={selectedTransport === type ? styles.OnwrapperTransportType : styles.wrapperTransportTupe} onClick={() => handleClick(type)}>
      {lable !== 'Все'
      && (
      <div className={styles.TransportPicture}>
        <TransportPicture transportType={type} />
      </div>
      )}

      <span>
        {lable}
      </span>
    </div>
  );
};
