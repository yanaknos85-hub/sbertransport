import React, { useEffect, useState } from 'react';
import type { FC } from 'react';
import { Divider } from 'antd';
import { useTranslation } from 'i18n';
import { FormInstance, Input } from '@sber-sbertransport/ui-kit/src';

import type { UUID } from 'utils/io-ts';

import { TransportField } from '../../../Waybill.constants';
import { StateNumberSearchOne } from 'api/telemechanicTransport/telemechanicTransport.types';
import Panel from 'components/Panel/Panel';
import { FormItem } from 'components/FormItem';
import TransportSelect from './TransportSelect';

import styles from './TransportSection.module.scss';

interface Props {
  disabled: boolean;
  form: FormInstance;
  changeTransportId: (value: UUID | null) => void;
}

const TransportSection: FC<Props> = ({
  disabled,
  form,
  changeTransportId,
}) => {
  const { transport: i18 } = useTranslation().t.Waybill.create;

  const [transport, setTransport] = useState<StateNumberSearchOne | null>(null);

  useEffect(() => {
    if (transport) {
      form.setFieldsValue({
        [TransportField.transportId]: transport.id,
        [TransportField.brand]: transport.brand,
        [TransportField.model]: transport.model,
        [TransportField.transportType]: transport.transportType,
      });
    } else {
      form.resetFields([
        TransportField.brand,
        TransportField.model,
        TransportField.transportType,
        TransportField.transportId,
      ]);
    }

    changeTransportId(transport?.id ?? null);
  }, [changeTransportId, form, transport]);

  return (
    <Panel className={styles.container}>
      <span className={styles.container__title}>{i18.title}</span>

      <Divider className={styles.container__divider} />

      <div className={styles.container__content}>
        <FormItem label={i18.stateNumber.label}>
          <TransportSelect
            placeholder={i18.stateNumber.placeholder}
            disabled={disabled}
            onChange={(value, option) => setTransport(option as StateNumberSearchOne)}
          />
        </FormItem>

        <FormItem name={TransportField.brand} label={i18.brand}>
          <Input disabled className={styles.field} />
        </FormItem>

        <FormItem name={TransportField.model} label={i18.model}>
          <Input disabled className={styles.field} />
        </FormItem>

        <FormItem name={TransportField.transportType} label={i18.transportType}>
          <Input disabled className={styles.field} />
        </FormItem>

        <FormItem name={TransportField.transportId} noStyle />
      </div>
    </Panel>
  );
};

export default TransportSection;
