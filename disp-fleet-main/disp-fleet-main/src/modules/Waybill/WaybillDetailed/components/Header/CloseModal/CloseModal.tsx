import React, { useState, type FC, type ChangeEvent } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Input, Modal } from 'antd';

import { useTranslation } from 'i18n';
import { useCloseEwb } from 'api/waybill/waybill.api';
import { Waybill } from 'api/waybill/waybill.types';
import * as routes from 'constants/routes.constants';
import { ignore } from 'utils/utils';
import { FormItem } from 'components/FormItem';

import styles from './CloseModal.module.scss';

interface Props {
  visible: boolean;
  onClose: () => void;
  waybill: Waybill;
}

const CloseModal: FC<Props> = ({
  visible,
  onClose,
  waybill,
}) => {
  const transport = waybill?.transport;

  const odometerDefault = transport?.odometerOut ?? 0;
  const litreageDefault = transport?.fuelLitreageOut ?? 0;
  const tankVolumeDefault = transport?.fuelTankVolume ?? Infinity;

  const history = useHistory();
  const { close: i18n } = useTranslation().t.Waybill.modal;

  const [close, { isLoading: isClosing }] = useCloseEwb();

  const [odometerValue, setOdometerValue] = useState(odometerDefault.toString());
  const [percentageValue, setPercentageValue] = useState(
    Math.round(litreageDefault / tankVolumeDefault * 100).toString()
  );
  const [litreageValue, setLitreageValue] = useState(litreageDefault);
  const calculatedLitreage = Math.round(Number(percentageValue) / 100 * tankVolumeDefault);

  const isOdometerValid = !!odometerValue && Number(odometerValue) !== 0
    && Number(odometerValue) >= odometerDefault && Number(odometerValue) <= (odometerDefault + 2500);
  const isLitreageValid = !!percentageValue && calculatedLitreage === litreageValue;
  const isSubmitDisabled = !isOdometerValid || !isLitreageValid;

  const handleOdometerChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value === '') {
      setOdometerValue(value);
    } else if (!value.match(/[^0-9]/)) {
      setOdometerValue(BigInt(value).toString());
    }
  };

  const handleLitreageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value === '') {
      setPercentageValue(value);
    } else if (!value.match(/[^0-9]/) && Number(value) <= 100) {
      setPercentageValue(Number(value).toString());
    }
  };

  const handleSubmit = () => {
    close({
      id: waybill.id,
      value: Number(odometerValue),
      fuelLitreage: litreageValue,
    })
      .then(() => {
        history.replace(routes.RELEASE_ON_LINE_LINK);
      })
      .catch(ignore);
  };

  return (
    <Modal
      visible={visible}
      width="450px"
      title={i18n.title}
      okText={i18n.okButtonText}
      okButtonProps={{ disabled: isSubmitDisabled, loading: isClosing }}
      onCancel={onClose}
      onOk={handleSubmit}
    >
      <div className={styles.itemBlock}>
        <FormItem label={i18n.odometer.title}>
          <Input
            value={odometerValue}
            maxLength={16}
            onChange={handleOdometerChange}
          />
        </FormItem>

        <span>{i18n.odometer.readings({ value: odometerDefault.toLocaleString() })}</span>
      </div>

      <div className={styles.itemBlock}>
        <FormItem label={i18n.litreage.title}>
          <Input
            value={percentageValue}
            onChange={handleLitreageChange}
            onBlur={() => setLitreageValue(calculatedLitreage)}
          />
        </FormItem>

        <span>{i18n.litreage.readings({ value: litreageValue })}</span>
      </div>
    </Modal>
  );
};

export default CloseModal;
