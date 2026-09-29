import React, { FC } from 'react';
import { DatePicker, Form } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import moment, { Moment } from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { DATE_FORMAT } from 'constants/constants.app';

import styles from './styles.module.scss';

export const DateRangeFilter: FC = observer(() => {
  const [form] = Form.useForm();

  const {
    [StoreNames.cargosStore]: cargosStore,
  } = useAppStoreContext();

  const onValueChange = dates => {
    if (!dates || !dates[0] || !dates[1]) {
      cargosStore.setDesiredDateRange(null);
      return;
    }
    const range: [Moment, Moment] = [dates[0].clone(), dates[1].clone()];
    cargosStore.setDesiredDateRange(range);
  };

  return (
    <Form form={form}>
      <DatePicker.RangePicker
        defaultValue={null}
        locale={locale}
        className={styles.DatePicker}
        value={cargosStore.desiredDateRange}
        onChange={onValueChange}
        format={DATE_FORMAT.BASE_REVERTED_DOTS}
        ranges={{
          ['День']: [moment().startOf('day'), moment().endOf('day')],
          ['Месяц']: [moment().startOf('month'), moment().endOf('month')],
          ['Квартал']: [moment().startOf('quarter'), moment().endOf('quarter')],
        }}
      />
    </Form>
  );
});
