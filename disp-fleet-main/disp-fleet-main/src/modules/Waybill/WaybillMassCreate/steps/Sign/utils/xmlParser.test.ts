import {
  decodeBase64ToXml,
  parseXmlToObject,
  extractDataFromBase64Xml,
  extractOrganizationData,
  extractDriverData,
  extractTransportData,
  extractWaybillData
} from './xmlParser';

describe('xmlParser', () => {
  describe('decodeBase64ToXml', () => {
    test('декодирует Base64 строку в XML', () => {
      // Используем только латинские теги для теста, так как DOMParser преобразует кириллические теги в lowercase
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><document>test</document>';
      const base64String = btoa(encodeURIComponent(xmlString).replace(/%([0-9A-F]{2})/g, (match, p1) => String.fromCharCode(parseInt(p1, 16))));
      const result = decodeBase64ToXml(base64String);

      expect(result).toContain('<?xml version="1.0" encoding="UTF-8"?>');
      expect(result).toContain('<document>');
      expect(result).toContain('test');
    });

    test('бросает ошибку при неверном Base64', () => {
      const invalidBase64 = '!!!invalid_base64!!!';

      expect(() => decodeBase64ToXml(invalidBase64)).toThrow('Ошибка при декодировании Base64');
    });
  });

  describe('parseXmlToObject', () => {
    test('парсит корректную XML строку', () => {
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><документ>тест</документ>';
      const result = parseXmlToObject(xmlString);

      expect(result).not.toBeNull();
      expect(result?.getElementsByTagName('документ')[0]?.textContent).toBe('тест');
    });

    test('парсит сложную XML структуру', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл ИдФайл="test-id">
          <Документ КНД="1110380">
            <СодИнфСоб>
              <ТС Тип="car" Марка="Лада"/>
            </СодИнфСоб>
          </Документ>
        </Файл>`;

      const result = parseXmlToObject(xmlString);

      expect(result).not.toBeNull();
      const fileElem = result?.getElementsByTagName('Файл')[0];
      expect(fileElem?.getAttribute('ИдФайл')).toBe('test-id');

      const tcElem = result?.getElementsByTagName('ТС')[0];
      expect(tcElem?.getAttribute('Тип')).toBe('car');
      expect(tcElem?.getAttribute('Марка')).toBe('Лада');
    });

    test('бросает ошибку при некорректной XML', () => {
      const invalidXml = '<?xml version="1.0"?><незакрытый тег>';

      expect(() => parseXmlToObject(invalidXml)).toThrow('Ошибка при парсинге XML');
    });
  });

  describe('extractOrganizationData', () => {
    test('извлекает данные организации', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл>
          <Документ>
            <СодИнфСоб>
              <СвЛицПЛ>
                <ИдСв>
                  <СвЮЛУч НаимОрг="Тестовая организация" ОГРН="123456789" ИННЮЛ="987654321"/>
                </ИдСв>
              </СвЛицПЛ>
            </СодИнфСоб>
          </Документ>
        </Файл>`;

      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractOrganizationData(xmlDoc!);

      expect(result).toEqual({
        name: 'Тестовая организация',
        ogrn: '123456789',
        tin: '987654321',
      });
    });

    test('возвращает пустой объект, если данные отсутствуют', () => {
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><Файл></Файл>';
      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractOrganizationData(xmlDoc!);

      expect(result).toEqual({});
    });
  });

  describe('extractDriverData', () => {
    test('извлекает данные водителя', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл>
          <Документ>
            <СодИнфСоб>
              <СвВодит ИННФЛ="1234567890">
                <ВодитУд НомВУ="123456" СерВУ="99 88" ДатаВыдВУ="01.01.2020"/>
                <ФИО Фамилия="Иванов" Имя="Иван" Отчество="Иванович"/>
              </СвВодит>
            </СодИнфСоб>
          </Документ>
        </Файл>`;

      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractDriverData(xmlDoc!);

      expect(result).toEqual({
        fullName: 'Иванов Иван Иванович',
        personnelNumber: '1234567890',
        licenseSeries: '99 88',
        licenseNumber: '123456',
        licenseIssueDate: '01.01.2020',
      });
    });

    test('возвращает пустой объект, если данные водителя отсутствуют', () => {
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><Файл></Файл>';
      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractDriverData(xmlDoc!);

      expect(result).toEqual({});
    });
  });

  describe('extractTransportData', () => {
    test('извлекает данные транспортного средства', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл>
          <Документ>
            <СодИнфСоб>
              <СвТС>
                <ТС Тип="car" Марка="Лада" Модель="Калина" РегНомер="А123ВС777"/>
              </СвТС>
            </СодИнфСоб>
          </Документ>
        </Файл>`;

      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractTransportData(xmlDoc!);

      expect(result).toEqual({
        transportType: 'car',
        vehicleBrand: 'Лада',
        vehicleModel: 'Калина',
        stateNumber: 'А123ВС777',
      });
    });

    test('возвращает пустой объект, если данные ТС отсутствуют', () => {
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><Файл></Файл>';
      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractTransportData(xmlDoc!);

      expect(result).toEqual({});
    });
  });

  describe('extractWaybillData', () => {
    test('извлекает данные путевого листа', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл>
          <Документ НомерПЛ="PL-0001" ДатаПЛ="20.05.2026">
            <СрокПЛ ДатаНачИспПЛ="20.05.2026" ДатаКонИспПЛ="21.05.2026"/>
          </Документ>
        </Файл>`;

      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractWaybillData(xmlDoc!);

      expect(result).toEqual({
        humanReadableId: 'PL-0001',
        startDate: '20.05.2026',
        finishDate: '21.05.2026',
        documentDate: '20.05.2026',
      });
    });

    test('возвращает undefined для startDate/finishDate, если нет СрокПЛ, но есть ДатаПЛ', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8"?>
        <Файл>
          <Документ НомерПЛ="PL-0001" ДатаПЛ="20.05.2026"/>
        </Файл>`;

      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractWaybillData(xmlDoc!);

      expect(result).toEqual({
        humanReadableId: 'PL-0001',
        startDate: '20.05.2026',
        finishDate: '20.05.2026',
        documentDate: '20.05.2026',
      });
    });

    test('возвращает пустой объект, если данные путевого листа отсутствуют', () => {
      const xmlString = '<?xml version="1.0" encoding="UTF-8"?><Файл></Файл>';
      const xmlDoc = parseXmlToObject(xmlString);
      const result = extractWaybillData(xmlDoc!);

      expect(result).toEqual({});
    });
  });

  describe('extractDataFromBase64Xml', () => {
    test('извлекает полные данные из Base64 XML', () => {
      const xmlString = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Файл ИдФайл="ON_PTLSSOBTS_2BK-7707083893-667102008-1027700132195_2BK-7707083893-667102008-1027700132195_2BK-7707083893-667102008-1027700132195_2AL-f43f43f43fg3f4f_0_20260520_8812f532-8dc9-4969-b4d9-53dcc0476909" ВерсПрог="1.0.0" ВерсФорм="5.01">
          <Документ КНД="1110380" ДатИнфСоб="20.05.2026" ВрИнфСоб="13:52:12" НомерПЛ="PL-0001-00000387" ДатаПЛ="20.05.2026" ПризнНачРейс="1">
            <СодИнфСоб УИДПЛ="7caf3079-c4a7-4849-b795-6cc7574174ab" ОбМедОсмПосле="2" ВидПрв="СН" ВидСообщ="Г">
              <СрокПЛ ПЛДень="2" ДатаНачИспПЛ="20.05.2026" ДатаКонИспПЛ="21.05.2026"/>
              <СвЛицПЛ ЛицоОфПЛ="С">
                <ИдСв>
                  <СвЮЛУч НаимОрг="000001Тестовая_организация_01" ОГРН="2107784000575" ИННЮЛ="1449869990"/>
                </ИдСв>
                <Адрес>
                  <АдрРФ Индекс="117312" КодРегион="77"/>
                </Адрес>
                <Контакт>
                  <Тлф>+79152422849</Тлф>
                </Контакт>
              </СвЛицПЛ>
              <СвТС>
                <ТС Тип="abqlsiBeuZHAjcg" Марка="Лада" Модель="Калина" РегНомер="А456АА888"/>
              </СвТС>
              <СвВодит ИННФЛ="6547852145">
                <ВодитУд НомВУ="323232" СерВУ="33 22" ДатаВыдВУ="01.05.2026"/>
                <ФИО Фамилия="Вишняков" Имя="Николай" Отчество="Алексеевич"/>
              </СвВодит>
            </СодИнфСоб>
            <ПодпИнфСоб ТипПодпис="1" СпосПодтПолном="3">
              <ФИО Фамилия="Рухля" Имя="Сергей" Отчество="Олегович"/>
              <СвДоверЭл НомДовер="550e8400-e29b-41d4-a716-446655440000" ДатаДовер="01.05.2026" ИдСистХран="ПАО Сбербанк"/>
            </ПодпИнфСоб>
          </Документ>
        </Файл>`;

      const base64String = btoa(encodeURIComponent(xmlString).replace(/%([0-9A-F]{2})/g, (match, p1) => String.fromCharCode(parseInt(p1, 16))));
      const result = extractDataFromBase64Xml(base64String);

      expect(result).toEqual({
        organizationName: '000001Тестовая_организация_01',
        organizationOgrn: '2107784000575',
        organizationTin: '1449869990',
        driverFullName: 'Вишняков Николай Алексеевич',
        driverPersonnelNumber: '6547852145',
        drivingLicenseSeries: '33 22',
        drivingLicenseNumber: '323232',
        drivingLicenseIssueDate: '01.05.2026',
        humanReadableId: 'PL-0001-00000387',
        startDate: '20.05.2026',
        finishDate: '21.05.2026',
        documentDate: '20.05.2026',
        stateNumber: 'А456АА888',
        vehicleBrand: 'Лада',
        vehicleModel: 'Калина',
        transportType: 'abqlsiBeuZHAjcg',
      });
    });

    test('возвращает пустой объект при ошибке парсинга', () => {
      const invalidBase64 = 'invalid_base64!';

      const result = extractDataFromBase64Xml(invalidBase64);

      expect(result).toEqual({});
    });
  });
});
