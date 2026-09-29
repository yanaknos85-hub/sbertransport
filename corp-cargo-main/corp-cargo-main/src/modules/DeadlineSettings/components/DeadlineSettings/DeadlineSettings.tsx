import React from 'react';
import { Form } from 'antd';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { Footer } from '../Footer/Footer';
import { SettingsTable } from '../SettingsTable/SettingsTable';
import styles from './DeadlineSettings.module.scss';
import { useDeadlineSettingsForm } from '../../hooks/useDeadlineSettingsForm';

const DeadlineSettings = withErrorBoundary(() => {
  const {
    form, busy, setBusy, settingsId,
  } = useDeadlineSettingsForm();

  return (
    <div className={styles.container}>
      <Form form={form}>
        <SettingsTable busy={busy} />
        <Footer
          form={form}
          setBusy={setBusy}
          settingsId={settingsId}
        />
      </Form>
    </div>
  );
});

export default DeadlineSettings;
