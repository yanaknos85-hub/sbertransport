import * as React from 'react';
import { Menu, Dropdown, Button } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { FileExcelOutlined } from '@ant-design/icons';
import contentDisposition from 'content-disposition';
import { getFilenameFromHeader, handleClick } from 'utils/downloads';
import { cloneElement, useState } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TYPE_RESPONSE } from '../constants/constants.app';
import { isJsonString } from '../utils/isJSONString';

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
/* TODO Временное решение блокировки кнопки */
const DownloadButton: React.FC<{
  url: string;
  downloadUrl?: string;
  formats?: Format[];
  mkUrl?: (url: string, format?: string) => string;
  mkFileName?: (fileName: string, format?: string) => string;
  caption?: string;
  fileName?: string;
  customElement?: JSX.Element;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  params?: Record<string, any>;
  disabled?: boolean;
  skipFormat?: boolean;
}> = ({
  url,
  downloadUrl,
  caption = 'Скачать',
  formats = defaultFormats,
  mkUrl = (url, format) => (format ? `${url}/?format=${format}` : url),
  mkFileName = (fileName, format) => (format ? `${fileName}.${format.toLowerCase()}` : fileName),
  fileName = 'download',
  customElement,
  params,
  disabled = false,
  skipFormat = false,
}) => {
  const { http, logger } = useAppStoreContext();

  const [busy, setBusy] = useState(false);

  const checkResponse = (filePath: string, type: string, filename: string): void => {
    const directory = downloadUrl ?? url.split('/')[1];

    http
      .get<ArrayBuffer>(directory + filePath, { responseType: 'arraybuffer' })
      .then(fileResponse => {
        if (fileResponse.headers['content-type'] !== TYPE_RESPONSE.json) {
          const newFileName = getFilenameFromHeader(fileResponse);

          handleClick(
            fileResponse.data,
            type === TYPE_RESPONSE.json ? fileResponse.headers['content-type'] : type,
            newFileName || fileName
          );

          setBusy(false);
        } else {
          setTimeout(() => checkResponse(filePath, type, filename), 2000);
        }
      })
      .catch(() => {
        logger.toNotify('error',
          'Запрашиваемых тарифов слишком много. Конкретизируйте запрос, применив фильтры и нажав "поиск".',
          'Ошибка',
          5,
          667
        );
        setBusy(false);
      });
  };

  const download = (selected: { key: React.ReactText } | undefined) => {
    const format = !skipFormat && selected ? selected.key.toString() : undefined;
    setBusy(true);

    const requestUrl = skipFormat ? url : mkUrl(url, format); // не добавляем format если true

    http
      .get(requestUrl, { responseType: 'arraybuffer', params })
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
              handleClick(data, type, filename);
              setBusy(false);
            }
          } else {
            setBusy(false);
          }
        };
      })
      .catch(() => {
        logger.toNotify('error',
          'Не удалось скачать файл',
          'Ошибка',
          5,
          667
        );
        setBusy(false);
      });
  };

  const defaultDownload = () => download(skipFormat ? undefined : formats.length ? { key: formats[0].ext } : undefined);

  if (customElement) {
    return busy ? <SpinWrapped /> : React.cloneElement(customElement, { onClick: defaultDownload });
  }

  if (skipFormat) {
    return (
      <Button
        onClick={defaultDownload}
        disabled={disabled || busy}
        loading={busy}
      >
        {caption}
      </Button>
    );
  }

  return formats.length ? (
    <Dropdown.Button
      disabled={busy || disabled}
      onClick={defaultDownload}
      overlay={<FormatMenu formats={formats} onClick={download} />}
      buttonsRender={([leftButton, rightButton]) => [
        leftButton,
        cloneElement(rightButton as React.ReactElement, { loading: busy }),
      ]}
    >
      {caption}
    </Dropdown.Button>
  ) : (
    <Button onClick={defaultDownload} disabled={disabled}>{caption}</Button>
  );
};

export default DownloadButton;
