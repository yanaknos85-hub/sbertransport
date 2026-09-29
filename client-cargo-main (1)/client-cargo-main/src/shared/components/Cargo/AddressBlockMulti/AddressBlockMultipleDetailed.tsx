import React, { FC, useState } from 'react';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Button, Space } from 'antd';
import classNames from 'classnames';
import { emptySign } from 'shared/constants/constants';
import { Contact, WaypointModel } from 'shared/models/geo/Waypoint.model';
import { getTypePointLabel } from 'utils';

import { ContactBlock } from './ContactBlock';

import styles from './styles.module.scss';

const AddressBlockMultipleDetailed: FC<any> = ({
  waypoints,
}: {
  waypoints: WaypointModel[];
  employees: EmployeeModel[];
  senderOrganization: string;
  recipientOrganization: string;
}) => {
  const [additionalAddressesVisible, setAdditionalAddressesVisible] = useState(false);

  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };
  const getAddressLetterExpanded = (index: number): string => String.fromCharCode(97 + index).toUpperCase();

  const getAddressLetter = (index: number): string => getAddressLetterExpanded(index);

  const firstAddressBlock = (address: string, type: string, organization: string, contacts: Contact[]): JSX.Element => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressTypePoint}>{type}</span>
          <span className={styles.addressFullName}>
            <ContactBlock contacts={contacts.filter(Boolean)} />
          </span>
          <span className={styles.addressOrganization}>{organization}</span>
        </div>
      </div>
    </div>
  );

  const secondAddressBlockCollapsed = (index: number, length: number): JSX.Element => (
    <div className={styles.addressMainDiv}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
      </div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
      <div className={classNames(styles.addressDiv)}>
        <div className={classNames(styles.addressString2, styles.addressStringCollapsed)}>
          {length - 1}
          {' '}
          адреса
          {' '}
          <Button
            className={styles.showAdditionalAddressesButton}
            type="link"
            onClick={showAdditionalAddressesHandle}
          >
            Показать
          </Button>
        </div>
      </div>
    </div>
  );

  const otherAddressBlocks = (
    index: number,
    length: number,
    address: string,
    type: string,
    organization: string,
    lastFlag: boolean,
    contacts: Contact[]
  ): JSX.Element => (
    <div className={styles.addressMainDivDetailed}>
      <div className={lastFlag ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>
        {getAddressLetterExpanded(index)}
      </div>
      <div
        className={lastFlag ? classNames(styles.addressCircleSpacerLast) : classNames(styles.addressCircleSpacer2)}
      />
      <div className={classNames(styles.addressDiv)}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressTypePoint}>{type}</span>
          <span className={styles.addressContactName}>
            <ContactBlock contacts={contacts.filter(Boolean)} />
          </span>
          <span className={styles.addressOrganization}>{organization}</span>
        </div>
      </div>
    </div>
  );

  const switchAddressBlock = (
    index: number,
    length: number,
    address = emptySign,
    type: string,
    organization: string,
    contacts: Contact[]
  ) => {
    if (index === 0) {
      return firstAddressBlock(address, type, organization, contacts);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length - 1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(index, length, address, type, organization, true, contacts);
      }
      return otherAddressBlocks(index, length, address, type, organization, false, contacts);
    }
    if (length === 2) {
      return otherAddressBlocks(index, length, address, type, organization, true, contacts);
    }
    return <Space />;
  };

  return (
    <div>
      <div className={styles.addressMainDiv}>
        <div className={styles.addressTitle}>Маршрут</div>
        {additionalAddressesVisible ? (
          <Button
            className={styles.hideAdditionalAddressesButton}
            type="link"
            onClick={showAdditionalAddressesHandle}
          >
            Скрыть промежуточные
          </Button>
        ) : (
          <Space />
        )}
      </div>
      {waypoints?.map((x, index, array) => (
        <div
          className={styles.address}
          key={x.latitude + x.longitude}
          title={x.addressStringRepresentation}
        >
          {switchAddressBlock(
            index,
            array.length,
            x.addressStringRepresentation,
            getTypePointLabel(x.type!),
            x?.organization || '',
            x?.contacts || []
          )}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockMultipleDetailed;
