import React, { FC } from 'react';
import { Form } from 'antd';
import { useDeadlineSettingsForm } from 'modules/DeadlineSettings/hooks/useDeadlineSettingsForm';
import { SettingsTable } from 'modules/DeadlineSettings/components/SettingsTable/SettingsTable';
import { Footer } from 'modules/DeadlineSettings/components/Footer/Footer';
import styles from './DeadlineSettings.module.scss';

export const DeadlineSettingsContent: FC = () => {
  const {
    form, busy, setBusy, settingsId,
  } = useDeadlineSettingsForm();

  return (
    <div className={styles.container}>
      <Form
        form={form}
        className={styles.form}
        // eslint-disable-next-line no-console
        onValuesChange={args => console.log(args)}
      >
        <SettingsTable busy={busy} type="passengers" />
        <Footer
          form={form}
          setBusy={setBusy}
          settingsId={settingsId}
        />
      </Form>
    </div>
  );
};

export default DeadlineSettingsContent;
