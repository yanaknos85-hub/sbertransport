import React from 'react';

import { LimitResponsible } from 'stores/Limits/Limit.interface';

import Service from 'shared/images/service.png';
import { ReactComponent as MailIcon } from 'shared/icons/mail.svg';

import {
  Container, ResponsiblesStyled, Title, Responsible, Name, Email,
  ImageStyled
} from './Responsibles.styled';

interface ResponsiblesProps {
  responsibles: LimitResponsible[];
}

const getResponsibleFullName = (responsible: LimitResponsible) => (
  [responsible.firstName, responsible?.lastName].filter(Boolean).join(' ')
);

export const Responsibles: React.FC<ResponsiblesProps> = ({ responsibles }) => {
  if (!responsibles?.length) return null;

  return (
    <Container>
      <ResponsiblesStyled>
        <Title>Ответсвенные за попопление лимита</Title>
        {responsibles.map(responsible => (
          <Responsible key={responsible.id}>
            <Name>
              {getResponsibleFullName(responsible)}
            </Name>
            <Email
              target="_blank"
              href={responsible.email}
              rel="noopener noreferrer"
            >
              <MailIcon />
              {responsible.email}
            </Email>
          </Responsible>
        ))}
      </ResponsiblesStyled>
      <ImageStyled src={Service} alt="" />
    </Container>
  );
};
