import React, { Dispatch, FC, SetStateAction } from 'react';
import { Checkbox } from 'antd';
import { useTranslation } from 'i18n';
import { preventDefault } from 'utils';
import { FormItem } from 'shared/components/FormItem';
import InputNumber from 'shared/form/InputNumber/InputNumber';
import { MAX_VAT_VALUE, MIN_VAT_VALUE } from '../constants/constants';

import styles from './styles.module.scss';

interface Props {
  vatChecked: boolean;
  setVatChecked: Dispatch<SetStateAction<boolean>>;
}

export const NDSCheckbox: FC<Props> = ({
  vatChecked, setVatChecked,
}) => {
  const { t } = useTranslation();

  return (
    <div className={styles.ndsContainer}>
      <Checkbox
        checked={vatChecked}
        onChange={() => setVatChecked(!vatChecked)}
        onKeyPress={preventDefault}
      >
        {t.Contracts.includeVat}
      </Checkbox>

      <FormItem
        name="vatValue"
        required={false}
      >
        <InputNumber
          onPressEnter={preventDefault}
          min={MIN_VAT_VALUE}
          max={MAX_VAT_VALUE}
          // className={styles.vatInput}
          disabled={!vatChecked}
        />
      </FormItem>
    </div>
  );
};
