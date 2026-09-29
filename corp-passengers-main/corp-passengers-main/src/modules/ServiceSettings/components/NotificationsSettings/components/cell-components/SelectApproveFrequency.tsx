import { Radio } from 'antd';
import { RadioChangeEvent } from 'antd/lib/radio';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import { EditableCellProps } from 'shared/components/EditableTable';
import {
  ApprovementFrequencyNotificationsTitles,
  NotificationsEventTypes,
  NotificationsEventValue,
  NotificationsEventValueTitle,
  NotificationsRecord
} from 'stores/Notifications/Notifications.interface';
import { LabeledValue } from 'utils';
import Select from 'shared/form/Select/Select';

type SelectOption = LabeledValue & { value: NotificationsEventValue; label: NotificationsEventValueTitle };

const defaultStatuses = [
  {
    eventType: NotificationsEventTypes.AT_EVENT,
    timeFieldName: ApprovementFrequencyNotificationsTitles.AT_EVENT,
  },
  {
    eventType: NotificationsEventTypes.DEADLINE,
    timeFieldName: ApprovementFrequencyNotificationsTitles.DEADLINE,
  },
];

const defaultNotificationsValues = [
  {
    eventType: NotificationsEventValue.TenMinutes,
    timeFieldName: NotificationsEventValueTitle.TenMinutes,
  },
  {
    eventType: NotificationsEventValue.ThirtyMinutes,
    timeFieldName: NotificationsEventValueTitle.ThirtyMinutes,
  },
  {
    eventType: NotificationsEventValue.OneHour,
    timeFieldName: NotificationsEventValueTitle.OneHour,
  },
];

export const SelectApproveFrequency: FC<
  EditableCellProps<NotificationsRecord, NotificationsRecord['enabledChannels']>
> = observer(({
  isEditingRecord, onChange, record: { approvementFrequency },
}) => {
  // access value length to force MobX observer react to additions/removals
  const [value, setValue] = useState((approvementFrequency && approvementFrequency[0]) ?? defaultStatuses[0]);
  const [deadline, setDeadline] = useState<NotificationsEventValue>(
    (approvementFrequency && approvementFrequency[0].timeBefore) ?? 0
  );

  const options: SelectOption[] = defaultNotificationsValues.map(value => ({
    value: value.eventType,
    label: value.timeFieldName,
  }));

  const handleDeadLine = (deadlineTime: number) => {
    setDeadline(deadlineTime);
  };

  useEffect(() => {
    onChange({
      approvementFrequency: [{ timeBefore: deadline, eventType: value.eventType }],
    });
  }, [deadline, value, onChange]);

  if (isEditingRecord) {
    const handleChange: (e: RadioChangeEvent) => void = e => {
      onChange({
        approvementFrequency: [{ timeBefore: deadline ?? 0, eventType: e.target.value.eventType }],
      });

      setValue(e.target.value);
    };

    return (
      <Radio.Group onChange={handleChange} value={value}>
        <Radio value={defaultStatuses[0]}>{defaultStatuses[0].timeFieldName}</Radio>

        <Radio value={defaultStatuses[1]}>{defaultStatuses[1].timeFieldName}</Radio>

        {value.eventType === NotificationsEventTypes.DEADLINE && (
          <Select
            onChange={e => handleDeadLine(e as number)}
            options={options}
            getPopupContainer={trigger => trigger.parentNode}
          />
        )}
      </Radio.Group>
    );
  }

  const timeBefore = value.eventType === NotificationsEventTypes.DEADLINE ? `за ${deadline} минут` : '';

  return (
    <span>
      {value.eventType === NotificationsEventTypes.DEADLINE
        ? `${ApprovementFrequencyNotificationsTitles[value.eventType]} ${timeBefore}`
        : defaultStatuses[0].timeFieldName}
    </span>
  );
});
