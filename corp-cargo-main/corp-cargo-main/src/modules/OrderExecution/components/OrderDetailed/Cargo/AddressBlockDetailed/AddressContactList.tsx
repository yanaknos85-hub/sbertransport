import React, { FC } from 'react';
import { Button, Tooltip } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { ContactType } from 'modules/Planner/types';

import styles from '../styles.module.scss';

export const AddressContactList: FC<{
  contacts: ContactType;
  onAddAdditionalContact?: (waypointId: string) => void;
  waypointId: string;
  nameVariant: 'fullName' | 'name';
  disabled?: boolean;
  titleTooltip?: string;
}> = ({
   contacts,
   onAddAdditionalContact,
   waypointId,
   nameVariant,
   disabled,
   titleTooltip
}) => {

  if (!contacts || contacts.length === 0) {
    return null;
  }

  const tooltipContent = contacts.map((contact, index) => (
    <div key={index}>
      <div>{contact.fullName}</div>
      <div>{formatPhoneNumber(contact.mobilePhone)}</div>
    </div>
  ));

  return (
    <div className={styles.addressContactWrapper}>
      <span className={nameVariant === 'fullName' ? styles.addressFullName : styles.addressContactName}>
        {contacts.length > 1 ? (
          <Tooltip placement="topLeft" title={tooltipContent}>
            <span className={styles.contactFirst}>
              {contacts[0].fullName}
              <span className={styles.contact}>{formatPhoneNumber(contacts[0].mobilePhone)}</span>
              <span className={styles.ellipsis}>...</span>
            </span>
          </Tooltip>
        ) : (
          <span className={styles.contactFirst}>
            {contacts[0]?.fullName}
            <span className={styles.contact}>{formatPhoneNumber(contacts[0]?.mobilePhone)}</span>
          </span>
        )}
      </span>
      {onAddAdditionalContact && (
        <Tooltip title={titleTooltip || ''}>
          <Button
            type="primary"
            shape="circle"
            icon={<PlusOutlined />}
            size="small"
            disabled={disabled}
            onClick={() => onAddAdditionalContact?.(waypointId)}
            className={disabled ? styles.addContactButton : `${styles.addContactButton} ${styles.addContactButtonWithMargin}`}
          />
        </Tooltip>
      )}
    </div>
  );
};
