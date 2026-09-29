import React, { FC } from 'react';

interface Contact {
  fullName?: string;
  mobilePhone?: string;
}

interface ContactItemProps {
  contact: Contact;
}

export const ContactItem: FC<ContactItemProps> = ({ contact }) => {
  const name = contact?.fullName || '';
  const phone = contact?.mobilePhone || '';

  return (
    <div>
      {name}
      {phone && (
      <div>
        Тел:
        {phone}
      </div>
      )}
    </div>
  );
};
export default ContactItem;
