export const i18nMock = {
  t: {
    Waybill: {
      title: 'Массовое создание путевых листов',
      startDate: 'Дата начала',
      dateRequired: 'Выберите дату начала',
      helperText: 'Выберите смены для создания путевых листов',
      backButton: 'Назад',
      nextButton: 'Далее',
    },
  },
};

export const useTranslationMock = jest.fn(() => i18nMock);
