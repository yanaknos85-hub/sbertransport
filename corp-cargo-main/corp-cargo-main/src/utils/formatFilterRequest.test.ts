import { formatFilterRequest } from './formatFilterRequest';

describe('formatFilterRequest', () => {
  const fieldLabels = {
    requestHumanId: 'Номер заявки',
    cargoTransportType: 'Вид транспорта',
    contractorSet: 'Контрагент',
    authorFIO: 'ФИО создателя',
    requestStatusSet: 'Статус поездки',
    deadlineDate: 'Контрольный срок',
    desiredDate: 'Дата отправления',
    creationDate: 'Дата создания заявки',
    changeDate: 'Дата изменения заявки',
    organizationSet: 'Организация',
    department1: 'Подразделение 1 уровня',
    department2: 'Подразделение 2 уровня',
    department3: 'Подразделение 3 уровня',
    department4: 'Подразделение 4 уровня',
    department5: 'Подразделение 5 уровня',
    department6: 'Подразделение 6 уровня',
    status: 'Статус',
    sortSetting: 'Настройки сортировки',
    organizationId: 'ID организации',
    empty: '',
  };

  test('должен возвращать пустую строку для пустого фильтра', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({} as any)).toBe('');
  });

  test('должен возвращать пустую строку для undefined', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest(undefined as any)).toBe('');
  });

  test('должен возвращать пустую строку для null', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest(null as any)).toBe('');
  });

  test('должен возвращать пустую строку для фильтра с только полем empty', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({ empty: true } as any)).toBe('');
  });

  test('должен форматировать один активный фильтр', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({ requestHumanId: '123' } as any)).toBe('Номер заявки');
  });

  test('должен форматировать несколько активных фильтров', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      requestHumanId: '123',
      organizationSet: ['org1'],
      creationDate: '2024-01-01',
    } as any)).toBe('Номер заявки, Организация, Дата создания заявки');
  });

  test('должен фильтровать пустые строки', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      requestHumanId: '',
      organizationSet: ['org1'],
    } as any)).toBe('Организация');
  });

  test('должен фильтровать null и undefined', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      requestHumanId: null,
      authorFIO: undefined,
      organizationSet: ['org1'],
    } as any)).toBe('Организация');
  });

  test('должен фильтровать пустые массивы', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      contractorSet: [],
      organizationSet: ['org1'],
    } as any)).toBe('Организация');
  });

  test('должен фильтровать пустые объекты', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      sortSetting: {} as any,
      organizationSet: ['org1'],
    } as any)).toBe('Организация');
  });

  test('должен игнорировать поле empty', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      empty: true,
      requestHumanId: '123',
    } as any)).toBe('Номер заявки');
  });

  test('должен использовать метки полей из fieldLabels', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      requestHumanId: '123',
      cargoTransportType: ['CITY_BUS'],
    } as any)).toBe('Номер заявки, Вид транспорта');
  });

  test('должен работать с массивами значений', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      organizationSet: ['org1', 'org2'],
      contractorSet: ['contr1', 'contr2', 'contr3'],
    } as any)).toBe('Организация, Контрагент');
  });

  test('должен фильтровать объекты с пустыми массивами', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      organizationSet: [],
      status: ['active'],
    } as any)).toBe('Статус');
  });

  test('должен форматировать с несколькими активными фильтрами', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    const result = formatRequest({
      requestHumanId: '500',
      cargoTransportType: ['CITY_BUS'],
      contractorSet: ['contr1'],
      authorFIO: 'Иванов Иван',
      requestStatusSet: ['WAIT'],
      creationDate: '2024-01-15',
    } as any);
    expect(result).toBe('Номер заявки, Вид транспорта, Контрагент, ФИО создателя, Статус поездки, Дата создания заявки');
  });

  test('должен учитывать объекты с ключами как активные', () => {
    const formatRequest = formatFilterRequest(fieldLabels);
    expect(formatRequest({
      sortSetting: { property: undefined, directionAsc: false } as any,
      organizationSet: ['org1'],
    } as any)).toBe('Настройки сортировки, Организация');
  });
});
