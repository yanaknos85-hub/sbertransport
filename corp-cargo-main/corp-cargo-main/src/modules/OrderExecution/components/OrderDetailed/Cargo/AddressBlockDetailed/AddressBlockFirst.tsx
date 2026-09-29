import React, { FC } from 'react';
import classNames from 'classnames';
import { AddressContactList } from './AddressContactList';
import { ContactType } from 'modules/Planner/types';

import styles from '../styles.module.scss';

interface Props {
  address: string;
  type: string;
  organization: string;
  contacts: ContactType;
  waypointId: string;
  onAddAdditionalContact?: (waypointId: string) => void;
  disabled?: boolean;
  titleTooltip?: string;
}

export const AddressBlockFirst: FC<Props> = ({
  address,
  type,
  organization,
  contacts,
  waypointId,
  onAddAdditionalContact,
  disabled,
  titleTooltip,
}) => (
  <div className={styles.addressMainDivDetailed}>
    <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
    <div className={styles.addressCircleSpacer1} />
    <div className={styles.addressDiv}>
      <div className={styles.addressString2}>
        <span>{address}</span>
        <span className={styles.addressTypePoint}>{type}</span>
        <AddressContactList
          contacts={contacts}
          onAddAdditionalContact={onAddAdditionalContact}
          waypointId={waypointId}
          nameVariant="fullName"
          disabled={disabled}
          titleTooltip={titleTooltip}
        />
        <span>{organization}</span>
      </div>
    </div>
  </div>
);
