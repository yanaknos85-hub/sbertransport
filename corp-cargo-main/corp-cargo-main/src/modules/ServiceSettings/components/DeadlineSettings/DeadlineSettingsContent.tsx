import React, { FC } from 'react';
import { Form } from 'antd';
import { useDeadlineSettingsForm } from 'modules/DeadlineSettings/hooks/useDeadlineSettingsForm';
import { SettingsTable } from 'modules/DeadlineSettings/components/SettingsTable/SettingsTable';
import { Footer } from 'modules/DeadlineSettings/components/Footer/Footer';
import withErrorBoundary from "shared/decorators/withErrorBoundary";

import styles from './DeadlineSettings.module.scss';

export const DeadlineSettingsContent: FC = () => {
  const { form, busy, setBusy } = useDeadlineSettingsForm();

  return (
    <div className={styles.container}>
      <Form
        form={form}
        className={styles.form}
        onValuesChange={args => console.log(args)}
      >
        <SettingsTable busy={busy} type="cargo" />
        <Footer
          form={form}
          setBusy={setBusy}
        />
      </Form>
    </div>
  );
};

export default withErrorBoundary(DeadlineSettingsContent);
