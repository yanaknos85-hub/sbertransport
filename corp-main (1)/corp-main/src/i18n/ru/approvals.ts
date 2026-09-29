import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

export const Approvals = {
  headers: {
    [TransportTypes.TAXI]: 'Заявка на такси',
    [TransportTypes.CARSHARING]: 'Заявка на каршеринг',
    [TransportTypes.PUBLIC]: 'Заявка на общественный транспорт',
    [TransportTypes.PERSONAL]: 'Заявка на личный транспорт',
    [TransportTypes.BICYCLE]: 'Заявка на велосипед',
    [TransportTypes.SCOOTER]: 'Заявка на самокат',
    // Алла попросила убрать. Добавим после пилота.
    // Limit: 'Заявка на лимит',
  } as Record<TransportTypes | 'Limit', string>,
  additionalSettings: 'Дополнительные настройки',
};
