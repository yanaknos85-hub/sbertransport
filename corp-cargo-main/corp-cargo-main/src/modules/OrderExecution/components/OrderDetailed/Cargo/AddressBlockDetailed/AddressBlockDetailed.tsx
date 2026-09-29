import React, { FC, useState } from 'react';
import { Button, Space } from 'antd';
import {
  AddressType,
  OrderWaypointType,
  RouteWaypointType,
  RouteAddressType,
} from 'modules/Planner/types';

import { ContactType } from "../../../../types";
import { AddressBlockFirst } from './AddressBlockFirst';
import { AddressBlockOther } from './AddressBlockOther';
import { AddressBlockCollapsed } from './AddressBlockCollapsed';

import styles from '../styles.module.scss';

interface Props {
  waypoints: RouteWaypointType[] | OrderWaypointType[];
  onAddAdditionalContact?: (waypointId: string) => void;
  disabled?: boolean;
  titleTooltip?: string;
}

export const AddressBlockDetailed: FC<Props> = props => {
  const { waypoints, onAddAdditionalContact, disabled, titleTooltip } = props;

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

  const orderingWaypoints =
    Array.isArray(waypoints) && waypoints.length > 0 && 'orderingIndex' in waypoints[0]
      ? waypoints.sort(sortingType)
      : waypoints;

  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };

  const getAddressString = (address: RouteAddressType | AddressType): string =>
    `${address?.city}, ${address?.street}, ${address?.house}`;

  const renderWaypointBlock = (
    x: RouteWaypointType | OrderWaypointType,
    index: number,
    length: number
  ): JSX.Element | null => {
    const waypointId = x.id;
    const contacts = (x?.contacts || []) as ContactType;

    const getWaypointAddress = (): string => {
      if ('address' in x) return getAddressString(x.address);
      return x.addressStringRepresentation || '';
    };

    const getWaypointType = (): string => (x.type ? x.type : '');

    const getOrganization = (): string => x?.organization || '';

    const isLast = index === length - 1;

    // Первый адрес
    if (index === 0) {
      return (
        <AddressBlockFirst
          address={getWaypointAddress()}
          type={getWaypointType()}
          organization={getOrganization()}
          contacts={contacts}
          waypointId={waypointId}
          onAddAdditionalContact={onAddAdditionalContact}
          disabled={disabled}
          titleTooltip={titleTooltip}
        />
      );
    }

    // Второй адрес — свёрнутый (если больше 2 адресов и не развёрнуто)
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return (
        <AddressBlockCollapsed
          count={length - 2}
          lastIndex={length - 1}
          onShow={showAdditionalAddressesHandle}
        />
      );
    }

    // Развёрнутые адреса или всего 2 адреса
    if (length === 2 || additionalAddressesVisible) {
      return (
        <AddressBlockOther
          index={index}
          address={getWaypointAddress()}
          type={getWaypointType()}
          organization={getOrganization()}
          isLast={isLast}
          contacts={contacts}
          waypointId={waypointId}
          onAddAdditionalContact={onAddAdditionalContact}
          disabled={disabled}
          titleTooltip={titleTooltip}
        />
      );
    }

    return null;
  };

  const renderListAddresses = ([...list]: RouteWaypointType[] | OrderWaypointType[]) =>
    list.map((x, index, array) => {
      const addressTitle = 'address' in x ? `${x?.address.street} ${x?.address.house}` : `${x.city}, ${x.street}, ${x.house}`;

      return (
        <div className={styles.address} key={x.id} title={addressTitle}>
          {renderWaypointBlock(x, index, array.length)}
        </div>
      );
    });

  return (
    <div className={styles.addressInfo}>
      <div className={styles.addressMainDiv}>
        <div className={styles.addressTitle}>Адреса</div>
        {additionalAddressesVisible ? (
          <Button className={styles.hideAdditionalAddressesButton} type="link" onClick={showAdditionalAddressesHandle}>
            Скрыть промежуточные
          </Button>
        ) : (
          <Space />
        )}
      </div>
      {renderListAddresses(orderingWaypoints)}
    </div>
  );
};
