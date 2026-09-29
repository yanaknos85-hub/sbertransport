import React, { Dispatch, FC, SetStateAction } from 'react';
import { Checkbox } from 'antd';
import { useTranslation } from 'i18n';
import { preventDefault } from 'utils';
import { FormItem } from 'shared/components/FormItem';
import Input from 'shared/form/Input/Input';

import styles from './styles.module.scss';

interface Props {
  templateChecked: boolean;
  setTemplateChecked: Dispatch<SetStateAction<boolean>>;
  initialValue?: string;
  disabled?: boolean;
  onToggleOther?: () => void; // для выключения чекбокса Регулярная доставка при включении Переезда и наоборот
}

export const TemplateCheckbox: FC<Props> = ({
  templateChecked,
  setTemplateChecked,
  initialValue,
  disabled,
  onToggleOther
}) => {
  const { t } = useTranslation();

  return (
    <div className={styles.templateContainer}>
      <Checkbox
        checked={templateChecked}
        onChange={() => {
          setTemplateChecked(!templateChecked);
          if (!templateChecked && onToggleOther) {
            onToggleOther();
          }
        }}
        onKeyPress={preventDefault}
        disabled={disabled}
      >
        {t.Contracts.template}
      </Checkbox>
      <FormItem
        name="template"
        required={false}
        initialValue={initialValue}
      >
        <Input
          onPressEnter={preventDefault}
          className={styles.templateInput}
          disabled={!templateChecked}
          readOnly
          style={{
            backgroundColor: templateChecked ? 'transparent' : '#f5f5f5',
            color: templateChecked ? 'inherit' : 'rgba(0, 0, 0, 0.25)',
            cursor: 'default'
          }}
        />
      </FormItem>
    </div>
  );
};