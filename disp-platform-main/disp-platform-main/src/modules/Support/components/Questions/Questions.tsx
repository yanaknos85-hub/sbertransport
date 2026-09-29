import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import Accordion from 'components/Accordion/Accordion';
import { Contacts, SDOContacts } from 'constants/app.constants';

import styles from './Questions.module.scss';

const Questions: FC<{ isSDO: boolean }> = ({ isSDO }) => {
  const { titles, popularQuestions } = useTranslation().t.Support;

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>{titles.questions}</h3>
      <div>
        <Accordion data={popularQuestions} />
      </div>
      <h3>
        Мы собираем популярные вопросы. Отправьте нам свой на
        {' '}
        <b>{isSDO ? SDOContacts.mailSbertransport : Contacts.mailSbertransport}</b>
        .
      </h3>
    </div>
  );
};

export default Questions;
