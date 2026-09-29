import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { Contact } from 'shared/models/geo/Waypoint.model';

import { formatterPhoneNumber } from 'modules/CargoMultiple/utils';

import ContactItem from './ContactItem';

import styles from './styles.module.scss';

interface ContactBlockProps {
  contacts: Contact[];
}

export const ContactBlock: FC<ContactBlockProps> = ({ contacts = [] }) => {
  if (!contacts || contacts.length === 0) return null;

  const firstContact = contacts[0];

  if (contacts.length > 1) {
    return (
      <Tooltip
        placement="topLeft"
        title={(
          <div>
            {contacts.map((c, i) => (
              <div key={c.id} className={styles.tooltipContact}>
                {i + 1}
                .
                {c.fullName}
                {c.mobilePhone && <span className={styles.contact}>{formatterPhoneNumber(c.mobilePhone)}</span>}
              </div>
            ))}
          </div>
        )}
      >
        <div>
          <ContactItem contact={firstContact} />
          <span className={styles.contact}>...</span>
        </div>
      </Tooltip>
    );
  }

  return <ContactItem contact={firstContact} />;
};

export default ContactBlock;
