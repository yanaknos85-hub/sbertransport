import React, { useEffect } from 'react';
import { Form } from 'antd';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { Footer } from '../Footer/Footer';
import { SettingsTable } from '../SettingsTable/SettingsTable';
import { fillFormInitialValues } from '../../utils/utils';
import { useCargoDeliveryTimeSettingsForm } from '../../hooks/useCargoDeliveryTimeSettingsForm';

import styles from './styles.module.scss';

const CargoDeliveryTimeSettings = withErrorBoundary(() => {
  const {
    form, cargoDeliveryTimeSettingsArray, busy, setBusy,
  } = useCargoDeliveryTimeSettingsForm();

  useEffect(() => {
    fillFormInitialValues(form, cargoDeliveryTimeSettingsArray);
    setBusy(false);
  }, [cargoDeliveryTimeSettingsArray, form, setBusy]);

  return (
    <div className={styles.container}>
      <Form form={form}>
        <SettingsTable busy={busy} cargoDeliveryTimeSettingsArray={cargoDeliveryTimeSettingsArray} />
        <Footer form={form} setBusy={setBusy} />
      </Form>
    </div>
  );
});

export default CargoDeliveryTimeSettings;
