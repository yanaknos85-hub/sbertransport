import { Radio } from 'antd';
import { RadioChangeEvent } from 'antd/lib/radio';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import { EditableCellProps } from 'shared/components/EditableTable';
import {
  NotificationClass,
  NotificationsCountingsTypes,
  NotificationsCountingsTypesTitles,
  NotificationsRecord
} from 'stores/Notifications/Notifications.interface';
import Input from 'shared/components/Inputs/Input/Input';
import { useDefaultNotificationsByClasses } from '../useDefaultNotifications';

const defaultCountings = [
  {
    type: NotificationsCountingsTypes.EXACT,
    value: 'Сумма',
    property: '',
  },
  {
    type: NotificationsCountingsTypes.PERCENT,
    value: 'Процент',
    property: '',
  },
];

export const SelectLowLimit: FC<
  EditableCellProps<NotificationsRecord, NotificationsRecord['enabledChannels']>
> = observer(({
  isEditingRecord, onChange, record: { lowLimitNotifications },
}) => {
  // access value length to force MobX observer react to additions/removals

  const [value, setValue] = useState((lowLimitNotifications && lowLimitNotifications[0]) ?? defaultCountings[0]);

  const [percent, setPercent] = useState(0);
  const [exact, setExact] = useState(0);

  const handlePercent = (p: React.ChangeEvent<HTMLInputElement>) => {
    setPercent(Number(p.target.value));
  };

  const handleExact = (e: React.ChangeEvent<HTMLInputElement>) => {
    setExact(Number(e.target.value));
  };

  const defaultValues = useDefaultNotificationsByClasses();

  useEffect(() => {
    // TODO: fix NotificationsRestrictionsTypes but type
    const property = defaultValues[NotificationClass.LIMIT_DEPARTMENT][0].countings?.[0]?.property ?? '';
    onChange({
      lowLimitNotifications: [
        {
          property,
          type: NotificationsCountingsTypes.EXACT,
          value: exact,
        },
      ],
    });
  }, [percent, exact, onChange, defaultValues]);

  if (isEditingRecord) {
    const handleChange: (e: RadioChangeEvent) => void = e => {
      onChange({
        lowLimitNotifications: [{
          value: e.target.value, type: NotificationsCountingsTypes.EXACT, property: '',
        }],
      });

      setValue(e.target.value);
    };

    return (
      <Radio.Group onChange={handleChange} value={value}>
        <Radio value={defaultCountings[0]}>{defaultCountings[0].value}</Radio>

        {value.type === NotificationsCountingsTypes.EXACT && <Input onChange={handleExact} type="number" />}

        <Radio value={defaultCountings[1]}>{defaultCountings[1].value}</Radio>

        {value.type === NotificationsCountingsTypes.PERCENT && <Input onChange={handlePercent} type="number" />}
      </Radio.Group>
    );
  }

  return (
    <span>
      {lowLimitNotifications && lowLimitNotifications?.length > 0
        ? `${NotificationsCountingsTypesTitles[lowLimitNotifications[0].type]} ${lowLimitNotifications[0].value}`
        : NotificationsCountingsTypesTitles[defaultCountings[0].type]}
    </span>
  );
});
