import { useTranslation } from 'i18n';
import { TableData } from '../types/types';

export const useTableData = (): { tableData: TableData } => {
  const { t } = useTranslation();
  const { firstColumn } = t.DeadlineSettings.tableData;
  const { lastColumn } = t.DeadlineSettings.tableData;

  const tableData: TableData = {
    taxi: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.deadline,
      },
      {
        claimStatus: firstColumn.driverSearch, deadline: '', description: lastColumn.anotherContractor,
      },
      {
        claimStatus: firstColumn.tripFinish, deadline: '', description: lastColumn.noMark,
      },
    ],
    personnel: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.joinMatching, deadline: '', description: lastColumn.notMatched,
      },
      {
        claimStatus: firstColumn.trip, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.routeApprovement, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.paymentOrder, deadline: '', description: lastColumn.deadline,
      },
      {
        claimStatus: firstColumn.waitingPayment, deadline: '', description: lastColumn.deadline,
      },
    ],
    public: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.tripApprovement, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.approvement, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.paymentOrder, deadline: '', description: lastColumn.deadline,
      },
      {
        claimStatus: firstColumn.waitingPayment, deadline: '', description: lastColumn.deadline,
      },
    ],
    personnelLimit: [{
      claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
    }],
    depLimit: [{
      claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
    }],
    carsharing: [{
      claimStatus: firstColumn.carsharingMatching, deadline: '', description: lastColumn.cancel,
    }],
    cargoDedicated: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.approved, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.inWork,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.deliveryConfirmation}»`,
      },
      {
        claimStatus: firstColumn.deliveryConfirmation,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.trial}»`,
      },
      {
        claimStatus: firstColumn.trial, deadline: '', description: `${lastColumn.notIn}«${firstColumn.lost}»`,
      },
    ],
    cargoCourier: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.approved, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.inWork,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.deliveryConfirmation}»`,
      },
      {
        claimStatus: firstColumn.deliveryConfirmation,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.trial}»`,
      },
      {
        claimStatus: firstColumn.trial, deadline: '', description: `${lastColumn.notIn}«${firstColumn.lost}»`,
      },
    ],
    cargoInterregional: [
      {
        claimStatus: firstColumn.matching, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.approved, deadline: '', description: lastColumn.cancel,
      },
      {
        claimStatus: firstColumn.inWork,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.deliveryConfirmation}»`,
      },
      {
        claimStatus: firstColumn.deliveryConfirmation,
        deadline: '',
        description: `${lastColumn.notIn}«${firstColumn.trial}»`,
      },
      {
        claimStatus: firstColumn.trial, deadline: '', description: `${lastColumn.notIn}«${firstColumn.lost}»`,
      },
    ],
  };

  return {
    tableData,
  };
};
