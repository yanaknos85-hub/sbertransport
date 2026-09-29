import { Space, Tooltip  } from 'antd';
import classNames from 'classnames';
import React, { ReactNode, useState, FC } from 'react';
import uuid from 'utils/uuid';
import { getAddressLetter } from "utils/getAddressLetter";

import { OrderWaypointType, RouteWaypointType } from '../../types';
import { declension } from '../utils';

import * as S from './AddressBlockDetailed.styles_New';
import styles from './styles.module.scss';

interface Props {
  waypoints: RouteWaypointType[] | OrderWaypointType[];
}

export const AddressBlockDetailed_New: FC<Props> = props => {
  const { waypoints } = props;

  const [additionalAddressesVisible, setAdditionalAddressesVisible] = useState(false);

  const sortingType = (
    obj1: RouteWaypointType | OrderWaypointType,
    obj2: RouteWaypointType | OrderWaypointType
  ): number => {
    if ('orderingIndex' in obj1 && 'orderingIndex' in obj2) {
      return obj1.orderingIndex - obj2.orderingIndex;
    }
    return 0;
  };

  const orderingWaypoints = Array.isArray(waypoints) && waypoints.length > 0 && 'orderingIndex' in waypoints[0] ? waypoints.slice().sort(sortingType) : waypoints;
  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };

  const firstAddressBlock = (address: string): ReactNode => (
    <S.AddressMainDivDetailed>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <Tooltip title={address}>
        <S.AddressDiv>
          {address}
        </S.AddressDiv>
      </Tooltip>
    </S.AddressMainDivDetailed>
  );

  const secondAddressBlockCollapsed = (index: number, length: number): ReactNode => (
    <S.AddressMainDivDetailed>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
      </div>
      <S.AddressDiv>
        <div className={classNames(styles.addressString2, styles.addressStringCollapsed)}>
          {`${length - 1} ${declension(length - 1)}`}
          <S.ShowAdditionalAddressesButton type="link" onClick={showAdditionalAddressesHandle}>
            Показать
          </S.ShowAdditionalAddressesButton>
        </div>
      </S.AddressDiv>
    </S.AddressMainDivDetailed>
  );

  const otherAddressBlocks = (index: number, length: number, address: string, lastFlag: boolean,): ReactNode => (
    <S.AddressMainDivDetailed>
      <div className={lastFlag ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>
        {getAddressLetter(index)}
        </div>
      <Tooltip title={address}>
        <S.AddressDiv>
          {address}
        </S.AddressDiv>
      </Tooltip>
    </S.AddressMainDivDetailed>
  );

  const switchAddressBlock = (index: number, length: number, address: string): ReactNode => {
    if (index === 0) {
      return firstAddressBlock(address);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length-1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(index, length, address, true);
      }
      return otherAddressBlocks(index, length, address, false);
    }
    if (length === 2) {
      return otherAddressBlocks(index, length, address, true);
    }
    return <Space />;
  };

  const renderListAddresses = ([...list]: RouteWaypointType[] | OrderWaypointType[]) => list.map((x, index, array) => {
    if ('address' in x) {
      const routeAddress = `${x?.address.street} ${x?.address.house}`;
      return (
        <S.Address key={uuid()} title={routeAddress}>
           {switchAddressBlock(index, array.length, x?.addressStringRepresentation ?? '')}
        </S.Address>
      );
    }

    const orderAddress = `${x.city}, ${x.street}, ${x.house}`;
    return (
      <S.Address key={uuid()} title={orderAddress}>
        {switchAddressBlock(index, array.length, x?.addressStringRepresentation ?? '')}
      </S.Address>
    );
  });

  return (
    <S.AddressInfo>
      <S.AddressMainDiv>
        {additionalAddressesVisible ? (
          <S.HideAdditionalAddressesButton type="link" onClick={showAdditionalAddressesHandle}>
            Скрыть промежуточные
          </S.HideAdditionalAddressesButton>
        ) : (
          <Space />
        )}
      </S.AddressMainDiv>
      {renderListAddresses(orderingWaypoints)}
    </S.AddressInfo>
  );
};
