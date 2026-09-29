import { useEffect, useState } from 'react';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { useAppStoreContext } from './useAppStoreContext';

interface FetchImageHook {
  imageBlob: Blob | null;
  isLoading: boolean;
}

export const useFetchImage = (url: string, type: string): FetchImageHook => {
  const { http } = useAppStoreContext();

  const [imageBlob, setImageBlob] = useState<Blob | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const fetchImage = () => http
    .get(url, { responseType: 'arraybuffer' })
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    .then((response: any) => {
      if (response) {
        setImageBlob(new Blob([response.data], { type }));
      } else {
        setImageBlob(null);
      }
      setIsLoading(false);
    })
    .catch(() => {
      setImageBlob(null);
      setIsLoading(false);
    });

  useEffect(() => {
    setIsLoading(true);
    fetchImage();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [url]);

  return { imageBlob, isLoading };
};

interface ImageUrlHook {
  imageUrl: string;
  isLoading: boolean;
}

export const useImageUrl = (url: string, type: string): ImageUrlHook => {
  const { imageBlob, isLoading } = useFetchImage(url, type);
  const imageUrl = imageBlob ? window.URL.createObjectURL(imageBlob) : '';

  return { imageUrl, isLoading };
};

export const SavedFileInfo = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    fileFormat: t.string,
    fileName: t.string,
    fileSize: t.number,
    folder: tt.uuid,
  }),
]);

export type SavedFileInfo = t.TypeOf<typeof SavedFileInfo>;
