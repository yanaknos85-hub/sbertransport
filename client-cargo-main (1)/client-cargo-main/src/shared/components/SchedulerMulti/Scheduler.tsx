import React, { FC, useEffect, useState } from 'react';
import { Form } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';

import { StoreNames } from 'stores/StoreNames.enum';
import { DATE_FORMAT } from 'constants/constants.app';

import { useAppStoreContext } from '../../hooks/useEmpContext';
import { FieldName, Periodicity, PeriodicityLabel } from './constants';
import RowButtons from './RowButtons/RowButtons';

interface Props {
  tariffCost?: number;
  setSchedulerError?: (param: boolean) => void;
}

const Scheduler: FC<Props> = observer(props => {
  const { tariffCost, setSchedulerError } = props;

  const [form] = Form.useForm();

  const { [StoreNames.cargoStore]: cargoStore } = useAppStoreContext();

  const [data, setData] = useState(cargoStore.periodValues);
  const {
    monthOfQuartal, weekOfMonth, dayOfWeek,
  } = data;

  useEffect(() => {
    setData(cargoStore.periodValues);
  }, [cargoStore.periodValues]);

  const disabledDate = (currentDate: Date) => {
    return currentDate && moment(currentDate) < moment().startOf('day').add(1, 'days');
  };

  const handleFormatDate = (date: string) => (
    moment(date)
      .startOf('day')
      .format(DATE_FORMAT.BASE)
  );

  const field = {
    [FieldName.beginDate]: {
      label: 'Дата начала',
      name: FieldName.beginDate,
      type: FieldType.date,
      rules: [ValidationRules.general.required],
      initialValue: data.beginDate ? moment(data.beginDate) : '',
      params: {
        format: DATE_FORMAT.BASE_REVERTED,
        disabledDate,
      },
    },
    [FieldName.endDate]: {
      label: 'Дата окончания',
      name: FieldName.endDate,
      type: FieldType.date,
      rules: [ValidationRules.general.required],
      initialValue: data.endDate ? moment(data.endDate) : '',
      params: {
        format: DATE_FORMAT.BASE_REVERTED,
        disabledDate,
      },
    },
    [FieldName.periodicity]: {
      label: 'Периодичность',
      name: FieldName.periodicity,
      type: FieldType.select,
      initialValue: data.periodType,
      params: {
        options: [
          { label: PeriodicityLabel[Periodicity.week], value: Periodicity.week },
          { label: PeriodicityLabel[Periodicity.month], value: Periodicity.month },
          { label: PeriodicityLabel[Periodicity.quarter], value: Periodicity.quarter },
        ],
      },
    },
  };

  const onChangeScheduler = (key: string, values: string | number | number[]) => {
    const newData = {
      ...data,
      [key]: values,
    };

    setData(newData);
    cargoStore.setPeriodValues(newData);
    cargoStore.getPeriodValuesMulti({ ...newData, cost: tariffCost })
      .then(() => {
        setSchedulerError?.(false);
      })
      .catch(err => {
        if (err.response.data.status === 400) {
          setSchedulerError?.(true);
        }
      });
  };

  const handleOnChangeMonthsOfQuarter = (values: number[]) => {
    onChangeScheduler(FieldName.monthsOfQuarter, values);
  };

  const handleOnChangeWeeksOfMonth = (values: number[]) => {
    onChangeScheduler(FieldName.weeksOfMonth, values);
  };

  const handleOnChangeDaysOfWeek = (values: number[]) => {
    onChangeScheduler(FieldName.daysOfWeek, values);
  };

  const handleChangeForm = (values: {
    [FieldName.beginDate]: string;
    [FieldName.endDate]: string;
    [FieldName.periodicity]: string;
  }) => {
    if (Object.keys(values).includes(FieldName.periodicity)) {
      onChangeScheduler(FieldName.periodicity, values[FieldName.periodicity]);
    } else if (Object.keys(values).includes(FieldName.beginDate)) {
      onChangeScheduler(FieldName.beginDate, handleFormatDate(values[FieldName.beginDate].valueOf()));
    } else if (Object.keys(values).includes(FieldName.endDate)) {
      onChangeScheduler(FieldName.endDate, handleFormatDate(values[FieldName.endDate].valueOf()));
    }
  };

  useEffect(() => {
    if (tariffCost && data.beginDate) {
      cargoStore.getPeriodValuesMulti({ ...data, cost: tariffCost });
    }
  }, [tariffCost]);

  // при ежеквартальной - нужен месяц, неделя в месяце и день недели
  // для ежемесячной - день недели и неделя месяца,
  return (
    <div>
      <Form
        form={form}
        name="schedulerForm"
        onValuesChange={handleChangeForm}
      >
        <FormField {...field[FieldName.beginDate]} />
        <FormField {...field[FieldName.endDate]} />
        <FormField {...field[FieldName.periodicity]} />
        {data[FieldName.periodicity] === Periodicity.quarter && (
          <RowButtons
            initialValue={monthOfQuartal}
            label="Месяц"
            values={[1, 2, 3]}
            onChange={handleOnChangeMonthsOfQuarter}
          />
        )}
        <RowButtons
          initialValue={dayOfWeek}
          label="Дни недели"
          names={['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс']}
          values={[1, 2, 3, 4, 5, 6, 7]}
          onChange={handleOnChangeDaysOfWeek}
        />
        {(data[FieldName.periodicity] === Periodicity.month || data[FieldName.periodicity] === Periodicity.quarter) && (
          <RowButtons
            initialValue={weekOfMonth}
            label="Неделя месяца"
            values={[1, 2, 3, 4]}
            onChange={handleOnChangeWeeksOfMonth}
          />
        )}
      </Form>
    </div>
  );
});

export default Scheduler;
