/**
 * Очищает поле ввода от нечисловых символов (оставляет только цифры)
 * @param e - событие изменения input
 */
export const handleInputOnlyNumbers = e => {
  e.target.value = e.target.value.replace(/[^\d]/g, '');
};
