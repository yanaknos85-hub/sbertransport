import React, { FC } from 'react';
import { Button, Space } from 'antd';
import classNames from 'classnames';
import { emptySign } from 'shared/constants/constants';
import { getTypePointLabel } from 'utils';

import { formatterPhoneNumber } from 'modules/CargoMultiple/utils';

import { Contacts, WaypointExchange } from '../../types';

import styles from './styles.module.scss';

interface Props {
  waypoints: WaypointExchange[];
}

const AddressBlockMultipleDetailed: FC<Props> = ({ waypoints }): JSX.Element => {
  const [additionalAddressesVisible, setAdditionalAddressesVisible] = React.useState(false);

  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };
  const getAddressLetterExpanded = (index: number): string => String.fromCharCode(97 + index).toUpperCase();

  const getAddressLetter = (index: number): string => getAddressLetterExpanded(index);

  const firstAddressBlock = (address: string, type: string, organization: string, contacts: Contacts[]): JSX.Element => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressTypePoint}>{type}</span>
          <span className={styles.addressFullName}>
            {contacts.filter(Boolean).map(contact => (
              <span key={contact.requests[0].humanReadableId}>
                {contact.contact.name}
                <span className={styles.contact}>{formatterPhoneNumber(contact.contact.phone)}</span>
              </span>
            ))}
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
    contacts: Contacts[]
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
            {contacts.filter(Boolean).map(contact => (
              <span key={contact.requests[0].humanReadableId}>
                {contact.contact.name}
                <span className={styles.contact}>{formatterPhoneNumber(contact.contact.phone)}</span>
              </span>
            ))}
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
    contacts: Contacts[]
  ): React.ReactNode => {
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
          key={x.address.coordinates.latitude + x.address.coordinates.longitude}
          title={x.address.addressStringRepresentation}
        >
          {switchAddressBlock(
            index,
            array.length,
            x.address.addressStringRepresentation,
            getTypePointLabel(x.type!),
            x?.contacts[0].requests[0].organization || '',
            x?.contacts || []
          )}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockMultipleDetailed;
