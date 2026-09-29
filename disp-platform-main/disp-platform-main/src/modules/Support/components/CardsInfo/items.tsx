import React, { ReactNode } from 'react';
import { Link } from '@sber-sbertransport/ui-kit/src';

import { Contacts, SDOContacts } from 'constants/app.constants';

import SupportChat from 'assets/images/support-chat.png';
import SupportContact from 'assets/images/support-contact.png';
import SupportMail from 'assets/images/support-mail.png';

interface CardProps {
  isAvailable: boolean;
  icon: ReactNode;
  children: {
    isAvailable: boolean;
    title: string;
    link: ReactNode;
  }[];
}

export const getItems = (locales: Record<string, string>, isSDO: boolean): CardProps[] => [
  {
    isAvailable: true,
    icon: <img
      src={SupportContact}
      alt="support-chat.icon"
      height={226}
          />,
    children: [
      {
        isAvailable: true,
        title: isSDO ? locales.contactNumber : locales.outTel,
        link: (
          <Link href={isSDO ? SDOContacts.linkTel : Contacts.linkOutTel}>
            {isSDO ? SDOContacts.tel : Contacts.outTel}
          </Link>
        ),
      },
      {
        isAvailable: !isSDO,
        title: locales.insideTel,
        link: (
          <Link target="none" href={Contacts.linkInsideTel}>
            {Contacts.insideTel}
          </Link>
        ),
      },
    ],
  },
  {
    isAvailable: !isSDO,
    icon: <img
      src={SupportMail}
      alt="support-contact.icon"
      height={226}
          />,
    children: [
      {
        isAvailable: !isSDO,
        title: locales.supportChat,
        link: (
          <Link
            target="_blank"
            href={Contacts.linkChatSbertransport}
            rel="noopener noreferrer"
          >
            {Contacts.chatSbertransport}
          </Link>
        ),
      },
    ],
  },
  {
    isAvailable: true,
    icon: <img
      src={SupportChat}
      alt="support-mail.icon"
      height={140}
          />,
    children: [
      {
        isAvailable: true,
        title: locales.supportMail,
        link: (
          <Link
            target="_blank"
            href={isSDO ? SDOContacts.linkMailSbertransport : Contacts.linkMailSbertransport}
            rel="noopener noreferrer"
          >
            {isSDO ? SDOContacts.mailSbertransport : Contacts.mailSbertransport}
          </Link>
        ),
      },
    ],
  },
];
