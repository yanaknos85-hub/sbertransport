import { MutationResultPair } from 'react-query';
import { useAPIMutation } from 'api';
import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { UsedFileFormatEnum } from 'stores/Trip/Trip.interface';

export const UploadResponse = t.partial({
  fileName: t.string,
  fileSize: t.number,
  fileFormat: ioTypeFromEnum<UsedFileFormatEnum>('UsedFileFormatEnum', UsedFileFormatEnum),
  id: t.number,
});

export type UploadResponse = t.TypeOf<typeof UploadResponse>;

export interface UploadParams { file: File }

export const useUpload = (
  url: string
): MutationResultPair<UploadResponse, unknown, UploadParams, unknown> => {
  return useAPIMutation<UploadResponse, unknown, UploadParams, unknown>(
    ({ http, process }, { file }) => {
      const formData = new FormData();
      formData.append('file', file);

      return http.post<UploadResponse>(url, formData).then(process.decodeResponseData(UploadResponse));
    }, {
      onSuccess: ({ logger }) => {
        logger.toMessage('success', 'Файл успешно загружен');
      },
      onError: ({ logger }) => {
        logger.toError('error', 'Ошибка загрузки файла');
      },
    });
};
