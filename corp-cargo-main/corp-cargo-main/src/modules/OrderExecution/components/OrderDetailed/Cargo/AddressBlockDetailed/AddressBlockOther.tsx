import React, { FC } from 'react';
import classNames from 'classnames';
import { AddressContactList } from './AddressContactList';
import { ContactType } from 'modules/Planner/types';

import styles from '../styles.module.scss';
import { getAddressLetter } from "utils/getAddressLetter";

interface Props {
  index: number;
  address: string;
  type: string;
  organization: string;
  isLast: boolean;
  contacts: ContactType;
  waypointId: string;
  onAddAdditionalContact?: (waypointId: string) => void;
  disabled?: boolean;
  titleTooltip?: string;
}

export const AddressBlockOther: FC<Props> = ({
  index,
  address,
  type,
  organization,
  isLast,
  contacts,
  waypointId,
  onAddAdditionalContact,
  disabled,
  titleTooltip,
}) => {
  return (
    <div className={styles.addressMainDivDetailed}>
      <div className={isLast ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>{getAddressLetter(index)}</div>
      <div className={isLast ? styles.addressCircleSpacerLast : styles.addressCircleSpacer2} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressTypePoint}>{type}</span>
          <AddressContactList
            contacts={contacts}
            onAddAdditionalContact={onAddAdditionalContact}
            waypointId={waypointId}
            nameVariant="name"
            disabled={disabled}
            titleTooltip={titleTooltip}
          />
          <span>{organization}</span>
        </div>
      </div>
    </div>
  );
};
