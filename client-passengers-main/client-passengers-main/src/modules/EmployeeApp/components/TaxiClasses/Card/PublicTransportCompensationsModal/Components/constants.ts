import { TransportCompensations, TransportCompensationsTitles } from 'stores/TransportTypes/TransportTypes.interface';

export const tripsInfoTitle = 'tripsInfo';

export const minTicketQuantity = 1;

export const fieldTitles = {
  publicTransportType: 'publicTransportType',
  cost: 'cost',
  ticketCount: 'ticketCount',
  sum: 'sum',
  ticket: 'ticket',
  calendar: 'calendar',
};

export const costOneTrip = 'Стоимость 1 поездки';
export const costLabel = 'Стоимость билета';
export const costTravelCard = 'Стоимость 1 проездного документа';
export const ticketCountLabel = 'Количество билетов';
export const sumLabel = 'Общая сумма билетов';
export const generalSumLabel = 'Общая сумма компенсации';

export const deleteTooltip = 'Удалить элемент';

export const addButtonTitle = 'Добавить билет';

export const tariffWasNotFound = 'Не обнаружен тариф для выбранного вида компенсации';

export const ticketLabel = 'Документ, подтверждающий стоимость билета';

export const fileTypeErrorMessage
  = 'Необходимо добавить файл формата .jpg, .jpeg, .png, .tiff, .pdf, .doc, .docx, .heic, '
  + 'не превышающий размер 2Мб';

export const publicTransportTypeTitle = 'Вид общественного транспорта';

export const compensationOptions = Object.keys(TransportCompensations).map((value: string) => ({
  value,
  label: TransportCompensationsTitles[value as TransportCompensations],
}));

export const compensationFormTitle = 'Создание заявки на компенсацию за использование общественного транспорта';
export const errorEmptyFields = 'Проверьте заполнение полей формы!';
export const defaultFormText = 'Выберите тип компенсации';

export const compensationTypeTitle = 'compensationType';
export const compensationTypeLabel = 'Вид компенсации';

export const acceptRequestButton = 'Подтвердить';
export const chancelRequestButton = 'Отменить запрос';

export const chancelRequestMessage = 'Отменить создание заявки?';
export const YesButton = 'Да';
export const NoButton = 'Нет';
