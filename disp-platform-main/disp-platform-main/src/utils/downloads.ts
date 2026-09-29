import { AxiosResponse } from 'axios';
import contentDisposition from 'content-disposition';

/**
 * Извлекает имя файла из header-а Content-Disposition HTTP-ответа
 * @param response - AxiosResponse с заголовками
 * @returns имя файла, извлеченное из header-а
 */
export const getFilenameFromHeader = (response: AxiosResponse): string => {
  const { filename = '' } = response.headers['content-disposition']
    ? contentDisposition.parse(response.headers['content-disposition']).parameters
    : {};

  // const headerLine = response.headers['content-disposition'];
  // const fileName = headerLine?.substring(headerLine.indexOf("'") + 2, headerLine.length);
  return decodeURIComponent(filename);
};

/**
 * Создает и инициирует загрузку файла в браузере
 * @param data - данные файла в формате octet/stream
 * @param fileName - имя файла для сохранения
 * @param mimeType - MIME тип содержимого для Blob
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
