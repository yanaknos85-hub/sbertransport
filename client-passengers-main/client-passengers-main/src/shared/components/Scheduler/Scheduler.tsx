import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import { Form } from 'antd';

import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { ValidationRules } from 'shared/fieldValidationRules';

import RowButtons from './RowButtons/RowButtons';
import { FieldName, Periodicity, PeriodicityLabel } from './constants';

const INITIAL_PERIODICITY = Periodicity.week;

interface Props {
  tariffCost?: number;
}

const Scheduler: FC<Props> = observer(() => {
  const [form] = Form.useForm();

  // const { [StoreNames.cargoStore]: cargoStore } = useAppStoreContext();

  const [data, setData] = useState({
    [FieldName.periodicity]: INITIAL_PERIODICITY,
    [FieldName.monthsOfQuarter]: [],
    [FieldName.weeksOfMonth]: [],
    [FieldName.daysOfWeek]: [],
    [FieldName.beginDate]: '',
    [FieldName.endDate]: '',
  });

  const field = {
    [FieldName.beginDate]: {
      label: 'Дата начала',
      name: FieldName.beginDate,
      type: FieldType.date,
      rules: [ValidationRules.general.required],
    },
    [FieldName.endDate]: {
      label: 'Дата окончания',
      name: FieldName.endDate,
      type: FieldType.date,
      rules: [ValidationRules.general.required],
    },
    [FieldName.periodicity]: {
      label: 'Периодичность',
      name: FieldName.periodicity,
      type: FieldType.select,
      params: {
        options: [
          { label: PeriodicityLabel[Periodicity.week], value: Periodicity.week },
          { label: PeriodicityLabel[Periodicity.month], value: Periodicity.month },
          { label: PeriodicityLabel[Periodicity.quarter], value: Periodicity.quarter },
        ],
      },
      initialValue: INITIAL_PERIODICITY,
    },
  };

  const onChangeSheduler = (key: string, values: string | number[]) => {
    const newData = {
      ...data,
      [key]: values,
    };
    setData(newData);
    // cargoStore.setPeriodValues(newData);
    // cargoStore.getPeriodValues({ ...newData, cost: tariffCost });
  };

  const handleOnChangeMonthsOfQuarter = (values: number[]) => {
    onChangeSheduler(FieldName.monthsOfQuarter, values);
  };

  const handleOnChangeWeeksOfMonth = (values: number[]) => {
    onChangeSheduler(FieldName.weeksOfMonth, values);
  };

  const handleOnChangeDaysOfWeek = (values: number[]) => {
    onChangeSheduler(FieldName.daysOfWeek, values);
  };

  const handleChangeForm = (values: {
    [FieldName.beginDate]: string;
    [FieldName.endDate]: string;
    [FieldName.periodicity]: string;
  }) => {
    if (Object.keys(values).includes(FieldName.periodicity)) {
      onChangeSheduler(FieldName.periodicity, values[FieldName.periodicity]);
    } else if (Object.keys(values).includes(FieldName.beginDate)) {
      onChangeSheduler(FieldName.beginDate, values[FieldName.beginDate].valueOf());
    } else if (Object.keys(values).includes(FieldName.endDate)) {
      onChangeSheduler(FieldName.endDate, values[FieldName.endDate].valueOf());
    }
  };
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
            label="Месяц"
            values={[1, 2, 3]}
            onChange={handleOnChangeMonthsOfQuarter}
          />
        )}
        <RowButtons
          label="Дни недели"
          names={['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс']}
          values={[1, 2, 3, 4, 5, 6, 7]}
          onChange={handleOnChangeDaysOfWeek}
        />
        {(data[FieldName.periodicity] === Periodicity.month || data[FieldName.periodicity] === Periodicity.quarter) && (
          <RowButtons
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
