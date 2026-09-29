import { Radio } from 'antd';
import { RadioChangeEvent } from 'antd/lib/radio';
import { useRoles } from 'api/roles';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import { EditableCellProps } from 'shared/components/EditableTable';
import {
  NotificationsRecord,
  NotificationsRestrictionsTypes,
  NotificationsRestrictionsTypesHeaders
} from 'stores/Notifications/Notifications.interface';
import { Role } from 'stores/Roles/Roles.interface';
import { LabeledValue } from 'utils';
import { UUID } from 'utils/io-ts';
import Select from 'shared/form/Select/Select';

type SelectOption = LabeledValue & { label: string; title: string };

const defaultRestrictions = [
  {
    type: NotificationsRestrictionsTypes.ALLOW_ALL,
    label: NotificationsRestrictionsTypesHeaders.ALLOW_ALL,
  },
  {
    type: NotificationsRestrictionsTypes.ALLOW_BUT,
    label: NotificationsRestrictionsTypesHeaders.ALLOW_BUT,
    hasOptions: true,
  },
  {
    type: NotificationsRestrictionsTypes.DENY_BUT,
    label: NotificationsRestrictionsTypesHeaders.DENY_BUT,
    hasOptions: true,
  },
  {
    type: NotificationsRestrictionsTypes.DENY_ALL,
    label: NotificationsRestrictionsTypesHeaders.DENY_ALL,
  },
];

export const SelectRestrictions: FC<
  EditableCellProps<NotificationsRecord, NotificationsRecord['enabledChannels']>
> = observer(({
  isEditingRecord, onChange, record: { restrictions },
}) => {
  const { data } = useRoles();

  const [value, setValue] = useState((restrictions && restrictions[0].type) ?? defaultRestrictions[0].type);

  const [roles, setRoles] = useState([] as UUID[]);

  const options: SelectOption[] = data.map(role => ({
    value: role.code as UUID,
    label: role.name,
    title: role.name,
  }));

  const handleChangeRoles = (roleIds: UUID[]) => setRoles(roleIds);
  const handleChangeRestrictions = (e: RadioChangeEvent) => setValue(e.target.value);

  useEffect(() => {
    onChange({
      restrictions: [{ type: value, roles }],
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roles, value]);

  if (isEditingRecord) {
    return (
      <Radio.Group onChange={handleChangeRestrictions} value={value}>
        {defaultRestrictions.map(({
          type, label, hasOptions,
        }) => (
          <div key={type}>
            <Radio value={type}>{label}</Radio>
            {value === type && hasOptions && (
            <Select
              onChange={roleIds => handleChangeRoles(roleIds as UUID[])}
              options={options}
              mode="multiple"
            />
            )}
          </div>
        ))}
      </Radio.Group>
    );
  }

  const roleCodeToRoleName = (code: Role['code']) => data && data.find(role => role.code === code)?.name;

  return (
    <span>
      {restrictions && restrictions?.length > 0
        ? `${NotificationsRestrictionsTypesHeaders[restrictions[0].type]} ${restrictions[0].roles
          .map(code => roleCodeToRoleName(code))
          .join(', ')}`
        : defaultRestrictions[0].label}
    </span>
  );
});
