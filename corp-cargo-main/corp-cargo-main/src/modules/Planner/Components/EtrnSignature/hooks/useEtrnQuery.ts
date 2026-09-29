import { useEffect, useMemo } from 'react';
import { useSearchEtrn } from 'api/etrn-signature/etrn-signature';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';
import { Tab } from 'modules/Planner/types';

export const useEtrnQuery = () => {
  const { plannerStore } = useAppStoreContext();
  const { organizationId } = useOrganizationContext();
  const isEtrnActive = plannerStore.activeTab === Tab.etrn;

  useEffect(() => {
    plannerStore.setEtrnPageSettings({
      page: 0,
      size: plannerStore.setEtrnListPageSetting.size,
    });
  }, [organizationId]);

  const query = useMemo(
    () => ({
      organizationId,
      pageSetting: {
        page: plannerStore.setEtrnListPageSetting.page,
        size: plannerStore.setEtrnListPageSetting.size,
      },
    }),
    [
      organizationId,
      plannerStore.setEtrnListPageSetting.page,
      plannerStore.setEtrnListPageSetting.size,
    ]
  );

  return useSearchEtrn(query, { enabled: isEtrnActive });
};
