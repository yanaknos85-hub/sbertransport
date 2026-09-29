import { observer } from 'mobx-react';
import React, { FC } from 'react';
import { EditableCellProps } from 'shared/components/EditableTable';
import { NotificationsCountingsTypes, NotificationsRecord } from 'stores/Notifications/Notifications.interface';

import Input from 'shared/components/Inputs/Input/Input';
import styles from '../../styles.module.scss';

export const SelectWaitingTime: FC<
  EditableCellProps<NotificationsRecord, NotificationsRecord['enabledChannels']>
> = observer(({
  cellValue, isEditingRecord, onChange, record: { excessWaitingInIntermediate },
}) => {
  // access value length to force MobX observer react to additions/removals

  if (isEditingRecord) {
    const handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void = e => {
      onChange({
        excessWaitingInIntermediate: [
          {
            type: NotificationsCountingsTypes.EXACT,
            value: Math.abs(Number(e.target.value)),
            // stange property
            property: 'Время ожидания в промежуточной точке',
          },
        ],
      });
    };

    return (
      <div className={styles.waiting_time_container}>
        <Input
          defaultValue={excessWaitingInIntermediate?.[0]?.value ?? 10}
          onChange={handleChange}
          type="number"
          min={0}
        />
        <span>минут</span>
      </div>
    );
  }

  return (
    <span>
      {excessWaitingInIntermediate
      && excessWaitingInIntermediate?.length > 0
      && `${excessWaitingInIntermediate[0].value} минут`}
    </span>
  );
});
