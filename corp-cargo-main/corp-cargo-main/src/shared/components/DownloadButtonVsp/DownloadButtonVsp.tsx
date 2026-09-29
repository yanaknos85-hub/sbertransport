import React, {
  FC, ReactText, useEffect, useState
} from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Progress } from 'antd';
import { FileExcelOutlined } from '@ant-design/icons';
import contentDisposition from 'content-disposition';
import { getFilenameFromHeader } from 'utils/downloads';
import { isJsonString } from '../../../utils/isJSONString';
import { ButtonLoadVsp } from '../ButtonLoadForVsp';
import { Button } from '../Button/Button';

interface Format {
  ext: string;
  icon: JSX.Element | null;
}

const defaultFormats = [
  { ext: 'XLS', icon: <FileExcelOutlined /> },
  { ext: 'XLSX', icon: <FileExcelOutlined /> },
];

const DownloadButtonVsp: FC<{
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

  const [busy, setBusy] = useState(false);
  const [progress, setProgress] = useState(0);
  const [cancelRequest, setCancelRequest] = useState(false);

  useEffect(() => {
    return () => {
      setCancelRequest(true);
    };
  }, []);

  const download = async (selected: { key: ReactText } | undefined) => {
    if (busy) return;

    const format = selected ? selected.key.toString() : undefined;
    setBusy(true);
    setProgress(0);
    setCancelRequest(false);

    try {
      const downloadFile = (filename: string, data: ArrayBuffer, type: string): void => {
        const a = document.createElement('a');
        a.download = filename;
        const objectURL = URL.createObjectURL(new Blob([data], { type }));
        a.href = objectURL;
        a.click();
        URL.revokeObjectURL(objectURL);
      };

      setProgress(10);
      const initialResponse = await http.get(mkUrl(url, format), {
        responseType: 'arraybuffer',
      });

      const { type = initialResponse.headers['content-type'], filename = mkFileName(fileName, format) }
          = initialResponse.headers['content-disposition']
            ? contentDisposition.parse(initialResponse.headers['content-disposition']).parameters
            : {};

      const textDecoder = new TextDecoder();
      const responseText = textDecoder.decode(initialResponse.data as ArrayBuffer);

      if (isJsonString(responseText)) {
        const jsonResponse = JSON.parse(responseText);
        if (jsonResponse.result_url) {
          setProgress(20);
          const directory = downloadUrl ?? url.split('/')[1];
          const filePath = jsonResponse.result_url;
          let attempt = 0;

          while (!cancelRequest) {
            try {
              setProgress(20 + Math.min(60, 60 * (1 - Math.pow(0.99, attempt))));

              const fileResponse = await http.get<ArrayBuffer>(directory + filePath, {
                responseType: 'arraybuffer',
              });

              const fileResponseText = textDecoder.decode(fileResponse.data as ArrayBuffer);

              if (isJsonString(fileResponseText)) {
                const fileJson = JSON.parse(fileResponseText);
                if (fileJson.in_progress) {
                  await new Promise(resolve => setTimeout(resolve, 1000));
                  attempt++;
                  continue;
                }
                throw new Error('Не удалось получить файл');
              }

              setProgress(90);
              const newFileName = getFilenameFromHeader(fileResponse) || filename;
              downloadFile(
                newFileName,
                fileResponse.data as ArrayBuffer,
                fileResponse.headers['content-type'] || type
              );
              setProgress(100);
              break;
            } catch (error) {
              if (cancelRequest) {
                break;
              }
              await new Promise(resolve => setTimeout(resolve, 1000));
              attempt++;
            }
          }

          if (cancelRequest) {
            logger.toNotify('warning', 'Загрузка отменена', 'Предупреждение', 3, 668);
            return;
          }
        } else {
          throw new Error('Не получен URL для скачивания файла');
        }
      } else {
        // Если сразу получили файл (не JSON)
        setProgress(100);
        downloadFile(filename, initialResponse.data as ArrayBuffer, type);
      }
    } catch (error) {
      if (!cancelRequest) {
        logger.toNotify('error', 'Не удалось скачать файл', 'Ошибка', 5, 667);
      }
      setProgress(0);
    } finally {
      if (!cancelRequest) {
        setTimeout(() => {
          setBusy(false);
          setTimeout(() => setProgress(0), 500);
        }, 500);
      }
    }
  };

  const defaultDownload = () => download(formats.length ? { key: formats[0].ext } : undefined);

  return formats.length ? (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 8,
    }}
    >
      <ButtonLoadVsp
        type="import"
        onClick={defaultDownload}
        theme={theme}
        className={className}
        color={iconColor}
        disabled={busy}
      >
        {caption}
      </ButtonLoadVsp>
      {busy && (
      <Progress
        percent={progress}
        type="circle"
        width={60}
        strokeWidth={10}
        status={progress < 100 ? 'active' : 'success'}
        strokeColor={{
          '0%': '#10BF6A',
          '100%': '#0091FF',
        }}
        showInfo={true}
        format={percent => (
          <span style={{
            color: '#0091FF',
          }}
          >
            {Math.floor(percent!)}
            %
          </span>
        )}
      />
      )}
    </div>
  ) : (
    <Button onClick={defaultDownload} disabled={busy}>
      {caption}
    </Button>
  );
};

export default DownloadButtonVsp;
