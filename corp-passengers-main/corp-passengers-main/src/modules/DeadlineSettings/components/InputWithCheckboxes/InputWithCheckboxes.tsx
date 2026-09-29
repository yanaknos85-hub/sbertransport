import { Radio, Form, InputNumber } from 'antd';
import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import styles from './InputWithCheckboxes.module.scss';

interface Props {
  inputName: string;
  radioName: string;
  includeAll?: boolean;
  includeMinutes?: boolean;
  includeHours?: boolean;
  includeDays?: boolean;
}

export const InputWithCheckboxes: FC<Props> = ({
  inputName,
  radioName,
  includeAll,
  includeMinutes,
  includeHours,
  includeDays,
}) => {
  const { t } = useTranslation();
  return (
    <div className={styles.flexRow}>
      <Form.Item name={inputName} className={styles.formItem}>
        <InputNumber
          className={styles.input}
          max={999}
          min={0}
          step={1}
        />
      </Form.Item>
      <Form.Item name={radioName} className={styles.formItem}>
        <Radio.Group className={styles.radioGroup} value="value">
          {(includeMinutes || includeAll) && (
            <Radio defaultChecked value="MINUTES">
              {t.DeadlineSettings.radioValues.minutes}
            </Radio>
          )}
          {(includeHours || includeAll) && <Radio value="HOURS">{t.DeadlineSettings.radioValues.hours}</Radio>}
          {(includeDays || includeAll) && <Radio value="DAYS">{t.DeadlineSettings.radioValues.days}</Radio>}
        </Radio.Group>
      </Form.Item>
    </div>
  );
};
