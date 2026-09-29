import { gettingListDownloadedContractorAll, DownloadFileList } from 'api/reports';
import { useEffect, useState } from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

export const useContractorMonths = (): {
  isLoading: boolean;
  setLoading(loading: boolean): void;
  monthListContractor: DownloadFileList[];
} => {
  const [monthListContractor, setMonthListContractor] = useState<DownloadFileList[]>([]);
  const { http, process } = useAppStoreContext();
  const [isLoading, setLoading] = useState(true);

  useEffect(() => {
    gettingListDownloadedContractorAll(http, process).then(data => setMonthListContractor(data));
    setLoading(false);
  }, []);

  return {
    isLoading,
    setLoading,
    monthListContractor,
  };
};
