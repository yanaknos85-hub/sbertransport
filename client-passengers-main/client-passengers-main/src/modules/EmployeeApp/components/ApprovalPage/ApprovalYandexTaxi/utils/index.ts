/**
 * Создает загрузку файла
 * @param data - octet/stream
 * @param fileName - имя файла
 * @param mimeType - MIME тип для блоба
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
  document.body.removeChild(a);
};
