import React from 'react';
import type { FC } from 'react';
import { Divider } from 'antd';
import { Input } from '@sber-sbertransport/ui-kit/src';

import { useTranslation } from 'i18n';
import Panel from 'components/Panel/Panel';
import { FormItem } from 'components/FormItem';
import { DispatcherOrganizationField } from '../../../Waybill.constants';

import styles from './OwnerSection.module.scss';

const OwnerSection: FC = () => {
  const { owner: i18 } = useTranslation().t.Waybill.create;

  return (
    <Panel className={styles.container}>
      <span className={styles.container__title}>{i18.title}</span>

      <Divider className={styles.container__divider} />

      <div className={styles.container__fields}>
        <FormItem name={DispatcherOrganizationField.name} label={i18.organizationName}>
          <Input disabled />
        </FormItem>

        <FormItem name={DispatcherOrganizationField.regionCode} label={i18.subjectCode}>
          <Input disabled />
        </FormItem>

        <FormItem name={DispatcherOrganizationField.phone} label={i18.phone}>
          <Input disabled />
        </FormItem>

        <FormItem name={DispatcherOrganizationField.ogrn} label={i18.ogrn}>
          <Input disabled />
        </FormItem>

        <FormItem name={DispatcherOrganizationField.tin} label={i18.tin}>
          <Input disabled />
        </FormItem>
      </div>
    </Panel>
  );
};

export default OwnerSection;
