import React, {
  FC, useCallback, useMemo, useState
} from 'react';
import { Checkbox, notification } from 'antd';
import { TNotification } from './Notifications.interface';
import { useUpdateNotifications } from 'api/notifications';

export const CheckboxGroup: FC<TNotification> = props => {
  const {
    id,
    smsActive,
    pushActive,
    emailActive,
    smsEnabled,
    pushEnabled,
    emailEnabled,
  } = props;

  const [updateNotifications] = useUpdateNotifications();

  const [checkedState, setCheckedState] = useState({
    smsActive: smsActive,
    pushActive: pushActive,
    emailActive: emailActive,
  });

  const options = useMemo(() => [
    {
      label: 'Push', value: 'pushActive', disabled: !pushEnabled,
    },
    {
      label: 'SMS', value: 'smsActive', disabled: !smsEnabled,
    },
    {
      label: 'Email', value: 'emailActive', disabled: !emailEnabled,
    },
  ], [pushActive, smsActive, emailActive, pushEnabled, smsEnabled, emailEnabled]);

  // Преобразуем состояние в массив выбранных значений для Checkbox.Group
  const selectedValues = Object.keys(checkedState).filter(key => checkedState[key]);

  // Функция изменения состояния чекбоксов
  const handleChange = useCallback(checkedValues => {
    // Обновляем состояние для каждого чекбокса
    const newState = {
      smsActive: checkedValues.includes('smsActive'),
      pushActive: checkedValues.includes('pushActive'),
      emailActive: checkedValues.includes('emailActive'),
    };
    setCheckedState(newState);
    updateNotifications({ id: id, settings: newState })
      .then(() => {
        notification.success({ message: 'Настройки уведомлений успешно обновлены!' });
      })
      .catch(() => {
        notification.error({ message: 'Произошла ошибка при изменении настроек уведомлений!' });
      });
  }, [id, checkedState, smsActive, pushActive, emailActive]);

  return (
    <div>
      <Checkbox.Group
        options={options}
        value={selectedValues}
        onChange={handleChange}
      />
    </div>
  );
};
