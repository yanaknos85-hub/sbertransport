import React from 'react';
import type { FC } from 'react';
import { Divider, Row, Col } from 'antd';
import { useTranslation } from 'i18n';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/app.constants';
import type { Author } from 'api/waybill/waybill.types';
import Panel from 'components/Panel/Panel';
import FieldBlock from '../FieldBlock';

import styles from './Responsible.module.scss';

interface Props {
  author: Author;
}

const DetailedResponsible: FC<Props> = ({ author }) => {
  const { attorney } = author;
  const { responsible: i18 } = useTranslation().t.Waybill.detailed;

  const fullName = `${author.firstName} ${author.lastName} ${author.patronymic}`.trim();

  return (
    <Panel className={styles.panel}>
      <span className={styles.panel__header}>{i18.title}</span>

      <Divider className={styles.panel__divider} />

      <Row gutter={[10, 16]}>
        <Col span={6}>
          <FieldBlock
            title={i18.fields.fullName}
            value={fullName}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.phoneNumber}
            value={author.mobilePhone}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.attorneyNumber}
            value={attorney.attorneyNumber}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.issueDate}
            value={moment(attorney.issueDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.creationSystem}
            value={attorney.creationSystem}
          />
        </Col>
      </Row>
    </Panel>
  );
};

export default DetailedResponsible;
