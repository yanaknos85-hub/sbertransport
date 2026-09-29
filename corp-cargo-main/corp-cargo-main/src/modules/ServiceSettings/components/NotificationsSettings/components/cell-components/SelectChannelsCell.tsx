import { observer } from 'mobx-react';
import React, { FC } from 'react';
import { EditableCellProps } from 'shared/components/EditableTable';
import {
  NotificationsChannel,
  NotificationsChannels,
  NotificationsRecord
} from 'stores/Notifications/Notifications.interface';
import Select from 'shared/form/Select/Select';

const defaultChannels: NotificationsChannels = [
  {
    channel: NotificationsChannel.EMAIL,
    text: NotificationsChannel.EMAIL,
    enabled: true,
  },
  {
    channel: NotificationsChannel.PUSH,
    text: NotificationsChannel.PUSH,
    enabled: true,
  },
  {
    channel: NotificationsChannel.SMS,
    text: NotificationsChannel.SMS,
    enabled: true,
  },
];

export const SelectChannelsCell: FC<
  EditableCellProps<NotificationsRecord, NotificationsRecord['enabledChannels']>
> = observer(({
  cellValue, isEditingRecord, onChange, record: { enabledChannels },
}) => {
  // access value length to force MobX observer react to additions/removals
  const selectedChannels = (cellValue || []).filter(item => item.enabled).map(({ channel }) => channel);
  // Very strange place

  if (isEditingRecord) {
    const handleChange = (channelsTypes: NotificationsChannel[]) => onChange({
      enabledChannels: channelsTypes.map(channel => ({ channel, enabled: true })),
    });

    const options = defaultChannels.map(({ channel: value }) => ({ value }));

    return (
      <Select
        mode="multiple"
        allowClear
        value={selectedChannels}
        onChange={e => handleChange(e as NotificationsChannel[])}
        // @ts-ignore
        options={options}
        optionLabelProp="title"
        filterOption
        getPopupContainer={trigger => trigger.parentNode}
      />
    );
  }

  return (
    <span>
      {enabledChannels.length > 0
        ? enabledChannels
          .filter(enabledChannel => enabledChannel.enabled)
          .map(enabledChannel => enabledChannel.channel)
          .join(', ')
        : defaultChannels
          .filter(defaultChannel => selectedChannels.includes(defaultChannel.channel))
          .map(defaultChannel => defaultChannel.channel)
          .join(', ')}
    </span>
  );
});
