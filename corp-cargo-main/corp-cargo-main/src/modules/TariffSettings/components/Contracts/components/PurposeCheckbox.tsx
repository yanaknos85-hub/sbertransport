import React, { Dispatch, FC, SetStateAction } from 'react';
import { Checkbox, Tooltip } from 'antd';
import { useTranslation } from 'i18n';
import { preventDefault } from 'utils';
import { FormItem } from 'shared/components/FormItem';
import Input from 'shared/form/Input/Input';
import cn from 'classnames';

import styles from './styles.module.scss';

interface Props {
  purposeChecked: boolean;
  setPurposeChecked: Dispatch<SetStateAction<boolean>>;
  initialValue?: string;
  disabled?: boolean;
  onToggleOther?: () => void; // для выключения чекбокса Регулярная доставка при включении Переезда и наоборот
}

export const PurposeCheckbox: FC<Props> = ({
  purposeChecked,
  setPurposeChecked,
  initialValue,
  disabled,
  onToggleOther
}) => {
  const { t } = useTranslation();

  return (
    <div className={styles.purposeContainer}>
      <Tooltip
        title={t.Contracts.purposeRules}
        visible={purposeChecked ? undefined : false}
      >
        <span>
          <Checkbox
            checked={purposeChecked}
            onChange={() => {
              setPurposeChecked(!purposeChecked);
              if (!purposeChecked && onToggleOther) {
                onToggleOther();
              }
            }}
            onKeyPress={preventDefault}
            disabled={disabled}
          >
            {t.Contracts.purpose}
          </Checkbox>
        </span>
      </Tooltip>
      <FormItem
        name="purpose"
        required={false}
        initialValue={initialValue}
      >
        <Input
          onPressEnter={preventDefault}
          className={cn(styles.purposeInput, {
            [styles.purposeInput_disabled]: !purposeChecked,
          })}
          disabled={!purposeChecked}
          readOnly
        />
      </FormItem>
    </div>
  );
};