import * as React from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { FileExcelOutlined } from '@ant-design/icons';
import contentDisposition from 'content-disposition';
import { getFilenameFromHeader } from 'utils/downloads';
import { TYPE_RESPONSE } from '../../../constants/constants.app';
import { isJsonString } from '../../../utils/isJSONString';
import { ButtonLoad } from '../ButtonLoad';
import { Button } from '../Button/Button';

interface Format {
  ext: string;
  icon: JSX.Element | null;
}

const defaultFormats = [
  { ext: 'XLS', icon: <FileExcelOutlined /> },
  { ext: 'XLSX', icon: <FileExcelOutlined /> },
];

const DownloadButton: React.FC<{
  url: string;
  downloadUrl?: string;
  formats?: Format[];
  mkUrl?: (url: string, format?: string) => string;
  mkFileName?: (fileName: string, format?: string) => string;
  caption?: string;
  fileName?: string;
  theme?: 'primary' | 'secondary';
  className?: string;
  iconColor?: string;
}> = ({
  url,
  downloadUrl,
  caption = 'Скачать',
  formats = defaultFormats,
  mkUrl = (url, format) => (format ? `${url}/?format=${format}` : url),
  mkFileName = (fileName, format) => (format ? `${fileName}.${format.toLowerCase()}` : fileName),
  fileName = 'download',
  theme = 'primary',
  className,
  iconColor,
}) => {
  const { http, logger } = useAppStoreContext();

  const [, setBusy] = React.useState(false);

  const download = (selected: { key: React.ReactText } | undefined) => {
    const format = selected ? selected.key.toString() : undefined;

    setBusy(true);

    function downloadFile(filename: string, data: ArrayBuffer, type: string): void {
      const a = document.createElement('a');

      a.download = filename;

      const objectURL = URL.createObjectURL(new Blob([data], { type }));

      a.href = objectURL;
      a.click();
      URL.revokeObjectURL(objectURL);
    }

    function checkResponse(filePath: string, type: string, filename: string): void {
      const directory = downloadUrl ?? url.split('/')[1];

      http
        .get<ArrayBuffer>(directory + filePath, { responseType: 'arraybuffer' })
        .then(fileResponse => {
          if (
            fileResponse.headers['content-type'] === TYPE_RESPONSE.excel
            || fileResponse.headers['content-type'] === TYPE_RESPONSE.csv
          ) {
            const newFileName = getFilenameFromHeader(fileResponse);

            downloadFile(
              newFileName || fileName,
              fileResponse.data as ArrayBuffer,
              type === TYPE_RESPONSE.json ? fileResponse.headers['content-type'] : type
            );
          } else {
            setTimeout(() => checkResponse(filePath, type, filename), 1000);
          }
        });
    }

    http
      .get(mkUrl(url, format), { responseType: 'arraybuffer' })
      .then(({ data, headers }) => {
        const { type = headers['content-type'], filename = mkFileName(fileName, format) } = headers[
          'content-disposition'
        ]
          ? contentDisposition.parse(headers['content-disposition']).parameters
          : {};

        const reader = new FileReader();

        reader.readAsText(new Blob([data as ArrayBuffer], { type }));

        reader.onload = () => {
          if (typeof reader.result === 'string') {
            if (isJsonString(reader.result)) {
              const filePath = JSON.parse(reader.result).result_url;

              checkResponse(filePath, type, filename);
            } else {
              downloadFile(filename, data as ArrayBuffer, type);
            }
          }
        };
      })
      .catch(error => {
        // eslint-disable-next-line no-console
        console.error(error);
        logger.toNotify('error',
          'Не удалось скачать файл',
          'Ошибка',
          5,
          667
        );
      })
      .finally(() => {
        setBusy(false);
      });
  };

  const defaultDownload = () => download(formats.length ? { key: formats[0].ext } : undefined);

  return formats.length ? (
    <ButtonLoad
      type="import"
      onClick={defaultDownload}
      theme={theme}
      className={className}
      color={iconColor}
    >
      {caption}
    </ButtonLoad>
  ) : (
    <Button onClick={defaultDownload}>{caption}</Button>
  );
};

export default DownloadButton;
