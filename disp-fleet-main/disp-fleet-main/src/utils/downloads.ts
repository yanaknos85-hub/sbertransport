import { AxiosResponse } from 'axios';
import contentDisposition from 'content-disposition';
import { b64DecodeUnicode } from './utils';

/**
 * Возвращает имя файла из заголовка Content-Disposition
 * @param response - HTTP-ответ с файлом
 * @param settings - параметры обработки (base64Name: признак необходимости декодирования base64 имени)
 * @returns имя файла
 */
export const getFilenameFromHeader = (response: AxiosResponse, settings?: { base64Name?: boolean }): string => {
  const { filename = '' } = response.headers['content-disposition']
    ? contentDisposition.parse(response.headers['content-disposition']).parameters
    : {};

  // const headerLine = response.headers['content-disposition'];
  // const fileName = headerLine?.substring(headerLine.indexOf("'") + 2, headerLine.length);
  return settings?.base64Name ? b64DecodeUnicode(filename) : decodeURIComponent(filename);
};

/**
 * Создает загрузку файла в браузере
 * @param data - данные файла (octet/stream)
 * @param mimeType - MIME-тип файла для blob
 * @param fileName - имя файла для сохранения
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const handleClick = (data: any, mimeType: string, fileName: string) => {
  const blob = new Blob([data], { type: mimeType });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
};

/**
 * Загружает файл из HTTP-ответа
 * @param response - HTTP-ответ с данными файла
 * @param settings - параметры обработки (base64Name)
 */
export const downloadFile = (response: AxiosResponse<Blob>, settings?: { base64Name?: boolean }) => (
  handleClick(response.data, response.headers['content-type'], getFilenameFromHeader(response, settings))
);
