import { useEffect, useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

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
