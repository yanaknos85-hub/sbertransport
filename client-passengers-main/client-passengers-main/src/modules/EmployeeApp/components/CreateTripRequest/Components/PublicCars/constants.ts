import { TransportCompensations, TransportCompensationsTitles } from 'stores/TransportTypes/TransportTypes.interface';

export const tripsInfoTitle = 'tripsInfo';

export const enum FormItemNames {
  tripsInfo = 'tripsInfo',
  compensationTypes = 'compensationTypes',
  transportTypes = 'transportTypes',
}

export const minTicketQuantity = 1;

export const fieldTitles = {
  publicTransportType: 'publicTransportType',
  cost: 'cost',
  ticketCount: 'ticketCount',
  sum: 'sum',
  ticket: 'ticket',
  calendar: 'calendar',
  humanReadableId: 'humanReadableId',
};

export const costOneTrip = 'Стоимость 1 поездки';
export const costLabel = 'Стоимость билета';
export const costTravelCard = 'Стоимость проездного';
export const ticketCountLabel = 'Количество билетов';
export const sumLabel = 'Стоимость';
export const generalSumLabel = 'Общая стоимость';
export const generalSumTitle = 'Итого';
export const periodTravel = 'Период действия проездного';
export const applicationNumber = 'Номер заявки на поездку';

export const deleteTooltip = 'Удалить элемент';

export const addButtonTitle = 'Добавить';

export const tariffWasNotFound = 'Не обнаружен тариф для выбранного вида компенсации';

export const ticketLabel = 'Добавить подтверждение';

export const documentConfirmation
  = 'Для подтверждения стоимости билета на междугородний транспорт не забудьте добавить фото билета или снимок экрана с сайта поставщика услуги.';

export const falsificationAlert = 'В случае нарушения нормативных документов ваша заявка будет отменена';

export const fileTypeErrorMessage
  = 'Необходимо добавить файл формата .jpg, .jpeg, .png, .tiff, .pdf, .heif, '
  + 'не превышающий размер 2Мб';

export const publicTransportTypeTitle = 'Вид транспорта';
export const publicTransportServiceTitle = 'Вид сервиса';
export const publicTransportCompensationTitle = 'Вид компенсации';
export const selectRequestTrip = 'Выберите заявку на поездку';

export const compensationOptions = Object.keys(TransportCompensations).map((value: string) => ({
  value,
  label: TransportCompensationsTitles[value as TransportCompensations],
}));

export const compensationFormTitle = 'Создание заявки на компенсацию за использование общественного транспорта';
export const errorEmptyFields = 'Проверьте заполнение полей формы!';
export const defaultFormText = 'Выберите тип компенсации';

export const compensationTypeTitle = 'compensationType';
export const compensationTypeLabel = 'Тип билета';

export const acceptRequestButton = 'Подтвердить';
export const chancelRequestButton = 'Отменить запрос';

export const chancelRequestMessage = 'Отменить создание заявки?';
export const YesButton = 'Да';
export const NoButton = 'Нет';
