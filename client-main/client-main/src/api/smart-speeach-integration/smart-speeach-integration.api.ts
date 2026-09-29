import type { MutationResultPair } from 'react-query';
import type { AxiosError } from 'axios';

import { useAPIMutation } from 'api';
import { SMART_SPEACH_INTEGRATION_AUDIO_MESSAGES } from './smart-speeach-integration.constants';
import { TAudioMessageResponse } from './smart-speeach-integration.types';
import { ignore } from 'utils';

/**
 * Расшифровка аудиосообщения.
 * Принимает аудио-файл (Blob/File), преобразует в FormData и отправляет
 * В ответе получаем преобразованное в текст сообщение
 *
 * @param audio — аудио-файл в поддерживаемом формате (WAV, MP3, OGG, FLAC, M4A, AAC).
 */
export const useRecognizeAudioMessage = (): MutationResultPair<
  TAudioMessageResponse,
  AxiosError<Error>,
  File | Blob,
  unknown
> => (
  useAPIMutation(({ http, process }, audioFile) => {
    const formData = new FormData();

    formData.append('audio', audioFile);

    return http
      .post<TAudioMessageResponse>(SMART_SPEACH_INTEGRATION_AUDIO_MESSAGES, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(process.getResponseData);
  }, {
    onSuccess: ignore,
  })
);
