import React, { useEffect } from 'react';
import moment from 'moment';
import { FormInstance } from 'antd';
import { observer } from 'mobx-react';
import { StoreNames } from 'stores';

import { IMetricsMainData } from 'modules/RedesignHome/types/Home.types';

import { TotalNumberChart } from './TotalNumberChart/TotalNumberChart';
import { MutualSettlementsChart } from './MutualSettlementsChart/MutualSettlementsChart';
import { SlaChart } from './SlaChart/SlaChart';
import { CsiChart } from './CsiChart/CsiChart';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';

import styles from './Charts.module.scss';

export const Charts = observer(({
  form,
}: {
  form: FormInstance;
}) => {
  const { [StoreNames.homeStore]: homeStore } = useAppStoreContext();
  const { organizationId } = useOrganizationContext();

  useEffect(() => {
    const metricsData = form.getFieldsValue();
    const data = {
      startDate: moment(metricsData.date.value[0]).startOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]'),
      finishDate: metricsData.date.value[1]
        ? moment(metricsData.date.value[1]).endOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]')
        : moment(metricsData.date.value[0]).endOf('day').format('YYYY-MM-DDTHH:mm:ss[Z]'),
      serviceTypes: ['PASSENGER', 'AUTOSERVICE', 'CARGO'],
      organizationIds: metricsData.isOrganization === 'organization' && organizationId ? [organizationId] : undefined,
    };
    homeStore.getMetrics(data as IMetricsMainData).then(el => homeStore.setMetricsData(el));
  }, []);

  // доделать как будет готова ручка metrics по budget
  // const budget = homeStore.metricsData.budget;
  const sla = homeStore.metricsData.sla?.dataSla;
  const csi = homeStore.metricsData.csi?.dataCsi;
  const totalCounts = homeStore.metricsData.totalCounts?.dataTotalCount;
  const payments = homeStore.metricsData.payments?.dataPayment;

  return (
    <div>
      <div className={styles.mainCharts}>
        {/* <Budget /> */}
        <MutualSettlementsChart payments={payments} />
      </div>
      <div className={styles.charts}>
        <TotalNumberChart totalCounts={totalCounts} />
        <SlaChart sla={sla} />
        <CsiChart csi={csi} />
      </div>
    </div>
  );
});
