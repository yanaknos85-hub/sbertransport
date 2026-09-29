import { Space, Tooltip } from 'antd';
import classNames from 'classnames';
import React, { FC, useState } from 'react';
import uuid from 'utils/uuid';
import { getAddressLetter } from "utils/getAddressLetter";

import {
  OrderWaypointType,
  RouteWaypointType,
  OrderWaypointMinimalType
} from '../../types';

import styles from './styles.module.scss';
import * as S from './AddressBlockDetailed.styles';

interface Props {
  waypoints: RouteWaypointType[] | OrderWaypointType[] | OrderWaypointMinimalType[];
}

export const AddressBlockDetailed: FC<Props> = props => {
  const { waypoints } = props;

  const [additionalAddressesVisible, setAdditionalAddressesVisible] = useState(false);

  const sortingType = (
    obj1: RouteWaypointType | OrderWaypointType | OrderWaypointMinimalType,
    obj2: RouteWaypointType | OrderWaypointType | OrderWaypointMinimalType
  ): number => {
    if ('orderingIndex' in obj1 && 'orderingIndex' in obj2) {
      return (obj1).orderingIndex - (obj2).orderingIndex;
    }
    return 0;
  };

  const orderingWaypoints = Array.isArray(waypoints) && waypoints.length > 0 && 'orderingIndex' in waypoints[0] ? waypoints.slice().sort(sortingType) : waypoints;
  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };

  const declension = (number: number): string => {
    const words = ['адрес', 'адреса', 'адресов'];
    return words[
      number % 100 > 4 && number % 100 < 20 ? 2 : [2, 0, 1, 1, 1, 2][number % 10 < 5 ? Math.abs(number) % 10 : 5]
    ];
  };

  const truncatedAddress = (address: string): string => address.length > 45 ? address.substring(0, 45) + '...' : address;

  const firstAddressBlock = (address: string): JSX.Element => (
    <S.AddressMainDivDetailed>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <Tooltip title={address}>
        <S.AddressDiv>
          <S.AddressString2>
            <span>{truncatedAddress(address)}</span>
          </S.AddressString2>
        </S.AddressDiv>
      </Tooltip>
    </S.AddressMainDivDetailed>
  );

  const secondAddressBlockCollapsed = (index: number, length: number): JSX.Element => (
    <S.AddressMainDivDetailed>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
      </div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
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

  const otherAddressBlocks = (index: number, length: number, address: string, lastFlag: boolean): JSX.Element => (
    <S.AddressMainDivDetailed>
      <div className={lastFlag ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>
        {getAddressLetter(index)}
      </div>
      <div className={lastFlag ? classNames(styles.addressCircleSpacerLast) : classNames(styles.addressCircleSpacer2)} />
      <Tooltip title={address}>
        <S.AddressDiv>
          <span>{truncatedAddress(address)}</span>
        </S.AddressDiv>
      </Tooltip>
    </S.AddressMainDivDetailed>
  );

  const switchAddressBlock = (index: number, length: number, address: string, lastFlag: boolean): JSX.Element => {
    if (index === 0) {
      return firstAddressBlock(address);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length-1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      return otherAddressBlocks(index, length, address, length - 1 === index);
    }
    if (length === 2) {
      return otherAddressBlocks(index, length, address, true);
    }
    return <Space />;
  };

  const renderListAddresses = ([...list]: RouteWaypointType[] | OrderWaypointType[] | OrderWaypointMinimalType[]) => list.map((x, index, array) => {
    if ('address' in x) {
      const routeAddress = `${x?.address.street} ${x?.address.house}`;
      return (
        <S.Address key={uuid()} title={routeAddress}>
           {switchAddressBlock(index, array.length, x?.addressStringRepresentation ?? '', true)}
        </S.Address>
      );
    }

    return (
      <S.Address key={uuid()} title={x.addressStringRepresentation}>
        {switchAddressBlock(index, array.length, x?.addressStringRepresentation ?? '', true)}
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
