import * as React from 'react';
import { Menu, Dropdown, Button } from 'antd';
import { FileExcelOutlined } from '@ant-design/icons';
import contentDisposition from 'content-disposition';
import { getFilenameFromHeader } from 'utils/downloads';
import { isJsonString } from '../utils/isJSONString';
import { useAppStore } from 'ioc';
import { TYPE_RESPONSE } from 'constants/app.constants';

interface Format {
  ext: string;
  icon: JSX.Element | null;
}

const defaultFormats = [
  { ext: 'XLS', icon: <FileExcelOutlined /> },
  { ext: 'XLSX', icon: <FileExcelOutlined /> },
];

const FormatMenu: React.FC<{ formats: Format[]; onClick: React.ComponentProps<typeof Menu>['onClick'] }> = ({
  formats,
  onClick,
}) => (
  <Menu onClick={onClick}>
    {formats.map(({ ext, icon }) => (
      <Menu.Item key={ext} icon={icon}>
        {ext}
      </Menu.Item>
    ))}
  </Menu>
);

const DownloadButton: React.FC<{
  url: string;
  directoryUrl?: string;
  formats?: Format[];
  mkUrl?: (url: string, format?: string) => string;
  mkFileName?: (fileName: string, format?: string) => string;
  caption?: string;
  fileName?: string;
  customElement?: JSX.Element;
  onLoad?: (status: boolean) => void;
}> = ({
  url,
  directoryUrl = '',
  caption = 'Скачать',
  formats = defaultFormats,
  mkUrl = (url, format) => (format ? `${url}?format=${format}` : url),
  mkFileName = (fileName, format) => (format ? `${fileName}.${format.toLowerCase()}` : fileName),
  fileName = 'download',
  customElement,
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onLoad = () => {},
}) => {
  const { http, logger } = useAppStore();

  const [busy, setBusy] = React.useState(false);

  React.useEffect(() => {
    onLoad(busy);
  }, [busy, onLoad]);

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
      const directory = directoryUrl ? `${directoryUrl}`.replace(/\/+/g, '/') : '';

      http.get<ArrayBuffer>(directory + filePath, { responseType: 'arraybuffer' }).then(fileResponse => {
        if (fileResponse.headers['content-type'] !== TYPE_RESPONSE.json) {
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
              const filePath = JSON.parse(reader.result).url;

              checkResponse(filePath, type, filename);
            } else {
              downloadFile(filename, data as ArrayBuffer, type);
            }
          }
        };
      })
      .catch(() => {
        logger.toError('Не удалось скачать файл', 'Ошибка');
      })
      .finally(() => {
        setBusy(false);
      });
  };

  const defaultDownload = () => download(formats.length ? { key: formats[0].ext } : undefined);

  if (customElement) {
    return React.cloneElement(customElement, { onClick: defaultDownload });
  }

  return formats.length ? (
    <Dropdown.Button
      disabled={busy}
      onClick={defaultDownload}
      overlay={<FormatMenu formats={formats} onClick={download} />}
      buttonsRender={([leftButton, rightButton]) => [
        leftButton,
        React.cloneElement(rightButton as React.ReactElement, { loading: busy }),
      ]}
    >
      {caption}
    </Dropdown.Button>
  ) : (
    <Button onClick={defaultDownload}>{caption}</Button>
  );
};

export default DownloadButton;
